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

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.continew.starter.web.model.R;

/**
 * 全局 SaToken 异常处理器
 *
 * @author Charles7c
 * @since 2024/8/7 20:21
 */
@Slf4j
@Order(99)
@RestControllerAdvice
public class GlobalSaTokenExceptionHandler {

    /**
     * 客户端异常日志模板：[请求方法] 请求路径 - 异常信息
     *
     * <p>
     * 认证/鉴权失败（401、403）属于可预期的客户端错误，常由过期 Token 探测或越权尝试触发，
     * 打印完整堆栈会被外部请求放大成同步的多 KB 日志写入，因此只记录 WARN 级别的摘要信息。
     * </p>
     */
    private static final String LOG_CLIENT_ERROR_TEMPLATE = "[{}] {} - {}";

    /**
     * 认证异常-登录认证
     */
    @ExceptionHandler(NotLoginException.class)
    public R handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn(LOG_CLIENT_ERROR_TEMPLATE, request.getMethod(), request.getRequestURI(), e
            .getMessage());
        String errorMsg = switch (e.getType()) {
            case NotLoginException.KICK_OUT -> "您已被踢下线";
            case NotLoginException.BE_REPLACED -> "您已被顶下线";
            default -> "您的登录状态已过期，请重新登录";
        };
        return R.fail(String.valueOf(HttpStatus.UNAUTHORIZED.value()), errorMsg);
    }

    /**
     * 认证异常-权限认证
     */
    @ExceptionHandler(NotPermissionException.class)
    public R handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        log.warn(LOG_CLIENT_ERROR_TEMPLATE, request.getMethod(), request.getRequestURI(), e
            .getMessage());
        return R.fail(String.valueOf(HttpStatus.FORBIDDEN.value()), "没有访问权限，请联系管理员授权");
    }

    /**
     * 认证异常-角色认证
     */
    @ExceptionHandler(NotRoleException.class)
    public R handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        log.warn(LOG_CLIENT_ERROR_TEMPLATE, request.getMethod(), request.getRequestURI(), e
            .getMessage());
        return R.fail(String.valueOf(HttpStatus.FORBIDDEN.value()), "没有访问权限，请联系管理员授权");
    }
}
