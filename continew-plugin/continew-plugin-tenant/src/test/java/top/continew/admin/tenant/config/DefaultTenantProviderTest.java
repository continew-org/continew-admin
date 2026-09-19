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

package top.continew.admin.tenant.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.extension.tenant.context.TenantContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 默认租户提供者测试：验证刷新/退出时请求入口忽略残留租户头，而业务代码显式切换的
 * 会话租户不被覆盖。
 *
 * @author luoqiz
 * @since 4.2.0
 */
class DefaultTenantProviderTest {

    private TenantService tenantService;
    private DefaultTenantProvider provider;

    @BeforeEach
    void setUp() {
        TenantExtensionProperties properties = new TenantExtensionProperties();
        properties.setDefaultTenantId(0L);
        tenantService = mock(TenantService.class);
        provider = new DefaultTenantProvider(properties, tenantService);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldIgnoreTenantHeaderAtRequestEntryOnRefresh() {
        // 请求入口（verify=true）：/auth/refresh 必须忽略前端残留的 X-Tenant-Id 请求头
        setRequest("/api/auth/refresh", "2");
        TenantContext context = provider.getByTenantId("2", true);
        assertEquals(0L, context.getTenantId());
        verify(tenantService, never()).checkStatus(any());
    }

    @Test
    void shouldIgnoreTenantHeaderAtRequestEntryOnLogout() {
        setRequest("/api/auth/logout", "2");
        TenantContext context = provider.getByTenantId("2", true);
        assertEquals(0L, context.getTenantId());
        verify(tenantService, never()).checkStatus(any());
    }

    @Test
    void shouldKeepExplicitTenantOnRefreshForBusinessSwitch() {
        // 业务层显式切换（verify=false）：TenantUtils.execute 传入的会话租户不得被覆盖
        setRequest("/api/auth/refresh", "2");
        TenantContext context = provider.getByTenantId("2", false);
        assertEquals(2L, context.getTenantId());
        verify(tenantService, never()).checkStatus(any());
    }

    @Test
    void shouldResolveTenantFromHeaderForNormalRequest() {
        setRequest("/api/system/user/page", "2");
        TenantContext context = provider.getByTenantId("2", true);
        assertEquals(2L, context.getTenantId());
        verify(tenantService).checkStatus(2L);
    }

    @Test
    void shouldReturnDefaultTenantForDefaultTenantId() {
        setRequest("/api/system/user/page", "0");
        TenantContext context = provider.getByTenantId("0", true);
        assertEquals(0L, context.getTenantId());
        verify(tenantService, never()).checkStatus(any());
    }

    private void setRequest(String uri, String tenantId) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        if (tenantId != null) {
            request.addHeader("X-Tenant-Id", tenantId);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
