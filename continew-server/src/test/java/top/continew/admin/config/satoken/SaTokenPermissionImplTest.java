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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.RoleCodeEnum;

import java.util.List;
import java.util.Set;

/**
 * Sa-Token 权限认证实现测试。
 *
 * <p>Sa-Token 判权完全依赖 {@code getPermissionList}/{@code getRoleList} 返回的列表，
 * 测试验证列表与用户上下文的一致性：权限码透传、超管角色码透传、无权限返回空列表，
 * 以及返回列表为防御性拷贝（调用方修改不得污染上下文）。全部为纯单元测试。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class SaTokenPermissionImplTest {

    private final SaTokenPermissionImpl permissionImpl = new SaTokenPermissionImpl();

    @AfterEach
    void tearDown() {
        UserContextHolder.clearContext();
    }

    private static UserContext context(Long id) {
        UserContext context = new UserContext();
        context.setId(id);
        return context;
    }

    @Test
    void shouldReturnPermissionCodesFromContext() {
        UserContext context = context(1L);
        context.setPermissions(Set.of("system:user:list", "system:user:create"));
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(1L);
            UserContextHolder.setContext(context, false);

            List<String> permissions = permissionImpl.getPermissionList(1L, "login");

            assertEquals(2, permissions.size());
            assertTrue(permissions.contains("system:user:list"));
            assertTrue(permissions.contains("system:user:create"));
        }
    }

    @Test
    void shouldReturnSuperAdminRoleCodeFromContext() {
        UserContext context = context(1L);
        context.setRoleCodes(Set.of(RoleCodeEnum.SUPER_ADMIN.getCode(), "common"));
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(1L);
            UserContextHolder.setContext(context, false);

            List<String> roles = permissionImpl.getRoleList(1L, "login");

            assertEquals(2, roles.size());
            assertTrue(roles.contains(RoleCodeEnum.SUPER_ADMIN.getCode()));
        }
    }

    @Test
    void shouldReturnEmptyListWhenNoPermissions() {
        UserContext context = context(1L);
        context.setPermissions(Set.of());
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(1L);
            UserContextHolder.setContext(context, false);

            assertTrue(permissionImpl.getPermissionList(1L, "login").isEmpty());
        }
    }

    @Test
    void shouldReturnDefensiveCopyOfPermissions() {
        UserContext context = context(1L);
        context.setPermissions(Set.of("system:user:list"));
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(1L);
            UserContextHolder.setContext(context, false);

            List<String> permissions = permissionImpl.getPermissionList(1L, "login");
            permissions.clear();

            // 直接清空返回列表：实现返回独立拷贝时上下文不受影响，返回共享视图时此断言失败
            assertEquals(Set.of("system:user:list"), context.getPermissions());
        }
    }

    @Test
    void shouldReturnDefensiveCopyOfRoles() {
        UserContext context = context(1L);
        context.setRoleCodes(Set.of("common"));
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(1L);
            UserContextHolder.setContext(context, false);

            List<String> roles = permissionImpl.getRoleList(1L, "login");
            roles.clear();

            assertEquals(Set.of("common"), context.getRoleCodes());
        }
    }
}
