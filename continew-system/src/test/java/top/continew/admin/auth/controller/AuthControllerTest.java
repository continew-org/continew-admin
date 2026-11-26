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

package top.continew.admin.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import top.continew.admin.auth.model.resp.UserInfoResp;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.model.resp.user.UserDetailResp;
import top.continew.admin.system.service.UserService;
import top.continew.starter.auth.refresh.token.api.AuthSessionConstants;
import top.continew.starter.auth.refresh.token.model.RefreshSession;
import top.continew.starter.auth.refresh.token.model.SessionView;
import top.continew.starter.auth.refresh.token.service.SessionQueryService;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * 当前登录用户会话信息测试
 *
 * @author Charles7c
 * @since 5.0.0
 */
class AuthControllerTest {

    private final SessionQueryService sessions = mock(SessionQueryService.class);
    private final UserService users = mock(UserService.class);
    private final DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
    private final AuthController controller = new AuthController(null, users, beans
        .getBeanProvider(SessionQueryService.class), null);

    @Test
    void shouldReadOnlyTheCurrentSessionAndKeepItsOriginalLoginTime() {
        LocalDateTime loginTime = LocalDateTime.of(2026, 1, 10, 11, 12, 13);
        RefreshSession session = new RefreshSession();
        session.setCreatedAt(
            loginTime.atZone(GlobalConstants.DEFAULT_ZONE_ID).toInstant().toEpochMilli());
        session.setLastRefreshAt(session.getCreatedAt() + 60000);
        session.setIp("2001:db8::1234");
        when(sessions.getSession("current")).thenReturn(SessionView.from(session));

        UserInfoResp info = readWithSessionService("current");

        assertEquals(session.getIp(), info.getLoginIp());
        assertEquals(loginTime, info.getLoginTime());
        verify(sessions).getSession("current");
        verifyNoMoreInteractions(sessions);
    }

    @Test
    void shouldKeepIpAndDropLoginTimeWhenSessionHasNoCreationTime() {
        RefreshSession session = new RefreshSession();
        session.setCreatedAt(0);
        session.setIp("192.0.2.9");
        when(sessions.getSession("current")).thenReturn(SessionView.from(session));

        UserInfoResp info = readWithSessionService("current");

        assertEquals("192.0.2.9", info.getLoginIp());
        assertNull(info.getLoginTime());
    }

    @Test
    void shouldKeepProfileWhenSessionIsMissing() {
        UserInfoResp info = readWithSessionService("missing");
        assertEquals("user", info.getUsername());
        assertNull(info.getLoginIp());
        assertNull(info.getLoginTime());
        verify(sessions).getSession("missing");
        verifyNoMoreInteractions(sessions);
    }

    @Test
    void shouldNotQueryOtherSessionsWhenTokenHasNoSessionId() {
        UserInfoResp info = readWithSessionService(null);
        assertNull(info.getLoginIp());
        assertNull(info.getLoginTime());
        verifyNoInteractions(sessions);
    }

    @Test
    void shouldKeepProfileWhenSessionQueryServiceIsNotRegistered() {
        UserInfoResp info = readUserInfo("current");
        assertEquals("user", info.getUsername());
        assertNull(info.getLoginIp());
        assertNull(info.getLoginTime());
    }

    @Test
    void shouldKeepProfileWhenSessionQueryFails() {
        when(sessions.getSession("current"))
            .thenThrow(new IllegalStateException("Session store unavailable"));

        UserInfoResp info = readWithSessionService("current");

        assertEquals("user", info.getUsername());
        assertNull(info.getLoginIp());
        assertNull(info.getLoginTime());
        verify(sessions).getSession("current");
    }

    @Test
    void shouldKeepProfileWhenReadingSessionIdFails() {
        beans.registerSingleton("sessionQueryService", sessions);
        UserInfoResp info = readUserInfoWithSessionIdSupplier(() -> {
            throw new IllegalStateException("Token store unavailable");
        });

        assertEquals("user", info.getUsername());
        assertNull(info.getLoginIp());
        assertNull(info.getLoginTime());
        verifyNoInteractions(sessions);
    }

    private UserInfoResp readWithSessionService(String sessionId) {
        beans.registerSingleton("sessionQueryService", sessions);
        return readUserInfo(sessionId);
    }

    private UserInfoResp readUserInfo(String sessionId) {
        return readUserInfoWithSessionIdSupplier(() -> sessionId);
    }

    private UserInfoResp readUserInfoWithSessionIdSupplier(Supplier<Object> sessionId) {
        UserContext context = new UserContext();
        context.setId(1L);
        UserDetailResp profile = new UserDetailResp();
        profile.setUsername("user");
        when(users.get(1L)).thenReturn(profile);
        try (MockedStatic<UserContextHolder> holder = mockStatic(UserContextHolder.class);
            MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            holder.when(UserContextHolder::getContext).thenReturn(context);
            stp.when(StpUtil::getTokenValue).thenReturn("access");
            stp.when(() -> StpUtil.getExtra("access", AuthSessionConstants.SESSION_ID_CLAIM))
                .thenAnswer(invocation -> sessionId.get());
            return controller.getUserInfo();
        }
    }
}
