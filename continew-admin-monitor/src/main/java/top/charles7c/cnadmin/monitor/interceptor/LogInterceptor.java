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

package top.charles7c.cnadmin.monitor.interceptor;

import java.time.LocalDateTime;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;

import top.charles7c.cnadmin.common.model.dto.LogContext;
import top.charles7c.cnadmin.common.util.IpUtils;
import top.charles7c.cnadmin.common.util.ServletUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.holder.LogContextHolder;
import top.charles7c.cnadmin.monitor.annotation.Log;
import top.charles7c.cnadmin.monitor.config.properties.LogProperties;
import top.charles7c.cnadmin.monitor.enums.LogResultEnum;
import top.charles7c.cnadmin.monitor.model.entity.SysLog;

/**
 * 系统日志拦截器
 *
 * @author Charles7c
 * @since 2022/12/24 21:14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogInterceptor implements HandlerInterceptor {

    private final LogProperties operationLogProperties;
    private static final String ENCRYPT_SYMBOL = "****************";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull Object handler) {
        if (checkIsNeedRecord(handler, request)) {
            // 记录操作时间
            this.logCreateTime();
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull Object handler, Exception e) {
        // 记录请求耗时及异常信息
        SysLog sysLog = this.logElapsedTimeAndException();
        if (sysLog == null) {
            return;
        }

        // 记录日志描述
        this.logDescription(sysLog, handler);
        // 记录请求信息
        this.logRequest(sysLog, request);
        // 记录响应信息
        this.logResponse(sysLog, response);

        // 保存系统日志
        SpringUtil.getApplicationContext().publishEvent(sysLog);
    }

    /**
     * 记录操作时间
     */
    private void logCreateTime() {
        LogContext logContext = new LogContext();
        logContext.setCreateUser(LoginHelper.getUserId());
        logContext.setCreateTime(LocalDateTime.now());
        LogContextHolder.set(logContext);
    }

    /**
     * 记录请求耗时及异常信息
     *
     * @return 系统日志信息
     */
    private SysLog logElapsedTimeAndException() {
        LogContext logContext = LogContextHolder.get();
        if (logContext != null) {
            LogContextHolder.remove();
            SysLog sysLog = new SysLog();
            sysLog.setCreateTime(logContext.getCreateTime());
            sysLog.setElapsedTime(System.currentTimeMillis() - LocalDateTimeUtil.toEpochMilli(sysLog.getCreateTime()));
            sysLog.setResult(LogResultEnum.SUCCESS);

            // 记录异常信息
            Exception exception = logContext.getException();
            if (exception != null) {
                sysLog.setResult(LogResultEnum.FAILURE);
                sysLog.setException(ExceptionUtil.stacktraceToString(exception, -1));
            }
            return sysLog;
        }
        return null;
    }

    /**
     * 记录日志描述
     *
     * @param sysLog
     *            系统日志信息
     * @param handler
     *            处理器
     */
    private void logDescription(SysLog sysLog, Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        Operation methodOperation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Operation.class);
        Log methodLog = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Log.class);

        if (methodOperation != null) {
            sysLog.setDescription(
                StrUtil.isNotBlank(methodOperation.summary()) ? methodOperation.summary() : "请在该接口方法上指定日志描述");
        }
        // 例如：@Log("获取验证码") -> 获取验证码
        if (methodLog != null && StrUtil.isNotBlank(methodLog.value())) {
            sysLog.setDescription(methodLog.value());
        }
    }

    /**
     * 记录请求信息
     *
     * @param sysLog
     *            系统日志信息
     * @param request
     *            请求对象
     */
    private void logRequest(SysLog sysLog, HttpServletRequest request) {
        sysLog.setRequestUrl(StrUtil.isBlank(request.getQueryString()) ? request.getRequestURL().toString()
            : request.getRequestURL().append("?").append(request.getQueryString()).toString());
        sysLog.setRequestMethod(request.getMethod());
        sysLog.setRequestHeader(this.desensitize(ServletUtil.getHeaderMap(request)));
        String requestBody = this.getRequestBody(request);
        if (StrUtil.isNotBlank(requestBody)) {
            sysLog.setRequestBody(this.desensitize(
                JSONUtil.isTypeJSON(requestBody) ? JSONUtil.parseObj(requestBody) : ServletUtil.getParamMap(request)));
        }
        sysLog.setRequestIp(ServletUtil.getClientIP(request));
        sysLog.setLocation(IpUtils.getCityInfo(sysLog.getRequestIp()));
        sysLog.setBrowser(ServletUtils.getBrowser(request));
        sysLog.setCreateUser(sysLog.getCreateUser() == null ? LoginHelper.getUserId() : sysLog.getCreateUser());
    }

    /**
     * 记录响应信息
     *
     * @param sysLog
     *            系统日志信息
     * @param response
     *            响应对象
     */
    private void logResponse(SysLog sysLog, HttpServletResponse response) {
        int status = response.getStatus();
        sysLog.setStatusCode(status);
        sysLog.setResponseHeader(this.desensitize(ServletUtil.getHeadersMap(response)));
        // 响应体（不记录非 JSON 响应数据）
        String responseBody = this.getResponseBody(response);
        if (StrUtil.isNotBlank(responseBody) && JSONUtil.isTypeJSON(responseBody)) {
            sysLog.setResponseBody(responseBody);
        }
        // 操作失败：>= 400
        sysLog.setResult(status >= HttpStatus.HTTP_BAD_REQUEST ? LogResultEnum.FAILURE : sysLog.getResult());
    }

    /**
     * 数据脱敏
     *
     * @param waitDesensitizeData
     *            待脱敏数据
     * @return 脱敏后的 JSON 字符串数据
     */
    @SuppressWarnings("unchecked")
    private String desensitize(Map waitDesensitizeData) {
        String desensitizeDataStr = JSONUtil.toJsonStr(waitDesensitizeData);
        try {
            if (CollUtil.isEmpty(waitDesensitizeData)) {
                return desensitizeDataStr;
            }

            for (String desensitizeProperty : operationLogProperties.getDesensitize()) {
                waitDesensitizeData.computeIfPresent(desensitizeProperty, (k, v) -> ENCRYPT_SYMBOL);
                waitDesensitizeData.computeIfPresent(desensitizeProperty.toLowerCase(), (k, v) -> ENCRYPT_SYMBOL);
                waitDesensitizeData.computeIfPresent(desensitizeProperty.toUpperCase(), (k, v) -> ENCRYPT_SYMBOL);
            }
            return JSONUtil.toJsonStr(waitDesensitizeData);
        } catch (Exception ignored) {
        }
        return desensitizeDataStr;
    }

    /**
     * 获取请求体
     *
     * @param request
     *            请求对象
     * @return 请求体
     */
    private String getRequestBody(HttpServletRequest request) {
        String requestBody = "";
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            requestBody = StrUtil.utf8Str(wrapper.getContentAsByteArray());
        }
        return requestBody;
    }

    /**
     * 获取响应体
     *
     * @param response
     *            响应对象
     * @return 响应体
     */
    private String getResponseBody(HttpServletResponse response) {
        String responseBody = "";
        ContentCachingResponseWrapper wrapper =
            WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            responseBody = StrUtil.utf8Str(wrapper.getContentAsByteArray());
        }
        return responseBody;
    }

    /**
     * 检查是否要记录系统日志
     *
     * @param handler
     *            /
     * @param request
     *            /
     * @return true 需要记录，false 不需要记录
     */
    private boolean checkIsNeedRecord(Object handler, HttpServletRequest request) {
        // 1、未启用时，不需要记录系统日志
        if (!(handler instanceof HandlerMethod) || Boolean.FALSE.equals(operationLogProperties.getEnabled())) {
            return false;
        }

        // 2、检查是否需要记录内网 IP 操作
        boolean isInnerIp = IpUtils.isInnerIP(ServletUtil.getClientIP(request));
        if (isInnerIp && Boolean.FALSE.equals(operationLogProperties.getIncludeInnerIp())) {
            return false;
        }

        // 3、排除不需要记录系统日志的接口
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        Log methodLog = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Log.class);
        // 3.1 请求方式不要求记录且请求上没有 @Log 注解，则不记录系统日志
        if (operationLogProperties.getExcludeMethods().contains(request.getMethod()) && methodLog == null) {
            return false;
        }
        // 3.2 如果接口上既没有 @Log 注解，也没有 @Operation 注解，则不记录系统日志
        Operation methodOperation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Operation.class);
        if (methodLog == null && methodOperation == null) {
            return false;
        }
        // 3.3 如果接口被隐藏，不记录系统日志
        if (methodOperation != null && methodOperation.hidden()) {
            return false;
        }
        // 3.4 如果接口上有 @Log 注解，但是要求忽略该接口，则不记录系统日志
        return methodLog == null || !methodLog.ignore();
    }
}
