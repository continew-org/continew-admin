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

package top.continew.admin.config.satoken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.servlet.model.SaRequestForServlet;
import cn.dev33.satoken.servlet.model.SaResponseForServlet;
import cn.dev33.satoken.servlet.model.SaStorageForServlet;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feiniaojin.gracefulresponse.api.ResponseStatusFactory;
import com.feiniaojin.gracefulresponse.defaults.DefaultResponseStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.continew.admin.auth.service.SessionActivityService;
import top.continew.admin.auth.support.ClientPolicyCache;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.auth.refresh.token.api.AccessSessionValidator;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

/**
 * SaExtensionInterceptor 短路路径线程本地清理测试。
 *
 * <p>回归 C1：preHandle 返回 false 时 afterCompletion 不再回调，若不在短路前清除
 * UserContext 线程本地，池化线程会残留上一请求的上下文被下一请求复用。断言方式是
 * 短路后再次读取上下文：若已清除则重新走 {@link StpUtil#getSession()}（调用 2 次），
 * 未清除则命中线程本地（仅 1 次）。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
class SaExtensionInterceptorTest {

    private AccessSessionValidator accessSessionValidator;
    private SessionActivityService sessionActivityService;
    private ClientPolicyCache clientPolicyCache;
    private SaExtensionInterceptor interceptor;
    private MockedStatic<StpUtil> stpUtil;
    private MockedStatic<SaRouter> saRouter;
    private UserContext cachedContext;

    @BeforeEach
    void setUp() {
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());
        accessSessionValidator = mock(AccessSessionValidator.class);
        sessionActivityService = mock(SessionActivityService.class);
        clientPolicyCache = mock(ClientPolicyCache.class);
        when(clientPolicyCache.get(any()))
            .thenReturn(ClientPolicyCache.ClientPolicy.DEFAULT);
        LoginPasswordProperties passwordProperties = new LoginPasswordProperties();
        passwordProperties.setExcludes(new String[] {"/user/profile/password", "/auth/user/info"});
        interceptor = new SaExtensionInterceptor(handle -> {
        }, accessSessionValidator, sessionActivityService, clientPolicyCache, passwordProperties);
        // 路由匹配策略由 sa-token 的 Spring 集成注入，纯单元测试需要自己给出结果
        saRouter = mockStatic(SaRouter.class);
        saRouter.when(() -> SaRouter.isMatchCurrURI(any(String[].class))).thenReturn(false);
        stpUtil = mockStatic(StpUtil.class);
        stpUtil.when(StpUtil::isLogin).thenReturn(true);
        stpUtil.when(StpUtil::getTokenValue).thenReturn("access-token");
        cachedContext = new UserContext();
        SaSession saSession = mock(SaSession.class);
        when(saSession.getModel(SaSession.USER, UserContext.class)).thenReturn(cachedContext);
        stpUtil.when(StpUtil::getSession).thenReturn(saSession);
    }

    @AfterEach
    void tearDown() {
        saRouter.close();
        stpUtil.close();
        UserContextHolder.clearContext();
        SaManager.setSaTokenContext(null);
    }

    @Test
    void shouldClearThreadLocalBefore401ShortCircuit() throws Exception {
        when(accessSessionValidator.getInvalidReason("access-token")).thenReturn("已强制下线");
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ResponseStatusFactory factory = this.statusFactory();
            springUtil.when(() -> SpringUtil.getBean(ResponseStatusFactory.class))
                .thenReturn(factory);
            springUtil.when(() -> SpringUtil.getBean(ObjectMapper.class))
                .thenReturn(new ObjectMapper());
            try (MockedStatic<TenantContextHolder> tenantHolder = mockStatic(
                TenantContextHolder.class)) {
                tenantHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(false);
                MockHttpServletRequest request = this.request("/system/user");
                MockHttpServletResponse response = new MockHttpServletResponse();

                boolean flag = interceptor.preHandle(request, response, new Object());

                assertFalse(flag);
                assertEquals(401, response.getStatus());
                // preHandle 内已写入过一次上下文（getContext），短路后必须清除；若残留，
                // 第二次读取会命中线程本地（getSession 仅 1 次），清除后为 2 次。
                UserContextHolder.getContext();
                stpUtil.verify(StpUtil::getSession, times(2));
            }
        }
    }

    @Test
    void shouldClearThreadLocalBefore403ShortCircuit() throws Exception {
        when(accessSessionValidator.getInvalidReason("access-token")).thenReturn(null);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ResponseStatusFactory factory = this.statusFactory();
            springUtil.when(() -> SpringUtil.getBean(ResponseStatusFactory.class))
                .thenReturn(factory);
            springUtil.when(() -> SpringUtil.getBean(ObjectMapper.class))
                .thenReturn(new ObjectMapper());
            try (MockedStatic<TenantContextHolder> tenantHolder = mockStatic(
                TenantContextHolder.class)) {
                tenantHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(true);
                tenantHolder.when(TenantContextHolder::getTenantId).thenReturn(2L);
                stpUtil.when(() -> StpUtil.getExtra("access-token", "tenantId")).thenReturn(1L);
                MockHttpServletRequest request = this.request("/system/user");
                MockHttpServletResponse response = new MockHttpServletResponse();

                boolean flag = interceptor.preHandle(request, response, new Object());

                assertFalse(flag);
                assertEquals(403, response.getStatus());
                UserContextHolder.getContext();
                stpUtil.verify(StpUtil::getSession, times(2));
            }
        }
    }

    @Test
    void shouldClearThreadLocalInAfterCompletionOnNormalPath() throws Exception {
        when(accessSessionValidator.getInvalidReason("access-token")).thenReturn(null);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ResponseStatusFactory factory = this.statusFactory();
            springUtil.when(() -> SpringUtil.getBean(ResponseStatusFactory.class))
                .thenReturn(factory);
            springUtil.when(() -> SpringUtil.getBean(ObjectMapper.class))
                .thenReturn(new ObjectMapper());
            try (MockedStatic<TenantContextHolder> tenantHolder = mockStatic(
                TenantContextHolder.class)) {
                tenantHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(false);
                MockHttpServletRequest request = this.request("/system/user");
                MockHttpServletResponse response = new MockHttpServletResponse();

                boolean flag = interceptor.preHandle(request, response, new Object());
                interceptor.afterCompletion(request, response, new Object(), null);

                assertTrue(flag);
                UserContextHolder.getContext();
                stpUtil.verify(StpUtil::getSession, times(2));
            }
        }
    }

    @Test
    void shouldRejectWhenIdleExpiredOnBusinessPath() throws Exception {
        when(accessSessionValidator.getInvalidReason("access-token")).thenReturn(null);
        when(sessionActivityService.isIdleExpired(any(), anyLong())).thenReturn(true);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ResponseStatusFactory factory = this.statusFactory();
            springUtil.when(() -> SpringUtil.getBean(ResponseStatusFactory.class))
                .thenReturn(factory);
            springUtil.when(() -> SpringUtil.getBean(ObjectMapper.class))
                .thenReturn(new ObjectMapper());
            try (MockedStatic<TenantContextHolder> tenantHolder = mockStatic(
                TenantContextHolder.class)) {
                tenantHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(false);
                MockHttpServletResponse response = new MockHttpServletResponse();

                boolean flag = interceptor.preHandle(this.request("/system/user"), response,
                    new Object());

                assertFalse(flag);
                assertEquals(401, response.getStatus());
            }
        }
    }

    @Test
    void shouldAllowSelfServicePathWhenIdleExpired() throws Exception {
        when(accessSessionValidator.getInvalidReason("access-token")).thenReturn(null);
        // 已配置闲置失效，才会进入放行路径匹配
        when(clientPolicyCache.get(any())).thenReturn(new ClientPolicyCache.ClientPolicy(3600L,
            2_592_000L));
        saRouter.when(() -> SaRouter.isMatchCurrURI(any(String[].class))).thenReturn(true);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ResponseStatusFactory factory = this.statusFactory();
            springUtil.when(() -> SpringUtil.getBean(ResponseStatusFactory.class))
                .thenReturn(factory);
            springUtil.when(() -> SpringUtil.getBean(ObjectMapper.class))
                .thenReturn(new ObjectMapper());
            try (MockedStatic<TenantContextHolder> tenantHolder = mockStatic(
                TenantContextHolder.class)) {
                tenantHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(false);
                MockHttpServletResponse response = new MockHttpServletResponse();

                // 密码过期放行的改密路径：即使已闲置也不能拦，否则用户只能重新登录才能改密
                boolean flag = interceptor.preHandle(this
                    .request("/user/profile/password"), response, new Object());

                assertTrue(flag);
                // 短路求值：自助路径不查闲置，但活跃时间仍要记录
                verify(sessionActivityService, never()).isIdleExpired(any(), anyLong());
                verify(sessionActivityService).touch(any(), any());
            }
        }
    }

    @Test
    void shouldSkipSelfServiceMatchWhenIdleDisabled() throws Exception {
        when(accessSessionValidator.getInvalidReason("access-token")).thenReturn(null);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ResponseStatusFactory factory = this.statusFactory();
            springUtil.when(() -> SpringUtil.getBean(ResponseStatusFactory.class))
                .thenReturn(factory);
            springUtil.when(() -> SpringUtil.getBean(ObjectMapper.class))
                .thenReturn(new ObjectMapper());
            try (MockedStatic<TenantContextHolder> tenantHolder = mockStatic(
                TenantContextHolder.class)) {
                tenantHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(false);
                MockHttpServletResponse response = new MockHttpServletResponse();

                boolean flag = interceptor.preHandle(this.request("/user/profile/password"),
                    response, new Object());

                assertTrue(flag);
                // 未配置闲置失效时直接短路，不做放行路径匹配（setUp 默认策略 idle=0）
                saRouter.verify(() -> SaRouter.isMatchCurrURI(any(String[].class)), never());
            }
        }
    }

    @Test
    void shouldPreferClientIdFromTokenClaim() throws Exception {
        when(accessSessionValidator.getInvalidReason("access-token")).thenReturn(null);
        // 用户级 SaSession 里的客户端是 A 端（会被同账号的其它登录覆盖），令牌声明才是本端
        cachedContext.setClientId("client-a");
        stpUtil.when(() -> StpUtil.getExtra("access-token", "clientId")).thenReturn("client-b");
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ResponseStatusFactory factory = this.statusFactory();
            springUtil.when(() -> SpringUtil.getBean(ResponseStatusFactory.class))
                .thenReturn(factory);
            springUtil.when(() -> SpringUtil.getBean(ObjectMapper.class))
                .thenReturn(new ObjectMapper());
            try (MockedStatic<TenantContextHolder> tenantHolder = mockStatic(
                TenantContextHolder.class)) {
                tenantHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(false);

                boolean flag = interceptor.preHandle(this.request("/system/user"),
                    new MockHttpServletResponse(), new Object());

                assertTrue(flag);
                // 闲置策略与活跃时间都必须按令牌所属客户端取
                verify(clientPolicyCache).get("client-b");
                verify(sessionActivityService).touch(any(), eq("client-b"));
            }
        }
    }

    private ResponseStatusFactory statusFactory() {
        ResponseStatusFactory factory = mock(ResponseStatusFactory.class);
        when(factory.defaultSuccess()).thenReturn(new DefaultResponseStatus("0", "操作成功"));
        when(factory.defaultError()).thenReturn(new DefaultResponseStatus("1", "操作失败"));
        return factory;
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        request.setScheme("https");
        request.setServerName("admin.example");
        request.setServerPort(443);
        request.setRemoteAddr("127.0.0.1");
        SaManager.getSaTokenContext()
            .setContext(new SaRequestForServlet(request), new SaResponseForServlet(
                new MockHttpServletResponse()), new SaStorageForServlet(request));
        return request;
    }
}
