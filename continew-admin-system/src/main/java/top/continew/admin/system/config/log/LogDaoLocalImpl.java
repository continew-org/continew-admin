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

package top.continew.admin.system.config.log;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import top.continew.admin.auth.model.req.AccountLoginReq;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.system.enums.LogStatusEnum;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.model.entity.LogDO;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.util.StrUtils;
import top.continew.starter.log.core.dao.LogDao;
import top.continew.starter.log.core.model.LogRecord;
import top.continew.starter.log.core.model.LogRequest;
import top.continew.starter.log.core.model.LogResponse;
import top.continew.starter.web.autoconfigure.trace.TraceProperties;
import top.continew.starter.web.model.R;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

/**
 * 日志持久层接口本地实现类
 *
 * @author Charles7c
 * @since 2023/12/16 23:55
 */
@RequiredArgsConstructor
public class LogDaoLocalImpl implements LogDao {

    private final UserService userService;
    private final LogMapper logMapper;
    private final TraceProperties traceProperties;

    @Async
    @Override
    public void add(LogRecord logRecord) {
        LogDO logDO = new LogDO();
        logDO.setDescription(logRecord.getDescription());
        String module = logRecord.getModule();
        logDO.setModule(StrUtils.blankToDefault(module, null, m -> m.replace("API", StringConstants.EMPTY).trim()));
        logDO.setCreateTime(LocalDateTime.ofInstant(logRecord.getTimestamp(), ZoneId.systemDefault()));
        logDO.setTimeTaken(logRecord.getTimeTaken().toMillis());
        // 请求信息
        LogRequest logRequest = logRecord.getRequest();
        logDO.setRequestMethod(logRequest.getMethod());
        URI requestUrl = logRequest.getUrl();
        String requestUri = requestUrl.getPath();
        logDO.setRequestUrl(requestUrl.toString());
        Map<String, String> requestHeaderMap = logRequest.getHeaders();
        logDO.setRequestHeaders(JSONUtil.toJsonStr(requestHeaderMap));
        String requestBody = logRequest.getBody();
        logDO.setRequestBody(requestBody);
        logDO.setIp(logRequest.getIp());
        logDO.setAddress(logRequest.getAddress());
        logDO.setBrowser(logRequest.getBrowser());
        logDO.setOs(StrUtil.subBefore(logRequest.getOs(), " or", false));
        // 响应信息
        LogResponse logResponse = logRecord.getResponse();
        Integer statusCode = logResponse.getStatus();
        logDO.setStatusCode(statusCode);
        Map<String, String> responseHeaders = logResponse.getHeaders();
        logDO.setResponseHeaders(JSONUtil.toJsonStr(responseHeaders));
        logDO.setTraceId(responseHeaders.get(traceProperties.getHeaderName()));
        String responseBody = logResponse.getBody();
        logDO.setResponseBody(responseBody);
        // 状态
        logDO.setStatus(statusCode >= HttpStatus.HTTP_BAD_REQUEST ? LogStatusEnum.FAILURE : LogStatusEnum.SUCCESS);
        if (StrUtil.isNotBlank(responseBody) && JSONUtil.isTypeJSON(responseBody)) {
            R result = JSONUtil.toBean(responseBody, R.class);
            if (!result.isSuccess()) {
                logDO.setStatus(LogStatusEnum.FAILURE);
                logDO.setErrorMsg(result.getMsg());
            }
            // 操作人
            if (requestUri.startsWith(SysConstants.LOGOUT_URI)) {
                Long loginId = Convert.toLong(result.getData(), -1L);
                logDO.setCreateUser(-1 != loginId ? loginId : null);
            } else if (result.isSuccess() && requestUri.startsWith(SysConstants.LOGIN_URI)) {
                AccountLoginReq loginReq = JSONUtil.toBean(requestBody, AccountLoginReq.class);
                logDO.setCreateUser(ExceptionUtils.exToNull(() -> userService.getByUsername(loginReq.getUsername())
                    .getId()));
            }
        }
        // 操作人
        String headerName = HttpHeaders.AUTHORIZATION;
        boolean isContains = CollUtil.containsAny(requestHeaderMap.keySet(), Set.of(headerName, headerName
            .toLowerCase()));
        if (!requestUri.startsWith(SysConstants.LOGOUT_URI) && MapUtil.isNotEmpty(requestHeaderMap) && isContains) {
            String authorization = requestHeaderMap.getOrDefault(headerName, requestHeaderMap.get(headerName
                .toLowerCase()));
            String token = authorization.replace(SaManager.getConfig()
                .getTokenPrefix() + StringConstants.SPACE, StringConstants.EMPTY);
            logDO.setCreateUser(Convert.toLong(StpUtil.getLoginIdByToken(token)));
        }
        logMapper.insert(logDO);
    }
}
