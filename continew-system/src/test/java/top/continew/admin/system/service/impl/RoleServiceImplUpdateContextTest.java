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

import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.service.RoleDeptService;
import top.continew.admin.system.service.RoleMenuService;
import top.continew.admin.system.service.UserRoleService;

import java.util.List;
import java.util.Set;

/**
 * 角色权限变更后同步在线用户上下文的测试。
 *
 * <p>{@code updateUserContext} 遍历的是目标用户的上下文，用
 * {@code UserContextHolder.setContext} 写入会把他人上下文留在当前请求线程上，同一请求后续步骤
 * 把它当作自己的上下文读取（跨用户串用）。同步他人会话数据不应触碰线程本地。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RoleServiceImplUpdateContextTest {

    private static final long ROLE_ID = 9L;
    private static final long CURRENT_USER_ID = 1L;
    private static final long TARGET_USER_ID = 2L;

    private UserRoleService userRoleService;
    private RoleServiceImpl roleService;
    private UserContext currentContext;

    @BeforeEach
    void setUp() {
        userRoleService = mock(UserRoleService.class);
        roleService =
            spy(new RoleServiceImpl(mock(RoleMenuService.class), mock(RoleDeptService.class)));
        ReflectionTestUtils.setField(roleService, "userRoleService", userRoleService);
        // 权限与角色集合由被测方法内部自取，此处只关注上下文写到哪里
        doReturn(Set.of()).when(roleService).listByUserId(anyLong());
        doReturn(Set.of()).when(roleService).listPermissionByUserId(anyLong());
        currentContext = context(CURRENT_USER_ID);
        UserContextHolder.setContext(currentContext, false);
    }

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
    void shouldSyncTargetUserSessionWithoutPollutingCurrentThread() {
        UserContext targetContext = context(TARGET_USER_ID);
        SaSession targetSession = sessionOf(targetContext);
        SaSession currentSession = sessionOf(currentContext);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getSessionByLoginId(TARGET_USER_ID, false))
                .thenReturn(targetSession);
            stpUtil.when(() -> StpUtil.getSessionByLoginId(TARGET_USER_ID))
                .thenReturn(targetSession);
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(CURRENT_USER_ID);
            stpUtil.when(StpUtil::getSession).thenReturn(currentSession);
            when(userRoleService.listUserIdByRoleId(ROLE_ID)).thenReturn(List.of(TARGET_USER_ID));

            roleService.updateUserContext(ROLE_ID);

            // 目标用户的会话被同步，权限变更对其在线会话生效
            verify(targetSession).set(SaSession.USER, targetContext);
            // 当前线程未被写入：读到的还是自己的上下文，且没有因归属不符而回源会话
            assertSame(currentContext, UserContextHolder.getContext());
            stpUtil.verify(StpUtil::getSession, never());
        }
    }

    @Test
    void shouldSkipOfflineTargetUser() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getSessionByLoginId(TARGET_USER_ID, false)).thenReturn(null);
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(CURRENT_USER_ID);
            when(userRoleService.listUserIdByRoleId(ROLE_ID)).thenReturn(List.of(TARGET_USER_ID));

            roleService.updateUserContext(ROLE_ID);

            // 目标用户不在线（无会话）时不写任何数据，也不影响当前线程
            stpUtil.verify(() -> StpUtil.getSessionByLoginId(TARGET_USER_ID), never());
            assertSame(currentContext, UserContextHolder.getContext());
        }
    }

    private static SaSession sessionOf(UserContext context) {
        SaSession session = mock(SaSession.class);
        when(session.getModel(SaSession.USER, UserContext.class)).thenReturn(context);
        return session;
    }
}
