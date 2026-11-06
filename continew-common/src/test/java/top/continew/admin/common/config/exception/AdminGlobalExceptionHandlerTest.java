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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import tools.jackson.databind.exc.InvalidFormatException;
import top.continew.admin.common.exception.PasswordExpiredException;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.exception.BaseException;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.web.model.R;

/**
 * 全局异常处理器错误码契约测试。
 *
 * <p>直接调用各 handler 方法，锁定错误码与文案的对应关系（前端按 code 分流，文案是用户可见
 * 契约）。与 {@link GlobalExceptionHandlerTest} 的 MockMvc 场景互补：那里验证处理器装配
 * 与兜底归属，本类逐分支锁定码值。全部为纯单元测试。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class AdminGlobalExceptionHandlerTest {

    private final AdminGlobalExceptionHandler handler = new AdminGlobalExceptionHandler();

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/test/any");
    }

    @Test
    void shouldReturn400WithMessageForBadRequestException() {
        R result = handler.handleBadRequestException(new BadRequestException("客户端不存在"),
            request);

        assertEquals("400", result.getCode());
        assertEquals("客户端不存在", result.getMsg());
    }

    @Test
    void shouldReturn500WithMessageForBusinessException() {
        R result = handler.handleBusinessException(new BusinessException("库存不足"), request);

        assertEquals("500", result.getCode());
        assertEquals("库存不足", result.getMsg());
    }

    @Test
    void shouldReturn500WithMessageForBaseException() {
        R result = handler.handleBaseException(new BaseException("资源不存在"), request);

        assertEquals("500", result.getCode());
        assertEquals("资源不存在", result.getMsg());
    }

    @Test
    void shouldReturn428WithMessageForPasswordExpiredException() {
        R result = handler.handlePasswordExpiredException(new PasswordExpiredException(), request);

        assertEquals(PasswordExpiredException.CODE, result.getCode());
        assertEquals("密码已过期，请先修改密码", result.getMsg());
    }

    @Test
    void shouldReportMissingParameterName() {
        R result = handler
            .handleMissingServletRequestParameterException(
                new MissingServletRequestParameterException(
                    "id", "long"),
                request);

        assertEquals("400", result.getCode());
        assertEquals("参数 'id' 缺失", result.getMsg());
    }

    @Test
    void shouldReportTypeMismatchParameterName() {
        MethodArgumentTypeMismatchException exception =
            new MethodArgumentTypeMismatchException("abc",
                Long.class, "id", null, null);
        R result = handler.handleMethodArgumentTypeMismatchException(exception, request);

        assertEquals("400", result.getCode());
        assertEquals("参数 'id' 类型不匹配", result.getMsg());
    }

    @Test
    void shouldReturnFirstFieldErrorMessageForBindException() {
        BindException bindException = new BindException(new Object(), "req");
        bindException.addError(new FieldError("req", "username", "用户名不能为空"));
        bindException.addError(new FieldError("req", "nickname", "昵称长度非法"));

        R result = handler.handleBindException(bindException, request);

        assertEquals("400", result.getCode());
        assertEquals("用户名不能为空", result.getMsg());
    }

    @Test
    void shouldReturnEmptyMessageWhenBindExceptionWithoutFieldError() {
        BindException bindException = new BindException(new Object(), "req");

        R result = handler.handleBindException(bindException, request);

        assertEquals("400", result.getCode());
        assertEquals("", result.getMsg());
    }

    @Test
    void shouldReportValueForInvalidFormatCause() {
        InvalidFormatException cause = new InvalidFormatException(null, "格式错误", "abc", Long.class);
        R result = handler.handleHttpMessageNotReadableException(this
            .unreadable("bad body", cause), request);

        assertEquals("400", result.getCode());
        assertEquals("参数 'abc' 类型不匹配", result.getMsg());
    }

    @Test
    void shouldReturnFallbackMessageWhenBodyUnreadable() {
        R result = handler.handleHttpMessageNotReadableException(this
            .unreadable("bad body", null), request);

        assertEquals("400", result.getCode());
        assertEquals("参数缺失或格式不正确", result.getMsg());
    }

    @Test
    void shouldReportNotFoundRequestUrl() {
        request.setRequestURI("/x/not-exist");
        R result = handler.handleNoHandlerFoundException(new NoHandlerFoundException("GET",
            "/x/not-exist", new HttpHeaders()), request);

        assertEquals("404", result.getCode());
        assertEquals("请求 URL '/x/not-exist' 不存在", result.getMsg());
    }

    @Test
    void shouldReportUnsupportedHttpMethod() {
        R result = handler.handleHttpRequestMethodNotSupportedException(
            new HttpRequestMethodNotSupportedException(
                "DELETE"),
            request);

        assertEquals("405", result.getCode());
        assertEquals("请求方式 'DELETE' 不支持", result.getMsg());
    }

    @Test
    void shouldTranslateLargerThanSizeLimit() {
        R result =
            handler.handleMultipartException(new MultipartException("上传失败 larger than 1048576"),
                request);

        assertEquals("400", result.getCode());
        assertEquals("请上传小于 1 MB 的文件", result.getMsg());
    }

    @Test
    void shouldReturnOriginalMessageWhenMultipartNotSizeExceeded() {
        R result = handler.handleMultipartException(new MultipartException("其他上传错误"), request);

        assertEquals("400", result.getCode());
        assertEquals("其他上传错误", result.getMsg());
    }

    private HttpMessageNotReadableException unreadable(String message, Throwable cause) {
        return new HttpMessageNotReadableException(message, cause,
            new MockHttpInputMessage(new byte[0]));
    }
}
