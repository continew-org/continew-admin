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

package top.continew.admin.common.config.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.continew.starter.auth.refresh.token.exception.RefreshTokenException;
import top.continew.starter.web.model.R;

/**
 * Refresh Token 协议异常处理器测试。
 *
 * <p>覆盖认证会话协议异常到统一响应的转换：HTTP 状态写入响应、响应体携带
 * 状态码与错误信息。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RefreshTokenExceptionHandlerTest {

    private final RefreshTokenExceptionHandler handler = new RefreshTokenExceptionHandler();

    @Test
    void shouldWriteStatusAndFailResponse() {
        RefreshTokenException e = RefreshTokenException.unauthorized("令牌已失效");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();

        R result = handler.handle(e, request, response);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals("401", result.getCode());
        assertEquals("令牌已失效", result.getMsg());
    }

    @Test
    void shouldHandleForbidden() {
        RefreshTokenException e = RefreshTokenException.forbidden("无权操作");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/session");
        MockHttpServletResponse response = new MockHttpServletResponse();

        R result = handler.handle(e, request, response);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("403", result.getCode());
        assertEquals("无权操作", result.getMsg());
    }
}
