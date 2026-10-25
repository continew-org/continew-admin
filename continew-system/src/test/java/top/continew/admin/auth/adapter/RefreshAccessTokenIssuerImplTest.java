/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.auth.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.AuthTokenService;
import top.continew.admin.auth.service.SessionActivityService;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.auth.refresh.token.exception.RefreshTokenException;
import top.continew.starter.auth.refresh.token.model.IssuedAccessToken;
import top.continew.starter.auth.refresh.token.model.RefreshSession;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

/**
 * 刷新前的会话状态校验测试：闲置判定三条分支（超阈值撤销、未超阈值放行、无记录按 createdAt
 * 兜底）以及客户端、租户、用户状态校验。
 *
 * @author luoqiz
 * @since 5.0.0
 */
class RefreshAccessTokenIssuerImplTest {

    private static final String CLIENT_ID = "client-1";
    private static final String SESSION_ID = "sid-1";
    private static final long TENANT_ID = 1L;
    private static final long USER_ID = 1L;
    private static final String INVALID_MESSAGE = "登录状态已失效，请重新登录";
    private static final String IDLE_MESSAGE = "长时间未操作，请重新登录";

    private ClientService clientService;
    private AuthTokenService authTokenService;
    private TenantApi tenantApi;
    private UserService userService;
    private SessionActivityService sessionActivityService;
    private RefreshAccessTokenIssuerImpl issuer;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        clientService = mock(ClientService.class);
        authTokenService = mock(AuthTokenService.class);
        tenantApi = mock(TenantApi.class);
        userService = mock(UserService.class);
        sessionActivityService = mock(SessionActivityService.class);
        issuer = new RefreshAccessTokenIssuerImpl(authTokenService, clientService, tenantApi,
            userService,
            sessionActivityService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldRejectWhenClientMissing() {
        when(clientService.getByClientId(CLIENT_ID)).thenReturn(null);

        RefreshTokenException exception = assertThrows(RefreshTokenException.class,
            () -> issuer.issue(this.session(System.currentTimeMillis()), request, response));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(INVALID_MESSAGE, exception.getMessage());
    }

    @Test
    void shouldRejectWhenClientDisabled() {
        when(clientService.getByClientId(CLIENT_ID))
            .thenReturn(this.client(0L, DisEnableStatusEnum.DISABLE));

        RefreshTokenException exception = assertThrows(RefreshTokenException.class,
            () -> issuer.issue(this.session(System.currentTimeMillis()), request, response));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(INVALID_MESSAGE, exception.getMessage());
    }

    @Test
    void shouldRejectWhenIdleThresholdExceeded() {
        when(clientService.getByClientId(CLIENT_ID))
            .thenReturn(this.client(60L, DisEnableStatusEnum.ENABLE));
        when(sessionActivityService.getLastActiveTime(SESSION_ID))
            .thenReturn(System.currentTimeMillis() - 120_000L);

        RefreshTokenException exception = assertThrows(RefreshTokenException.class,
            () -> issuer.issue(this.session(System.currentTimeMillis()), request, response));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(IDLE_MESSAGE, exception.getMessage());
        // 必须在签发新令牌之前抛出，否则会先续命再撤销
        verify(authTokenService, never()).issueAccessToken(any(), any(), any(), any(), any(),
            any());
    }

    @Test
    void shouldAllowWhenActiveWithinThreshold() {
        when(clientService.getByClientId(CLIENT_ID))
            .thenReturn(this.client(60L, DisEnableStatusEnum.ENABLE));
        when(sessionActivityService.getLastActiveTime(SESSION_ID))
            .thenReturn(System.currentTimeMillis() - 10_000L);
        this.stubUserAndToken();

        try (MockedStatic<TenantUtils> tenantUtils = this.tenantUtils()) {
            IssuedAccessToken issued =
                issuer.issue(this.session(System.currentTimeMillis()), request,
                    response);

            assertEquals("new-token", issued.accessToken());
            assertEquals(1800L, issued.expiresIn());
            assertEquals(TENANT_ID, issued.tenantId().longValue());
        }
    }

    @Test
    void shouldFallBackToCreatedAtWhenNoActivityRecord() {
        when(clientService.getByClientId(CLIENT_ID))
            .thenReturn(this.client(60L, DisEnableStatusEnum.ENABLE));
        // 活跃键被淘汰（如 Redis maxmemory）时没有活跃记录，按会话创建时间兜底
        when(sessionActivityService.getLastActiveTime(SESSION_ID)).thenReturn(null);

        RefreshTokenException exception = assertThrows(RefreshTokenException.class, () -> issuer
            .issue(this.session(System.currentTimeMillis() - 120_000L), request, response));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(IDLE_MESSAGE, exception.getMessage());
    }

    @Test
    void shouldAllowFreshSessionWithoutActivityRecord() {
        when(clientService.getByClientId(CLIENT_ID))
            .thenReturn(this.client(60L, DisEnableStatusEnum.ENABLE));
        // 刚登录还没有业务请求：创建时间在阈值内，不能误判为闲置
        when(sessionActivityService.getLastActiveTime(SESSION_ID)).thenReturn(null);
        this.stubUserAndToken();

        try (MockedStatic<TenantUtils> tenantUtils = this.tenantUtils()) {
            IssuedAccessToken issued =
                issuer.issue(this.session(System.currentTimeMillis() - 1_000L),
                    request, response);

            assertEquals("new-token", issued.accessToken());
        }
    }

    @Test
    void shouldSkipIdleCheckWhenTimeoutDisabled() {
        when(clientService.getByClientId(CLIENT_ID))
            .thenReturn(this.client(0L, DisEnableStatusEnum.ENABLE));
        this.stubUserAndToken();

        try (MockedStatic<TenantUtils> tenantUtils = this.tenantUtils()) {
            IssuedAccessToken issued =
                issuer.issue(this.session(System.currentTimeMillis()), request,
                    response);

            assertEquals("new-token", issued.accessToken());
            // 未配置闲置失效时不应读取活跃时间
            verify(sessionActivityService, never()).getLastActiveTime(any());
        }
    }

    @Test
    void shouldRejectWhenTenantInvalid() {
        when(clientService.getByClientId(CLIENT_ID))
            .thenReturn(this.client(0L, DisEnableStatusEnum.ENABLE));
        doThrow(new BusinessException("租户已停用")).when(tenantApi).checkStatus(TENANT_ID);

        RefreshTokenException exception = assertThrows(RefreshTokenException.class,
            () -> issuer.issue(this.session(System.currentTimeMillis()), request, response));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("租户已停用", exception.getMessage());
    }

    @Test
    void shouldRejectWhenUserMissing() {
        when(clientService.getByClientId(CLIENT_ID))
            .thenReturn(this.client(0L, DisEnableStatusEnum.ENABLE));
        when(userService.getById(USER_ID)).thenReturn(null);

        try (MockedStatic<TenantUtils> tenantUtils = this.tenantUtils()) {
            RefreshTokenException exception = assertThrows(RefreshTokenException.class, () -> issuer
                .issue(this.session(System.currentTimeMillis()), request, response));

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
            assertEquals(INVALID_MESSAGE, exception.getMessage());
        }
    }

    private RefreshSession session(long createdAt) {
        RefreshSession session = new RefreshSession();
        session.setSessionId(SESSION_ID);
        session.setUserId(USER_ID);
        session.setClientId(CLIENT_ID);
        session.setTenantId(TENANT_ID);
        session.setCreatedAt(createdAt);
        return session;
    }

    private ClientResp client(Long idleTimeoutSeconds, DisEnableStatusEnum status) {
        ClientResp client = new ClientResp();
        client.setClientId(CLIENT_ID);
        client.setStatus(status);
        client.setRefreshTokenIdleTimeout(idleTimeoutSeconds);
        return client;
    }

    private void stubUserAndToken() {
        UserDO user = new UserDO();
        user.setUsername("admin");
        when(userService.getById(USER_ID)).thenReturn(user);
        when(authTokenService.issueAccessToken(any(), any(), any(), any(), any(), any()))
            .thenReturn(
                LoginResp.builder().accessToken("new-token").expiresIn(1800L).tenantId(TENANT_ID)
                    .build());
    }

    private MockedStatic<TenantUtils> tenantUtils() {
        MockedStatic<TenantUtils> mocked = mockStatic(TenantUtils.class);
        mocked.when(() -> TenantUtils.execute(any(), any(Runnable.class)))
            .thenAnswer(invocation -> {
                invocation.getArgument(1, Runnable.class).run();
                return null;
            });
        return mocked;
    }
}
