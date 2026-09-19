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

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.continew.admin.auth.service.RefreshAccessTokenIssuer;
import top.continew.admin.auth.service.RefreshTokenService;
import top.continew.admin.auth.exception.RefreshTokenException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** logout Cookie 安全边界测试。 */
class SessionControllerTest {

    private RefreshTokenService refreshTokenService;
    private SessionController controller;

    @BeforeEach
    void setUp() {
        refreshTokenService = mock(RefreshTokenService.class);
        controller =
            new SessionController(mock(RefreshAccessTokenIssuer.class), refreshTokenService);
    }

    @Test
    void shouldClearMalformedCookieForTrustedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RefreshTokenException malformed = RefreshTokenException.forbidden("Cookie 损坏");
        when(refreshTokenService.resolve(null, request)).thenThrow(malformed);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getLoginId(-1L)).thenReturn(1L);
            stpUtil.when(StpUtil::getTokenValue).thenReturn(null);

            assertThrows(RefreshTokenException.class,
                () -> controller.logout(null, request, response));
        }

        verify(refreshTokenService).validateCookieOrigin(request);
        verify(refreshTokenService).clearCookie(response);
    }

    @Test
    void shouldNotClearCookieForUntrustedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(RefreshTokenException.forbidden("请求来源不合法"))
            .when(refreshTokenService)
            .validateCookieOrigin(request);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getLoginId(-1L)).thenReturn(1L);
            stpUtil.when(StpUtil::getTokenValue).thenReturn(null);

            assertThrows(RefreshTokenException.class,
                () -> controller.logout(null, request, response));
        }

        verify(refreshTokenService, never()).resolve(any(), any());
        verify(refreshTokenService, never()).clearCookie(response);
    }

    @Test
    void shouldKeepCookieWhenSessionRevocationFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(refreshTokenService.resolve(null, request)).thenReturn("session.secret");
        doThrow(new IllegalStateException("Redis unavailable"))
            .when(refreshTokenService)
            .revokeCurrent("access-token", "session.secret");

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getLoginId(-1L)).thenReturn(1L);
            stpUtil.when(StpUtil::getTokenValue).thenReturn("access-token");

            assertThrows(IllegalStateException.class,
                () -> controller.logout(null, request, response));
        }

        verify(refreshTokenService, never()).clearCookie(response);
    }
}
