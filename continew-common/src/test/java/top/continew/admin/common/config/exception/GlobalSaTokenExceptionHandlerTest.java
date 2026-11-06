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

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.continew.starter.web.model.R;

/**
 * 全局 Sa-Token 异常处理器错误码契约测试。
 *
 * <p>锁定认证/鉴权失败的三类响应形态：未登录按踢出、被顶、过期三种类型区分文案且统一 401，
 * 权限与角色越权统一 403。文案与状态码由前端与会话层共同消费，属用户可见契约。
 * 全部为纯单元测试。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class GlobalSaTokenExceptionHandlerTest {

    private final GlobalSaTokenExceptionHandler handler = new GlobalSaTokenExceptionHandler();

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/test/any");
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturnKickOutMessageForKickOutType() {
        NotLoginException exception =
            new NotLoginException("token", "login", NotLoginException.KICK_OUT);

        R result = handler.handleNotLoginException(exception, request, response);

        assertEquals(401, response.getStatus());
        assertEquals("401", result.getCode());
        assertEquals("您已被踢下线", result.getMsg());
    }

    @Test
    void shouldReturnReplacedMessageForBeReplacedType() {
        NotLoginException exception =
            new NotLoginException("token", "login", NotLoginException.BE_REPLACED);

        R result = handler.handleNotLoginException(exception, request, response);

        assertEquals(401, response.getStatus());
        assertEquals("401", result.getCode());
        assertEquals("您已被顶下线", result.getMsg());
    }

    @Test
    void shouldReturnExpiredMessageForOtherTypes() {
        NotLoginException exception =
            new NotLoginException("token", "login", NotLoginException.NOT_TOKEN);

        R result = handler.handleNotLoginException(exception, request, response);

        assertEquals(401, response.getStatus());
        assertEquals("401", result.getCode());
        assertEquals("您的登录状态已过期，请重新登录", result.getMsg());
    }

    @Test
    void shouldReturnForbiddenForNotPermissionException() {
        R result =
            handler.handleNotPermissionException(new NotPermissionException("system:user:list",
                "login"), request, response);

        assertEquals(403, response.getStatus());
        assertEquals("403", result.getCode());
        assertEquals("没有访问权限，请联系管理员授权", result.getMsg());
    }

    @Test
    void shouldReturnForbiddenForNotRoleException() {
        R result = handler.handleNotRoleException(new NotRoleException("super_admin", "login"),
            request, response);

        assertEquals(403, response.getStatus());
        assertEquals("403", result.getCode());
        assertEquals("没有访问权限，请联系管理员授权", result.getMsg());
    }
}
