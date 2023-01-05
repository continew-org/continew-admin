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

package top.charles7c.cnadmin.common.handler;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.exception.BadRequestException;
import top.charles7c.cnadmin.common.exception.ServiceException;
import top.charles7c.cnadmin.common.model.dto.OperationLog;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.StreamUtils;
import top.charles7c.cnadmin.common.util.holder.LogContextHolder;

/**
 * 全局异常处理器
 *
 * @author Charles7c
 * @since 2022/12/21 21:01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截未知的系统异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public R handleException(Exception e, HttpServletRequest request) {
        this.setException(e);
        log.error("请求地址'{}'，发生未知异常", request.getRequestURI(), e);
        return R.fail(e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public R handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        this.setException(e);
        log.error("请求地址'{}'，发生系统异常", request.getRequestURI(), e);
        return R.fail(e.getMessage());
    }

    /**
     * 拦截业务异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServiceException.class)
    public R handleServiceException(ServiceException e, HttpServletRequest request) {
        this.setException(e);
        log.error("请求地址'{}'，发生业务异常", request.getRequestURI(), e);
        return R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

    /**
     * 拦截自定义验证异常-错误请求
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public R handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        log.error("请求地址'{}'，自定义验证失败", request.getRequestURI(), e);
        return R.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    /**
     * 拦截校验异常-绑定异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public R handleBindException(BindException e, HttpServletRequest request) {
        log.error("请求地址'{}'，参数验证失败", request.getRequestURI(), e);
        String message = StreamUtils.join(e.getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, "，");
        return R.fail(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 拦截校验异常-违反约束异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public R constraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.error("请求地址'{}'，参数验证失败", request.getRequestURI(), e);
        String message = StreamUtils.join(e.getConstraintViolations(), ConstraintViolation::getMessage, "，");
        return R.fail(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 拦截校验异常-方法参数无效异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("请求地址'{}'，参数验证失败", request.getRequestURI(), e);
        return R.fail(HttpStatus.BAD_REQUEST.value(), ExceptionUtils
            .exToNull(() -> Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage()));
    }

    /**
     * 拦截校验异常-方法参数类型不匹配异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
        HttpServletRequest request) {
        String subMsg = StrUtil.format("参数名：'{}'，期望参数类型：'{}'", e.getName(), e.getParameter().getParameterType());
        log.error("请求地址'{}'，参数转换失败。方法：'{}'，{}", request.getRequestURI(),
            Objects.requireNonNull(e.getParameter().getMethod()).getName(), subMsg, e);
        return R.fail(HttpStatus.BAD_REQUEST.value(), subMsg);
    }

    /**
     * 拦截校验异常-请求方式不支持异常
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.error("请求地址'{}'，不支持'{}'请求", request.getRequestURI(), e.getMethod());
        return R.fail(HttpStatus.METHOD_NOT_ALLOWED.value(), e.getMessage());
    }

    /**
     * 拦截认证异常-未登录异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    public R handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.error("请求地址'{}'，认证失败'{}'，无法访问系统资源", request.getRequestURI(), e.getMessage());
        return R.fail(HttpStatus.UNAUTHORIZED.value(), "认证失败，无法访问系统资源");
    }

    /**
     * 拦截文件上传异常-超过上传大小限制
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        String sizeLimit = StrUtil.subBetween(e.getMessage(), "The maximum size ", " for");
        log.error("请求地址'{}'，上传文件失败，文件大小超过限制", request.getRequestURI(), e);
        return R.fail(HttpStatus.BAD_REQUEST.value(),
            String.format("请上传小于 %s MB 的文件", NumberUtil.parseLong(sizeLimit) / 1024 / 1024));
    }

    /**
     * 操作日志保存异常信息
     *
     * @param e
     *            异常信息
     */
    private void setException(Exception e) {
        OperationLog operationLog = LogContextHolder.get();
        if (operationLog != null) {
            operationLog.setException(e);
        }
    }
}