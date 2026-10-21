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

package top.continew.admin.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.system.constant.SystemConstants;
import top.continew.admin.system.model.req.user.UserReq;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserPasswordHistoryService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserSocialService;
import top.continew.starter.storage.core.FileStorageService;

import java.util.Collections;
import java.util.List;

/**
 * 用户角色分配权限测试
 *
 * @author Charles7c
 * @since 5.0.0
 */
class UserServiceImplTest {

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
            mock(PasswordEncoder.class),
            mock(UserPasswordHistoryService.class),
            mock(UserSocialService.class),
            mock(UserRoleService.class),
            mock(OptionService.class),
            mock(RoleService.class),
            mock(OnlineUserService.class),
            mock(FileService.class),
            mock(FileStorageService.class));
    }

    @Test
    void shouldCheckPermissionWhenRolesChanged() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            userService.checkAssignRolePermission(List.of(2L), List.of(1L));

            stpUtil
                .verify(() -> StpUtil.checkPermission(SystemConstants.USER_UPDATE_ROLE_PERMISSION));
        }
    }

    @Test
    void shouldCheckPermissionWhenCreatingUserWithRoles() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            userService.checkAssignRolePermission(List.of(2L), Collections.emptyList());

            stpUtil
                .verify(() -> StpUtil.checkPermission(SystemConstants.USER_UPDATE_ROLE_PERMISSION));
        }
    }

    @Test
    void shouldNotCheckPermissionWhenRolesUnchanged() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            userService.checkAssignRolePermission(List.of(2L, 1L), List.of(1L, 2L));

            stpUtil.verifyNoInteractions();
        }
    }

    @Test
    void shouldRejectRoleChangeWithoutPermission() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.checkPermission(SystemConstants.USER_UPDATE_ROLE_PERMISSION))
                .thenThrow(new NotPermissionException(SystemConstants.USER_UPDATE_ROLE_PERMISSION));
            List<Long> newRoleIds = List.of(2L);
            List<Long> oldRoleIds = List.of(1L);

            assertThrows(NotPermissionException.class,
                () -> userService.checkAssignRolePermission(newRoleIds, oldRoleIds));
        }
    }

    @Test
    void shouldRejectCreateUserWithRolesWithoutPermission() {
        UserReq req = new UserReq();
        req.setRoleIds(List.of(2L));
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.checkPermission(SystemConstants.USER_UPDATE_ROLE_PERMISSION))
                .thenThrow(new NotPermissionException(SystemConstants.USER_UPDATE_ROLE_PERMISSION));

            assertThrows(NotPermissionException.class, () -> userService.beforeCreate(req));
        }
    }
}
