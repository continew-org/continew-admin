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

package top.continew.admin.common.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.util.List;

/**
 * 角色编码枚举的超级权限判定测试。
 *
 * <p>覆盖多租户禁用/默认租户/非默认租户三种场景下的超级角色编码列表，以及
 * {@code isSuperRoleCode} 的判定口径。租户配置经 SpringUtil 静态桩供给，
 * 不依赖 Spring 上下文。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RoleCodeEnumTest {

    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldReturnSuperAdminOnlyWhenTenantDisabled() {
        try (MockedStatic<TenantContextHolder> tenantHolder =
            mockStatic(TenantContextHolder.class)) {
            tenantHolder.when(TenantContextHolder::isTenantDisabled).thenReturn(true);

            assertEquals(List.of("super_admin"), RoleCodeEnum.getSuperRoleCodes());
            assertTrue(RoleCodeEnum.isSuperRoleCode("super_admin"));
            assertFalse(RoleCodeEnum.isSuperRoleCode("admin"));
        }
    }

    @Test
    void shouldReturnSuperAdminOnlyWhenDefaultTenant() {
        try (MockedStatic<TenantContextHolder> tenantHolder =
            mockStatic(TenantContextHolder.class)) {
            tenantHolder.when(TenantContextHolder::isTenantDisabled).thenReturn(false);
            tenantHolder.when(TenantContextHolder::getTenantId).thenReturn(0L);
            TenantExtensionProperties properties = new TenantExtensionProperties();
            properties.setDefaultTenantId(0L);
            springUtilMock.when(() -> SpringUtil.getBean(TenantExtensionProperties.class))
                .thenReturn(properties);

            assertEquals(List.of("super_admin"), RoleCodeEnum.getSuperRoleCodes());
        }
    }

    @Test
    void shouldIncludeTenantAdminWhenNonDefaultTenant() {
        try (MockedStatic<TenantContextHolder> tenantHolder =
            mockStatic(TenantContextHolder.class)) {
            tenantHolder.when(TenantContextHolder::isTenantDisabled).thenReturn(false);
            tenantHolder.when(TenantContextHolder::getTenantId).thenReturn(1L);
            TenantExtensionProperties properties = new TenantExtensionProperties();
            properties.setDefaultTenantId(0L);
            springUtilMock.when(() -> SpringUtil.getBean(TenantExtensionProperties.class))
                .thenReturn(properties);

            assertEquals(List.of("super_admin", "admin"), RoleCodeEnum.getSuperRoleCodes());
            assertTrue(RoleCodeEnum.isSuperRoleCode("admin"));
        }
    }
}
