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

package top.continew.admin.common.config.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.DataScopeEnum;
import top.continew.starter.extension.datapermission.enums.DataScope;
import top.continew.starter.extension.datapermission.model.RoleData;
import top.continew.starter.extension.datapermission.model.UserData;
import top.continew.starter.extension.datapermission.provider.DataPermissionUserDataProvider;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.util.Set;

/**
 * 数据权限用户数据供给测试。
 *
 * <p>覆盖过滤判定（普通用户过滤、超管/租户管理员不过滤）与用户数据映射
 * （用户 ID、部门 ID、角色数据权限枚举转换）。登录 ID 经 StpUtil 静态桩、
 * 租户配置经 SpringUtil 静态桩供给，不依赖 Spring 上下文。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class DefaultDataPermissionUserDataProviderTest {

    private static final long USER_ID = 9L;

    private final DataPermissionUserDataProvider provider =
        new DefaultDataPermissionUserDataProvider();

    private MockedStatic<StpUtil> stpUtilMock;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        stpUtilMock = mockStatic(StpUtil.class);
        stpUtilMock.when(StpUtil::getLoginIdDefaultNull).thenReturn(USER_ID);
        springUtilMock = mockStatic(SpringUtil.class);
        TenantExtensionProperties properties = new TenantExtensionProperties();
        properties.setDefaultTenantId(0L);
        springUtilMock.when(() -> SpringUtil.getBean(TenantExtensionProperties.class))
            .thenReturn(properties);
    }

    @AfterEach
    void tearDown() {
        stpUtilMock.close();
        springUtilMock.close();
        UserContextHolder.clearContext();
    }

    private void login(UserContext context) {
        UserContextHolder.setContext(context, false);
    }

    private static UserContext contextWithRoles(String... roleCodes) {
        UserContext context = new UserContext();
        context.setId(USER_ID);
        context.setDeptId(2L);
        context.setRoleCodes(Set.of(roleCodes));
        return context;
    }

    @Test
    void shouldFilterForRegularUser() {
        login(contextWithRoles("general"));

        assertTrue(provider.isFilter());
    }

    @Test
    void shouldNotFilterForSuperAdmin() {
        login(contextWithRoles("super_admin"));

        assertFalse(provider.isFilter());
    }

    @Test
    void shouldNotFilterForTenantAdminOnNonDefaultTenant() {
        try (MockedStatic<TenantContextHolder> tenantHolder =
            mockStatic(TenantContextHolder.class)) {
            tenantHolder.when(TenantContextHolder::getTenantId).thenReturn(1L);
            UserContext context = contextWithRoles("admin");
            login(context);

            assertFalse(provider.isFilter());
        }
    }

    @Test
    void shouldMapUserDataFromContext() {
        UserContext context = contextWithRoles("general");
        RoleContext role = new RoleContext();
        role.setId(3L);
        role.setCode("general");
        role.setDataScope(DataScopeEnum.DEPT_AND_CHILD);
        context.setRoles(Set.of(role));
        login(context);

        UserData userData = provider.getUserData();

        assertEquals(USER_ID, userData.getUserId());
        assertEquals(2L, userData.getDeptId());
        assertEquals(1, userData.getRoles().size());
        RoleData roleData = userData.getRoles().iterator().next();
        assertEquals(3L, roleData.getRoleId());
        assertEquals(DataScope.DEPT_AND_CHILD, roleData.getDataScope());
    }
}
