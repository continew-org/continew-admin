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

package top.continew.admin.auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.continew.admin.auth.exception.RefreshTokenException;
import top.continew.starter.web.model.R;

/** Refresh Token HTTP 协议异常处理器。 */
@Slf4j
@Order(98)
@RestControllerAdvice
public class RefreshTokenExceptionHandler {

    /**
     * 将认证会话协议异常转换为与 HTTP 状态一致的统一响应。
     *
     * @param e        Refresh Token 协议异常
     * @param request  当前请求
     * @param response 当前响应
     * @return 统一错误响应
     */
    @ExceptionHandler(RefreshTokenException.class)
    public R handle(RefreshTokenException e, HttpServletRequest request,
        HttpServletResponse response) {
        log.warn("[{}] {}：{}", request.getMethod(), request.getRequestURI(), e.getMessage());
        response.setStatus(e.getStatus().value());
        return R.fail(String.valueOf(e.getStatus().value()), e.getMessage());
    }
}
