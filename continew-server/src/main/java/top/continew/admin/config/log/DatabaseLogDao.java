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

package top.continew.admin.config.log;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import top.continew.admin.auth.constant.AuthConstants;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.AccountLoginReq;
import top.continew.admin.auth.model.req.EmailLoginReq;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.req.PhoneLoginReq;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.system.enums.LogStatusEnum;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.model.entity.LogDO;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.util.StrUtils;
import top.continew.starter.extension.tenant.autoconfigure.TenantProperties;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.extension.tenant.util.TenantUtils;
import top.continew.starter.log.dao.LogDao;
import top.continew.starter.log.model.LogRecord;
import top.continew.starter.log.model.LogRequest;
import top.continew.starter.log.model.LogResponse;
import top.continew.starter.trace.autoconfigure.TraceProperties;
import top.continew.starter.web.model.R;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 日志持久层 - 数据库存储
 *
 * @author Charles7c
 * @since 2.2.0
 */
@RequiredArgsConstructor
public class DatabaseLogDao implements LogDao {

    private final UserService userService;
    private final LogMapper logMapper;
    private final TraceProperties traceProperties;

    @Async
    @Override
    public void add(LogRecord logRecord) {
        LogDO logEntity = new LogDO();
        // 设置请求信息
        LogRequest logRequest = logRecord.getRequest();
        this.setRequest(logEntity, logRequest);
        // 设置响应信息
        LogResponse logResponse = logRecord.getResponse();
        this.setResponse(logEntity, logResponse);
        // 设置基本信息
        logEntity.setDescription(logRecord.getDescription());
        logEntity.setModule(StrUtils.blankToDefault(logRecord.getModule(), null, m -> m
            .replace("API", StringConstants.EMPTY)
            .trim()));
        logEntity.setTimeTaken(logRecord.getTimeTaken().toMillis());
        logEntity.setCreateTime(
            LocalDateTime.ofInstant(logRecord.getTimestamp(), GlobalConstants.DEFAULT_ZONE_ID));
        // 设置操作人
        this.setCreateUser(logEntity, logRequest, logResponse);
        // 保存记录
        if (TenantContextHolder.isTenantEnabled()) {
            // 异步无法获取租户 ID
            String tenantId = this.getRequestHeaders(logRequest)
                .get(SpringUtil.getBean(TenantProperties.class).getTenantIdHeader());
            if (StrUtil.isNotBlank(tenantId)) {
                TenantUtils.execute(Long.parseLong(tenantId), () -> logMapper.insert(logEntity));
                return;
            }
        }
        logMapper.insert(logEntity);
    }

    /**
     * 设置请求信息
     *
     * @param logEntity      日志信息
     * @param logRequest 请求信息
     */
    private void setRequest(LogDO logEntity, LogRequest logRequest) {
        logEntity.setRequestMethod(logRequest.getMethod());
        logEntity.setRequestUrl(logRequest.getUrl().toString());
        logEntity.setRequestHeaders(JSONUtil.toJsonStr(this.getRequestHeaders(logRequest)));
        String requestUri = URLUtil.getPath(logEntity.getRequestUrl());
        // 登录请求体仅在当前日志处理链路中用于解析操作人，禁止持久化加密密码报文。
        logEntity.setRequestBody(
            AuthConstants.LOGIN_URI.equals(requestUri) ? null : logRequest.getBody());
        logEntity.setIp(logRequest.getIp());
        logEntity.setAddress(logRequest.getAddress());
        logEntity.setBrowser(logRequest.getBrowser());
        logEntity.setOs(StrUtil.subBefore(logRequest.getOs(), " or", false));
    }

    /**
     * 设置响应信息
     *
     * @param logEntity       日志信息
     * @param logResponse 响应信息
     */
    private void setResponse(LogDO logEntity, LogResponse logResponse) {
        if (logResponse == null) {
            logEntity.setStatusCode(HttpStatus.HTTP_INTERNAL_ERROR);
            logEntity.setStatus(LogStatusEnum.FAILURE);
            return;
        }
        Map<String, String> responseHeaders = logResponse.getHeaders();
        responseHeaders =
            responseHeaders == null ? Collections.emptyMap() : responseHeaders;
        logEntity.setResponseHeaders(JSONUtil.toJsonStr(responseHeaders));
        logEntity.setTraceId(responseHeaders.get(traceProperties.getTraceIdName()));
        String responseBody = logResponse.getBody();
        logEntity.setResponseBody(responseBody);
        // 状态
        Integer statusCode = logResponse.getStatus();
        statusCode = statusCode == null ? HttpStatus.HTTP_INTERNAL_ERROR : statusCode;
        logEntity.setStatusCode(statusCode);
        logEntity.setStatus(statusCode >= HttpStatus.HTTP_BAD_REQUEST ? LogStatusEnum.FAILURE
            : LogStatusEnum.SUCCESS);
        if (StrUtil.isNotBlank(responseBody)) {
            R result = JSONUtil.toBean(responseBody, R.class);
            if (!result.isSuccess()) {
                logEntity.setStatus(LogStatusEnum.FAILURE);
                logEntity.setErrorMsg(result.getMsg());
            }
        }
    }

