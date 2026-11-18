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

package top.continew.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.DeptService;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.auth.refresh.token.exception.RefreshTokenException;
import top.continew.starter.auth.refresh.token.service.RefreshTokenService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证令牌签发服务单元测试。
 *
 * <p>覆盖登录状态重载（客户端缺失/禁用/策略变更/凭证变更/通过）、用户与部门状态
 * 校验的刷新与登录两种失败出口、Access Token 有效期收敛规则与剩余秒数换算。
 * 签发主链路依赖 Sa-Token 会话写入，本类聚焦其外围判定逻辑。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class AuthTokenServiceImplTest {

    private RoleService roleService;
    private OptionService optionService;
    private DeptService deptService;
    private UserService userService;
    private ClientService clientService;
    private RefreshTokenService refreshTokenService;
    private TenantApi tenantApi;
    private AuthTokenServiceImpl authTokenService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<RedisUtils> redisUtilsMock;

    @BeforeEach
    void setUp() {
        roleService = mock(RoleService.class);
        optionService = mock(OptionService.class);
        deptService = mock(DeptService.class);
        userService = mock(UserService.class);
        clientService = mock(ClientService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        tenantApi = mock(TenantApi.class);
        // RedisUtils 静态块经 SpringUtil 取 RedissonClient，先桩 SpringUtil 再模拟 RedisUtils
        springUtilMock = mockStatic(SpringUtil.class);
        redisUtilsMock = mockStatic(RedisUtils.class);
        authTokenService = new AuthTokenServiceImpl(roleService, optionService, deptService,
            userService, clientService, refreshTokenService, tenantApi, null);
    }

    @AfterEach
    void tearDown() {
        redisUtilsMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldReloadStateWhenClientUnchanged() {
        ClientResp expected = client("pc", DisEnableStatusEnum.ENABLE, 1800L, 86400L, 60L);
        ClientResp current = client("pc", DisEnableStatusEnum.ENABLE, 1800L, 86400L, 60L);
        when(clientService.getByClientId("pc")).thenReturn(current);
        UserDO oldUser = user(1L, 100L);
        UserDO newUser = user(1L, 100L);
        when(userService.getById(1L)).thenReturn(newUser);
        stubStatusChecks(100L);
        LoginStateHolder state;
        try (MockedStatic<TenantUtils> tenantUtils = mockStatic(TenantUtils.class)) {
            tenantUtils.when(() -> TenantUtils.execute(any(), any(Runnable.class)))
                .thenAnswer(passThrough());
            // reloadLoginState 为私有方法，经同包反射驱动
            state = Reflections.invokeReloadLoginState(authTokenService, oldUser, expected, 0L);
        }
        assertEquals(newUser, state.user);
        assertEquals(current, state.client);
    }

    @Test
    void shouldRejectWhenClientMissingOnReload() {
        when(clientService.getByClientId("pc")).thenReturn(null);
        assertReloadThrows("客户端不存在");
    }

    @Test
    void shouldRejectWhenClientDisabledOnReload() {
        when(clientService.getByClientId("pc")).thenReturn(client("pc",
            DisEnableStatusEnum.DISABLE, 1800L, 86400L, 60L));
        assertReloadThrows("客户端已禁用");
    }

    @Test
    void shouldRejectWhenSecurityPolicyChangedOnReload() {
        when(clientService.getByClientId("pc")).thenReturn(client("pc",
            DisEnableStatusEnum.ENABLE, 900L, 86400L, 60L));
        assertReloadThrows("客户端认证配置已变更，请重新登录");
    }

    @Test
    void shouldRejectWhenPasswordResetTimeChangedOnReload() {
        ClientResp expected = client("pc", DisEnableStatusEnum.ENABLE, 1800L, 86400L, 60L);
        when(clientService.getByClientId("pc")).thenReturn(client("pc",
            DisEnableStatusEnum.ENABLE, 1800L, 86400L, 60L));
        UserDO oldUser = user(1L, 100L);
        oldUser.setPwdResetTime(LocalDateTime.of(2026, 1, 2, 0, 0));
        UserDO newUser = user(1L, 100L);
        newUser.setPwdResetTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(userService.getById(1L)).thenReturn(newUser);
        stubStatusChecks(100L);
        try (MockedStatic<TenantUtils> tenantUtils = mockStatic(TenantUtils.class)) {
            tenantUtils.when(() -> TenantUtils.execute(any(), any(Runnable.class)))
                .thenAnswer(passThrough());
            BusinessException exception = assertThrows(BusinessException.class, () -> Reflections
                .invokeReloadLoginState(authTokenService, oldUser, expected, 0L));
            assertEquals("账号凭证已变更，请重新登录", exception.getMessage());
        }
    }

    @Test
    void shouldRejectDisabledUserForLoginRequest() {
        UserDO user = user(1L, 100L);
        user.setStatus(DisEnableStatusEnum.DISABLE);
        DeptDO dept = new DeptDO();
        dept.setId(100L);
        dept.setStatus(DisEnableStatusEnum.ENABLE);
        when(deptService.getById(100L)).thenReturn(dept);

        BusinessException exception = assertThrows(BusinessException.class, () -> Reflections
            .invokeCheckUserStatus(authTokenService, user, false));
        assertEquals("此账号已被禁用，如有疑问，请联系管理员", exception.getMessage());
    }

    @Test
    void shouldRejectMissingDeptForRefreshRequest() {
        UserDO user = user(1L, 100L);
        when(deptService.getById(100L)).thenReturn(null);

        RefreshTokenException exception =
            assertThrows(RefreshTokenException.class, () -> Reflections
                .invokeCheckUserStatus(authTokenService, user, true));
        assertEquals("此账号所属部门不存在", exception.getMessage());
    }

    @Test
    void shouldCapAccessTokenTimeoutToSessionTimeout() {
        Reflections.invokeGetEffectiveTimeout(authTokenService);
    }

    @Test
    void shouldRoundUpRemainingSeconds() {
        Reflections.invokeRemainingSeconds(authTokenService);
    }

    private void assertReloadThrows(String expectedMessage) {
        UserDO oldUser = user(1L, 100L);
        ClientResp client = client("pc", DisEnableStatusEnum.ENABLE, 1800L, 86400L, 60L);
        try (MockedStatic<TenantUtils> tenantUtils = mockStatic(TenantUtils.class)) {
            tenantUtils.when(() -> TenantUtils.execute(any(), any(Runnable.class)))
                .thenAnswer(passThrough());
            BusinessException exception = assertThrows(BusinessException.class, () -> Reflections
                .invokeReloadLoginState(authTokenService, oldUser, client, 0L));
            assertEquals(expectedMessage, exception.getMessage());
        }
    }

    private void stubStatusChecks(Long deptId) {
        DeptDO dept = new DeptDO();
        dept.setId(deptId);
        dept.setStatus(DisEnableStatusEnum.ENABLE);
        when(deptService.getById(deptId)).thenReturn(dept);
    }

    private org.mockito.stubbing.Answer<Void> passThrough() {
        return invocation -> {
            ((Runnable) invocation.getArgument(1)).run();
            return null;
        };
    }

    private ClientResp client(String clientId, DisEnableStatusEnum status, Long activeTimeout,
        Long timeout, Long refreshTokenTimeout) {
        ClientResp client = new ClientResp();
        client.setClientId(clientId);
        client.setClientType("PC");
        client.setAuthType(List.of("ACCOUNT"));
        client.setStatus(status);
        client.setActiveTimeout(activeTimeout);
        client.setTimeout(timeout);
        client.setRefreshTokenTimeout(refreshTokenTimeout);
        client.setRefreshTokenMode(
            top.continew.starter.auth.refresh.token.enums.RefreshTokenModeEnum.BODY);
        client.setIsConcurrent(true);
        client.setMaxLoginCount(-1);
        return client;
    }

    private UserDO user(Long id, Long deptId) {
        UserDO user = new UserDO();
        user.setId(id);
        user.setDeptId(deptId);
        user.setStatus(DisEnableStatusEnum.ENABLE);
        user.setPwdResetTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        return user;
    }

    static class LoginStateHolder {

        UserDO user;
        ClientResp client;
    }

    static class Reflections {

        static LoginStateHolder invokeReloadLoginState(Object service, UserDO user,
            ClientResp client, Long tenantId) {
            try {
                java.lang.reflect.Method m =
                    service.getClass().getDeclaredMethod("reloadLoginState",
                        UserDO.class, ClientResp.class, Long.class);
                m.setAccessible(true);
                Object result = m.invoke(service, user, client, tenantId);
                java.lang.reflect.Method userAccessor = result.getClass().getDeclaredMethod("user");
                java.lang.reflect.Method clientAccessor = result.getClass()
                    .getDeclaredMethod("client");
                userAccessor.setAccessible(true);
                clientAccessor.setAccessible(true);
                LoginStateHolder holder = new LoginStateHolder();
                holder.user = (UserDO) userAccessor.invoke(result);
                holder.client = (ClientResp) clientAccessor.invoke(result);
                return holder;
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw new IllegalStateException(cause);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        static void invokeCheckUserStatus(Object service, UserDO user, boolean refreshRequest) {
            try {
                java.lang.reflect.Method m = service.getClass()
                    .getDeclaredMethod("checkUserStatus", UserDO.class, boolean.class);
                m.setAccessible(true);
                m.invoke(service, user, refreshRequest);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw new IllegalStateException(cause);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        static void invokeGetEffectiveTimeout(Object service) {
            try {
                java.lang.reflect.Method m = service.getClass()
                    .getDeclaredMethod("getEffectiveAccessTokenTimeout", Long.class, long.class);
                m.setAccessible(true);
                // 配置 -1 或超过会话有效期时收敛到会话有效期
                Object capped = m.invoke(service, -1L, 3600L);
                org.junit.jupiter.api.Assertions.assertEquals(3600L, capped);
                Object larger = m.invoke(service, 7200L, 3600L);
                org.junit.jupiter.api.Assertions.assertEquals(3600L, larger);
                Object exact = m.invoke(service, 1800L, 3600L);
                org.junit.jupiter.api.Assertions.assertEquals(1800L, exact);
                try {
                    m.invoke(service, (Long) null, 3600L);
                    org.junit.jupiter.api.Assertions
                        .fail("未配置 Access Token 有效期时应抛出异常");
                } catch (java.lang.reflect.InvocationTargetException e) {
                    org.junit.jupiter.api.Assertions.assertTrue(e
                        .getCause() instanceof BusinessException);
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        static void invokeRemainingSeconds(Object service) {
            try {
                java.lang.reflect.Method m = service.getClass()
                    .getDeclaredMethod("remainingSeconds", long.class);
                m.setAccessible(true);
                long now = System.currentTimeMillis();
                // 2.4 秒 → 3 秒（向上取整）
                Object rounded = m.invoke(service, now + 2400);
                org.junit.jupiter.api.Assertions.assertEquals(3L, rounded);
                // 已过期 → 未授权异常
                try {
                    m.invoke(service, now - 1000);
                    org.junit.jupiter.api.Assertions.fail("已过期应抛出未授权异常");
                } catch (java.lang.reflect.InvocationTargetException e) {
                    org.junit.jupiter.api.Assertions.assertTrue(e
                        .getCause() instanceof RefreshTokenException);
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
