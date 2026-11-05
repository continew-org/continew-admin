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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.req.user.UserReq;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserPasswordHistoryService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserSocialService;
import top.continew.starter.storage.core.FileStorageService;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;

/**
 * 用户信息变更时会话快照同步时机的测试。
 *
 * <p>回归：{@code update} 在事务内同步目标用户的会话快照，后续数据库操作异常回滚时，已写入
 * Redis 的快照不随之回滚（角色授权回滚后快照多出角色、撤销后快照残留旧角色）。快照同步必须
 * 等事务提交后再执行。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class UserServiceImplUpdateContextTimingTest {

    private static final long CURRENT_USER_ID = 1L;
    private static final long TARGET_USER_ID = 2L;

    private UserRoleService userRoleService;
    private RoleService roleService;
    private UserMapper userMapper;
    private UserServiceImpl userService;
    private UserContext targetContext;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        // 校验工具的校验器实例取自 Spring 上下文，单测环境以 mock 供给
        springUtilMock = mockStatic(SpringUtil.class);
        Validator validator = mock(Validator.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class)).thenReturn(validator);
        userRoleService = mock(UserRoleService.class);
        roleService = mock(RoleService.class);
        userService = new UserServiceImpl(mock(PasswordEncoder.class),
            mock(UserPasswordHistoryService.class),
            mock(UserSocialService.class),
            userRoleService,
            mock(OptionService.class),
            roleService,
            mock(OnlineUserService.class),
            mock(FileService.class),
            mock(FileStorageService.class));
        userMapper = mock(UserMapper.class);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        when(roleService.listByUserId(anyLong())).thenReturn(Set.of());
        when(roleService.listPermissionByUserId(anyLong())).thenReturn(Set.of());
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
    private static LambdaQueryChainWrapper<UserDO> mockChain(UserDO oldUser) {
        LambdaQueryChainWrapper<UserDO> chain = mock(LambdaQueryChainWrapper.class);
        when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        when(chain.ne(anyBoolean(), any(SFunction.class), any())).thenReturn(chain);
        when(chain.exists()).thenReturn(false);
        when(chain.one()).thenReturn(oldUser);
        return chain;
    }

    private static UserContext context(Long id) {
        UserContext context = new UserContext();
        context.setId(id);
        return context;
    }

    private MockedStatic<StpUtil> stubOnlineTargetUser() {
        MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
        SaSession currentSession = mock(SaSession.class);
        when(currentSession.getModel(SaSession.USER, UserContext.class))
            .thenReturn(context(CURRENT_USER_ID));
        SaSession targetSession = mock(SaSession.class);
        when(targetSession.getModel(SaSession.USER, UserContext.class)).thenReturn(targetContext);
        stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(CURRENT_USER_ID);
        stpUtil.when(StpUtil::getSession).thenReturn(currentSession);
        stpUtil.when(() -> StpUtil.getSessionByLoginId(TARGET_USER_ID, false))
            .thenReturn(targetSession);
        return stpUtil;
    }

    @Test
    void shouldNotWriteSessionSnapshotBeforeCommit() {
        UserDO oldUser = new UserDO();
        oldUser.setId(TARGET_USER_ID);
        oldUser.setIsSystem(false);
        LambdaQueryChainWrapper<UserDO> chain = mockChain(oldUser);
        when(userMapper.lambdaQuery()).thenReturn(chain);
        when(userMapper.updateById(any(UserDO.class))).thenReturn(1);
        when(userRoleService.assignRolesToUser(any(), anyLong())).thenReturn(true);
        UserReq req = new UserReq();
        req.setUsername("user2");
        req.setStatus(DisEnableStatusEnum.ENABLE);
        req.setRoleIds(List.of(1L));
        TransactionSynchronizationManager.initSynchronization();
        try (MockedStatic<StpUtil> stpUtil = stubOnlineTargetUser()) {
            UserContextHolder.setContext(context(CURRENT_USER_ID), false);

            userService.update(req, TARGET_USER_ID);

            // 事务未提交（后续数据库操作异常回滚）时不写任何会话快照
            stpUtil.verify(() -> StpUtil.getSessionByLoginId(TARGET_USER_ID, false), never());
        }
    }

    @Test
    void shouldWriteSessionSnapshotAfterCommit() {
        UserDO oldUser = new UserDO();
        oldUser.setId(TARGET_USER_ID);
        oldUser.setIsSystem(false);
        LambdaQueryChainWrapper<UserDO> chain = mockChain(oldUser);
        when(userMapper.lambdaQuery()).thenReturn(chain);
        when(userMapper.updateById(any(UserDO.class))).thenReturn(1);
        when(userRoleService.assignRolesToUser(any(), anyLong())).thenReturn(true);
        UserReq req = new UserReq();
        req.setUsername("user2");
        req.setStatus(DisEnableStatusEnum.ENABLE);
        req.setRoleIds(List.of(1L));
        TransactionSynchronizationManager.initSynchronization();
        try (MockedStatic<StpUtil> stpUtil = stubOnlineTargetUser()) {
            UserContextHolder.setContext(context(CURRENT_USER_ID), false);

            userService.update(req, TARGET_USER_ID);

            TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

            // 提交后写入快照，目标在线用户的角色与权限随会话刷新
            assertEquals(Set.of(), targetContext.getRoles());
            assertEquals(Set.of(), targetContext.getPermissions());
        }
    }
}
