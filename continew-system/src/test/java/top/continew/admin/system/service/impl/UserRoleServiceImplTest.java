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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.List;

/**
 * 用户角色分配安全测试
 *
 * @author Charles7c
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    private static ApplicationContext originalApplicationContext;
    private static StaticApplicationContext applicationContext;
    private static ValidatorFactory validatorFactory;

    @Mock
    private UserRoleMapper baseMapper;
    @Mock
    private RoleService roleService;
    @Mock
    private UserService userService;

    private UserRoleServiceImpl userRoleService;

    @BeforeAll
    static void setUpSpringUtil() {
        originalApplicationContext = SpringUtil.getApplicationContext();
        validatorFactory = Validation.buildDefaultValidatorFactory();
        applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory()
            .registerSingleton("validator", validatorFactory.getValidator());
        applicationContext.refresh();
        new SpringUtil().setApplicationContext(applicationContext);
    }

    @AfterAll
    static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
        applicationContext.close();
        validatorFactory.close();
    }

    @BeforeEach
    void setUp() {
        userRoleService = new UserRoleServiceImpl(baseMapper);
        ReflectionTestUtils.setField(userRoleService, "roleService", roleService);
        ReflectionTestUtils.setField(userRoleService, "userService", userService);
    }

    @Test
    void shouldRejectAssigningSuperAdminRole() {
        this.mockUser(false);
        List<Long> roleIds = List.of(1L);
        when(roleService.listByIds(roleIds))
            .thenReturn(List.of(this.mockRole(RoleCodeEnum.SUPER_ADMIN.getCode())));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.assignRolesToUser(roleIds, 2L));

        assertEquals("不允许分配超级管理员角色", exception.getMessage());
    }

    @Test
    void shouldRejectAssigningTenantAdminRole() {
        this.mockUser(false);
        List<Long> roleIds = List.of(2L);
        when(roleService.listByIds(roleIds))
            .thenReturn(List.of(this.mockRole(RoleCodeEnum.TENANT_ADMIN.getCode())));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.assignRolesToUser(roleIds, 2L));

        assertEquals("不允许分配系统管理员角色", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateRoleIds() {
        this.mockUser(false);
        List<Long> roleIds = List.of(2L, 2L);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.assignRolesToUser(roleIds, 2L));

        assertEquals("角色 ID 重复", exception.getMessage());
    }

    @Test
    void shouldRejectUnknownRole() {
        this.mockUser(false);
        List<Long> roleIds = List.of(999L);
        when(roleService.listByIds(roleIds)).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.assignRolesToUser(roleIds, 2L));

        assertEquals("角色不存在或无权限", exception.getMessage());
    }

    private void mockUser(boolean isSystem) {
        UserDO user = new UserDO();
        user.setIsSystem(isSystem);
        when(userService.getById(2L)).thenReturn(user);
    }

    private RoleDO mockRole(String code) {
        RoleDO role = new RoleDO();
        role.setCode(code);
        return role;
    }
}