    /**
     * 设置操作人
     *
     * @param logEntity       日志信息
     * @param logRequest  请求信息
     * @param logResponse 响应信息
     */
    private void setCreateUser(LogDO logEntity, LogRequest logRequest, LogResponse logResponse) {
        String requestUri = URLUtil.getPath(logEntity.getRequestUrl());
        String responseBody = logResponse == null ? null : logResponse.getBody();
        // 解析退出接口信息
        if (this.trySetCreateUserByLogout(logEntity, requestUri, responseBody)) {
            return;
        }
        // 解析登录接口信息
        if (this.trySetCreateUserByLogin(logEntity, logRequest, requestUri)) {
            return;
        }
        // 解析 Token 信息
        this.setCreateUserByToken(logEntity, logRequest);
    }

    /**
     * 解析退出接口响应中的操作人
     *
     * @param logEntity        日志信息
     * @param requestUri   请求路径
     * @param responseBody 响应信息
     * @return 是否处理完毕（false 时继续尝试其他方式）
     */
    private boolean trySetCreateUserByLogout(LogDO logEntity, String requestUri,
        String responseBody) {
        if (!AuthConstants.LOGOUT_URI.equals(requestUri) || StrUtil.isBlank(responseBody)) {
            return false;
        }
        R result = JSONUtil.toBean(responseBody, R.class);
        Long userId = Convert.toLong(result.getData(), null);
        if (userId != null && userId > 0) {
            logEntity.setCreateUser(userId);
        }
        return true;
    }

    /**
     * 解析登录接口请求中的操作人
     *
     * @param logEntity      日志信息
     * @param logRequest 请求信息
     * @param requestUri 请求路径
     * @return 是否处理完毕（false 时继续尝试其他方式）
     */
    private boolean trySetCreateUserByLogin(LogDO logEntity, LogRequest logRequest,
        String requestUri) {
        if (!AuthConstants.LOGIN_URI.equals(requestUri)
            || !LogStatusEnum.SUCCESS.equals(logEntity.getStatus())) {
            return false;
        }
        String requestBody = logRequest.getBody();
        LoginReq loginReq = ExceptionUtils.exToNull(() -> JSONUtil.toBean(requestBody,
            LoginReq.class));
        AuthTypeEnum authType = loginReq == null ? null : loginReq.getAuthType();
        // 登录接口已配置脱敏，不同日志实现或旧管理端可能没有保留 authType。操作日志
        // 不能因附加信息不完整而覆盖真实登录结果。
        if (authType == null) {
            logEntity.setDescription("登录");
            return true;
        }
        logEntity.setDescription(authType.getDescription() + "登录");
        // 解析登录用户为操作人
        if (AuthTypeEnum.ACCOUNT.equals(authType)) {
            logEntity.setCreateUser(ExceptionUtils.exToNull(() -> userService.getByUsername(JSONUtil
                .toBean(requestBody, AccountLoginReq.class)
                .getUsername()).getId()));
            return true;
        }
        if (AuthTypeEnum.EMAIL.equals(authType)) {
            logEntity.setCreateUser(ExceptionUtils.exToNull(() -> userService.getByEmail(JSONUtil
                .toBean(requestBody, EmailLoginReq.class)
                .getEmail()).getId()));
            return true;
        }
        if (AuthTypeEnum.PHONE.equals(authType)) {
            logEntity.setCreateUser(ExceptionUtils.exToNull(() -> userService.getByPhone(JSONUtil
                .toBean(requestBody, PhoneLoginReq.class)
                .getPhone()).getId()));
            return true;
        }
        // 第三方登录请求体不含用户标识，操作人继续交由 Token 解析
        return false;
    }

    /**
     * 解析请求头 Token 中的操作人
     *
     * @param logEntity      日志信息
     * @param logRequest 请求信息
     */
    private void setCreateUserByToken(LogDO logEntity, LogRequest logRequest) {
        Map<String, String> requestHeaders = this.getRequestHeaders(logRequest);
        String headerName = HttpHeaders.AUTHORIZATION;
        boolean isContainsAuthHeader = CollUtil.containsAny(requestHeaders.keySet(), Set
            .of(headerName, headerName.toLowerCase()));
        if (!isContainsAuthHeader) {
            return;
        }
        String authorization =
            requestHeaders.getOrDefault(headerName, requestHeaders
                .get(headerName.toLowerCase()));
        if (StrUtil.isBlank(authorization)) {
            return;
        }
        String token = authorization.replace(SaManager.getConfig()
            .getTokenPrefix() + StringConstants.SPACE, StringConstants.EMPTY);
        logEntity.setCreateUser(Convert.toLong(StpUtil.getLoginIdByToken(token)));
    }

    private Map<String, String> getRequestHeaders(LogRequest logRequest) {
        if (logRequest == null || logRequest.getHeaders() == null) {
            return Collections.emptyMap();
        }
        return logRequest.getHeaders();
    }
}
