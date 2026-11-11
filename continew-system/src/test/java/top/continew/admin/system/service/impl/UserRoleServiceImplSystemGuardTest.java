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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.model.entity.UserRoleDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;

/**
 * 用户和角色关联变更的系统内置用户守卫测试。
 *
 * @author Charles7c
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplSystemGuardTest {

    private static final long SYSTEM_USER_ID = 1L;
    private static final long NORMAL_USER_ID = 2L;
    private static final long SYSTEM_ROLE_ID = 1L;
    private static final long NORMAL_ROLE_ID = 3L;

    @Mock
    private UserRoleMapper baseMapper;
    @Mock
    private RoleService roleService;
    @Mock
    private UserService userService;

    private UserRoleServiceImpl userRoleService;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        // 校验工具的校验器实例取自 Spring 上下文，单测环境以 mock 供给
        springUtilMock = mockStatic(SpringUtil.class);
        Validator validator = mock(Validator.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class)).thenReturn(validator);
        userRoleService = spy(new UserRoleServiceImpl(baseMapper));
        ReflectionTestUtils.setField(userRoleService, "roleService", roleService);
        ReflectionTestUtils.setField(userRoleService, "userService", userService);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldRejectDeletingAssociationOfSystemUser() {
        when(baseMapper.selectByIds(List.of(10L)))
            .thenReturn(List.of(new UserRoleDO(SYSTEM_USER_ID, NORMAL_ROLE_ID)));
        when(userService.listByIds(Set.of(SYSTEM_USER_ID)))
            .thenReturn(List.of(this.user(SYSTEM_USER_ID, true)));

        List<Long> associationIds = List.of(10L);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.deleteByIds(associationIds));

        assertEquals("[超级管理员] 是系统内置用户，不允许变更角色", exception.getMessage());
        verify(baseMapper, never()).deleteByIds(anyList());
    }

    @Test
    void shouldDeleteNormalAssociation() {
        when(baseMapper.selectByIds(List.of(10L)))
            .thenReturn(List.of(new UserRoleDO(NORMAL_USER_ID, NORMAL_ROLE_ID)));
        when(userService.listByIds(Set.of(NORMAL_USER_ID)))
            .thenReturn(List.of(this.user(NORMAL_USER_ID, false)));

        userRoleService.deleteByIds(List.of(10L));

        verify(baseMapper).deleteByIds(List.of(10L));
    }

    @Test
    void shouldRejectWholeBatchWhenAnyAssociationGuarded() {
        when(baseMapper.selectByIds(List.of(10L, 11L))).thenReturn(List
            .of(new UserRoleDO(NORMAL_USER_ID, NORMAL_ROLE_ID), new UserRoleDO(SYSTEM_USER_ID,
                NORMAL_ROLE_ID)));
        when(userService.listByIds(Set.of(NORMAL_USER_ID, SYSTEM_USER_ID)))
            .thenReturn(List.of(this.user(NORMAL_USER_ID, false), this.user(SYSTEM_USER_ID, true)));

        List<Long> associationIds = List.of(10L, 11L);
        assertThrows(BusinessException.class, () -> userRoleService.deleteByIds(associationIds));

        verify(baseMapper, never()).deleteByIds(anyList());
    }

    @Test
    void shouldKeepRejectingSystemUserRoleChange() {
        doReturn(List.of(NORMAL_ROLE_ID)).when(userRoleService)
            .listRoleIdByUserId(SYSTEM_USER_ID);
        when(userService.listByIds(Set.of(SYSTEM_USER_ID)))
            .thenReturn(List.of(this.user(SYSTEM_USER_ID, true)));

        List<Long> newRoleIds = List.of(NORMAL_ROLE_ID, 5L);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.assignRolesToUser(newRoleIds, SYSTEM_USER_ID));

        assertEquals("[超级管理员] 是系统内置用户，不允许变更角色", exception.getMessage());
        verify(baseMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldSkipGuardWhenReplayingUnchangedRoles() {
        doReturn(List.of(NORMAL_ROLE_ID)).when(userRoleService)
            .listRoleIdByUserId(SYSTEM_USER_ID);
        when(roleService.listByUserId(SYSTEM_USER_ID)).thenReturn(Set.of());

        boolean result = userRoleService
            .assignRolesToUser(List.of(NORMAL_ROLE_ID), SYSTEM_USER_ID);

        assertFalse(result);
        verify(userService, never()).listByIds(any());
        verify(baseMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldRejectAssigningRoleToSystemUserViaRoleSide() {
        when(userService.listByIds(Set.of(SYSTEM_USER_ID)))
            .thenReturn(List.of(this.user(SYSTEM_USER_ID, true)));

        List<Long> userIds = List.of(SYSTEM_USER_ID);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.assignRoleToUsers(NORMAL_ROLE_ID, userIds));

        assertEquals("[超级管理员] 是系统内置用户，不允许变更角色", exception.getMessage());
        verify(baseMapper, never()).insertBatch(anyList());
    }

    @Test
    void shouldRejectDeletingBySystemUserId() {
        when(userService.listByIds(Set.of(SYSTEM_USER_ID)))
            .thenReturn(List.of(this.user(SYSTEM_USER_ID, true)));

        List<Long> userIds = List.of(SYSTEM_USER_ID);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.deleteByUserIds(userIds));

        assertEquals("[超级管理员] 是系统内置用户，不允许变更角色", exception.getMessage());
        verify(baseMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldRejectSavingAssociationForSystemUser() {
        when(userService.listByIds(Set.of(SYSTEM_USER_ID)))
            .thenReturn(List.of(this.user(SYSTEM_USER_ID, true)));

        List<UserRoleDO> list = List.of(new UserRoleDO(SYSTEM_USER_ID, NORMAL_ROLE_ID));
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userRoleService.saveBatch(list));

        assertEquals("[超级管理员] 是系统内置用户，不允许变更角色", exception.getMessage());
        verify(baseMapper, never()).insert(anyList());
    }

    @Test
    void shouldAllowInitBypassForSystemUserRole() {
        when(baseMapper.insertBatch(anyList())).thenReturn(true);

        boolean result = userRoleService
            .assignRoleToUsersForInit(SYSTEM_ROLE_ID, List.of(SYSTEM_USER_ID));

        assertTrue(result);
        verify(userService, never()).listByIds(any());
        verify(baseMapper).insertBatch(List.of(new UserRoleDO(SYSTEM_USER_ID, SYSTEM_ROLE_ID)));
    }

    @Test
    void shouldKeepNormalAssignFlow() {
        doReturn(List.of(NORMAL_ROLE_ID)).when(userRoleService)
            .listRoleIdByUserId(NORMAL_USER_ID);
        when(userService.listByIds(Set.of(NORMAL_USER_ID)))
            .thenReturn(List.of(this.user(NORMAL_USER_ID, false)));
        when(roleService.listByUserId(NORMAL_USER_ID)).thenReturn(Set.of());
        this.mockRemoveChain();
        when(baseMapper.insertBatch(anyList())).thenReturn(true);

        boolean result = userRoleService
            .assignRolesToUser(List.of(5L), NORMAL_USER_ID);

        assertTrue(result);
        verify(baseMapper).lambdaUpdate();
        verify(baseMapper).insertBatch(List.of(new UserRoleDO(NORMAL_USER_ID, 5L)));
    }

    private UserDO user(Long id, boolean isSystem) {
        UserDO user = new UserDO();
        user.setId(id);
        user.setNickname(isSystem ? "超级管理员" : "测试员");
        user.setIsSystem(isSystem);
        return user;
    }

    @SuppressWarnings("unchecked")
    private void mockRemoveChain() {
        LambdaUpdateChainWrapper<UserRoleDO> chain = mock(LambdaUpdateChainWrapper.class);
        when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        when(chain.remove()).thenReturn(true);
        when(baseMapper.lambdaUpdate()).thenReturn(chain);
    }
}
