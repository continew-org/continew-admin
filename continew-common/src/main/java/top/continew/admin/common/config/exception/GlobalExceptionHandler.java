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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.exception.BaseException;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.web.model.R;

import java.util.Objects;

/**
 * 全局异常处理器
 *
 * @author Charles7c
 * @author echo
 * @since 2024/8/7 20:21
 */
@Slf4j
@Order(99)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义异常
     */
    @ExceptionHandler(BaseException.class)
    public R handleBaseException(BaseException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage());
    }

    /**
     * 自定义验证异常-错误请求
     * <p>
     * {@code ValidationUtils.throwIfXxx(xxx)}
     * </p>
     */
    @ExceptionHandler(BadRequestException.class)
    public R handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), e.getMessage());
    }

    /**
     * 方法参数缺失异常
     * <p>
     * {@code @RequestParam} 参数缺失
     * </p>
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R handleMethodArgumentTypeMismatchException(MissingServletRequestParameterException e,
                                                       HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "参数 '%s' 缺失".formatted(e.getParameterName()));
    }

    /**
     * 方法参数无效异常
     * <p>
     * {@code @NotBlank}、{@code @NotNull} 等参数验证不通过
     * </p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        String errorMsg = ExceptionUtils.exToNull(() -> Objects.requireNonNull(e.getBindingResult().getFieldError())
            .getDefaultMessage());
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), errorMsg);
    }

    /**
     * 方法参数类型不匹配异常
     * <p>
     * {@code @RequestParam} 参数类型不匹配
     * </p>
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                       HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "参数 '%s' 类型不匹配".formatted(e.getName()));
    }

    /**
     * HTTP 消息不可读异常
     * <p>
     * 1.@RequestBody 缺失请求体<br />
     * 2.@RequestBody 实体内参数类型不匹配<br />
     * 3.请求体解析格式异常<br />
     * ...
     * </p>
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        // @RequestBody 实体内参数类型不匹配
        if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
            return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "参数 '%s' 类型不匹配"
                .formatted(invalidFormatException.getValue()));
        }
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "参数缺失或格式不正确");
    }

    /**
     * 文件上传异常-超过上传大小限制
     */
    @ExceptionHandler(MultipartException.class)
    public R handleMultipartException(MultipartException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        String msg = e.getMessage();
        R defaultFail = R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), msg);
        if (CharSequenceUtil.isBlank(msg)) {
            return defaultFail;
        }
        String sizeLimit;
        Throwable cause = e.getCause();
        if (cause != null) {
            msg = msg.concat(cause.getMessage().toLowerCase());
        }
        if (msg.contains("larger than")) {
            sizeLimit = CharSequenceUtil.subAfter(msg, "larger than ", true);
        } else if (msg.contains("size") && msg.contains("exceed")) {
            sizeLimit = CharSequenceUtil.subBetween(msg, "the maximum size ", " for");
        } else {
            return defaultFail;
        }
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "请上传小于 %s 的文件".formatted(FileUtil
            .readableFileSize(Long.parseLong(sizeLimit))));
    }

    /**
     * 请求 URL 不存在异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.NOT_FOUND.value()), "请求 URL '%s' 不存在".formatted(request
            .getRequestURI()));
    }

    /**
     * 不支持的 HTTP 请求方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                          HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value()), "请求方式 '%s' 不支持".formatted(e.getMethod()));
    }
}