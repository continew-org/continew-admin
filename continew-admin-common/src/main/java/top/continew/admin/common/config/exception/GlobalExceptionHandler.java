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

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.web.model.R;

/**
 * 全局异常处理器
 *
 * @author Charles7c
 * @since 2024/8/7 20:21
 */
@Slf4j
@Order(99)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage());
    }

    /**
     * 拦截自定义验证异常-错误请求
     */
    @ExceptionHandler(BadRequestException.class)
    public R handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), e.getMessage());
    }

    /**
     * 拦截文件上传异常-超过上传大小限制
     */
    @ExceptionHandler(MultipartException.class)
    public R handleRequestTooBigException(MultipartException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        String msg = e.getMessage();
        R defaultFail = R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), msg);
        if (CharSequenceUtil.isBlank(msg)) {
            return defaultFail;
        }
        String sizeLimit;
        Throwable cause = e.getCause();
        if (null != cause) {
            msg = msg.concat(cause.getMessage().toLowerCase());
        }
        if (msg.contains("size") && msg.contains("exceed")) {
            sizeLimit = CharSequenceUtil.subBetween(msg, "the maximum size ", " for");
        } else if (msg.contains("larger than")) {
            sizeLimit = CharSequenceUtil.subAfter(msg, "larger than ", true);
        } else {
            return defaultFail;
        }
        String errorMsg = "请上传小于 %sKB 的文件".formatted(NumberUtil.parseLong(sizeLimit) / 1024);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), errorMsg);
    }
}