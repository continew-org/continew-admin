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

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
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
import top.continew.admin.auth.api.AccessSessionValidator;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * SaExtensionInterceptor 短路路径线程本地清理测试。
 *
 * <p>回归 C1：preHandle 返回 false 时 afterCompletion 不再回调，若不在短路前清除
 * UserContext 线程本地，池化线程会残留上一请求的上下文被下一请求复用。断言方式是
 * 短路后再次读取上下文：若已清除则重新走 {@link StpUtil#getSession()}（调用 2 次），
 * 未清除则命中线程本地（仅 1 次）。</p>
 */
class SaExtensionInterceptorTest {

    private AccessSessionValidator accessSessionValidator;
    private SaExtensionInterceptor interceptor;
    private MockedStatic<StpUtil> stpUtil;
    private UserContext cachedContext;

    @BeforeEach
    void setUp() {
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());
        accessSessionValidator = mock(AccessSessionValidator.class);
        interceptor = new SaExtensionInterceptor(handle -> {
        }, accessSessionValidator);
        stpUtil = mockStatic(StpUtil.class);
        stpUtil.when(StpUtil::isLogin).thenReturn(true);
        stpUtil.when(StpUtil::getTokenValue).thenReturn("access-token");
        cachedContext = new UserContext();
        SaSession saSession = mock(SaSession.class);
        when(saSession.getModel(eq(SaSession.USER), eq(UserContext.class))).thenReturn(
            cachedContext);
        stpUtil.when(StpUtil::getSession).thenReturn(saSession);
    }

    @AfterEach
    void tearDown() {
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
                stpUtil.verify(() -> StpUtil.getSession(), times(2));
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
                stpUtil.verify(() -> StpUtil.getSession(), times(2));
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
                stpUtil.verify(() -> StpUtil.getSession(), times(2));
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
