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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.mapper.RoleMapper;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.req.RolePermissionUpdateReq;
import top.continew.admin.system.service.RoleDeptService;
import top.continew.admin.system.service.RoleMenuService;
import top.continew.admin.system.service.UserRoleService;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;

/**
 * 角色功能权限变更时会话快照同步时机的测试。
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RoleServiceImplUpdatePermissionTimingTest {

    private static final long ROLE_ID = 9L;
    private static final long TARGET_USER_ID = 2L;

    private UserRoleService userRoleService;
    private RoleMenuService roleMenuService;
    private RoleMapper roleMapper;
    private RoleServiceImpl roleService;
    private UserContext targetContext;
    private SaSession targetSession;
    private RolePermissionUpdateReq req;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        // 校验工具的校验器实例取自 Spring 上下文，单测环境以 mock 供给
        springUtilMock = mockStatic(SpringUtil.class);
        Validator validator = mock(Validator.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class)).thenReturn(validator);
        userRoleService = mock(UserRoleService.class);
        roleMenuService = mock(RoleMenuService.class);
        roleService = spy(new RoleServiceImpl(roleMenuService, mock(RoleDeptService.class)));
        ReflectionTestUtils.setField(roleService, "userRoleService", userRoleService);
        roleMapper = mock(RoleMapper.class);
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
        // 父类 getById 经实体类缓存推导实体类型，mock Mapper 缺少代理元数据，预置绕开
        ReflectionTestUtils.setField(roleService, "entityClass", RoleDO.class);
        // 权限与角色集合由被测方法内部自取，此处只关注会话快照写入的时机
        doReturn(Set.of()).when(roleService).listByUserId(anyLong());
        doReturn(Set.of()).when(roleService).listPermissionByUserId(anyLong());
        RoleDO role = new RoleDO();
        role.setId(ROLE_ID);
        role.setName("测试角色");
        role.setIsSystem(false);
        // super.getById 为显式父类调用，无法经代理拦截，由 baseMapper 兜住
        when(roleMapper.selectById(ROLE_ID)).thenReturn(role);
        LambdaUpdateChainWrapper<RoleDO> updateChain = mockChain();
        when(roleMapper.lambdaUpdate()).thenReturn(updateChain);
        req = new RolePermissionUpdateReq();
        req.setMenuIds(List.of(1L, 2L));
        req.setMenuCheckStrictly(Boolean.TRUE);
        targetContext = context(TARGET_USER_ID);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        UserContextHolder.clearContext();
        springUtilMock.close();
    }

    @SuppressWarnings("unchecked")
    private static LambdaUpdateChainWrapper<RoleDO> mockChain() {
        LambdaUpdateChainWrapper<RoleDO> chain = mock(LambdaUpdateChainWrapper.class);
        when(chain.set(any(SFunction.class), any())).thenReturn(chain);
        when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        when(chain.update()).thenReturn(true);
        return chain;
    }

    private MockedStatic<StpUtil> stubOnlineTargetUser() {
        MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
        targetSession = mock(SaSession.class);
        when(targetSession.getModel(SaSession.USER, UserContext.class)).thenReturn(targetContext);
        stpUtil.when(() -> StpUtil.getSessionByLoginId(TARGET_USER_ID, false))
            .thenReturn(targetSession);
        when(userRoleService.listUserIdByRoleId(ROLE_ID)).thenReturn(List.of(TARGET_USER_ID));
        return stpUtil;
    }

    @Test
    void shouldNotWriteSessionSnapshotBeforeCommit() {
        when(roleMenuService.add(req.getMenuIds(), ROLE_ID)).thenReturn(true);
        TransactionSynchronizationManager.initSynchronization();
        try (MockedStatic<StpUtil> stpUtil = stubOnlineTargetUser()) {
            roleService.updatePermission(ROLE_ID, req);

            // 事务未提交时不写会话快照
            stpUtil.verify(() -> StpUtil.getSessionByLoginId(TARGET_USER_ID, false), never());
        }
    }

    @Test
    void shouldWriteSessionSnapshotAfterCommit() {
        when(roleMenuService.add(req.getMenuIds(), ROLE_ID)).thenReturn(true);
        TransactionSynchronizationManager.initSynchronization();
        try (MockedStatic<StpUtil> stpUtil = stubOnlineTargetUser()) {
            roleService.updatePermission(ROLE_ID, req);

            TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

            // 提交后写入快照，在线用户的角色与权限随会话刷新
            verify(targetSession).set(SaSession.USER, targetContext);
            assertEquals(Set.of(), targetContext.getRoles());
            assertEquals(Set.of(), targetContext.getPermissions());
        }
    }

    @Test
    void shouldNotRegisterSyncWhenMenusUnchanged() {
        when(roleMenuService.add(req.getMenuIds(), ROLE_ID)).thenReturn(false);
        TransactionSynchronizationManager.initSynchronization();

        roleService.updatePermission(ROLE_ID, req);

        // 功能权限无变更时不注册同步，事务同步表保持为空
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
    }

    private static UserContext context(Long id) {
        UserContext context = new UserContext();
        context.setId(id);
        return context;
    }
}
