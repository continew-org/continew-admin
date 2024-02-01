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

package top.charles7c.continew.admin.monitor.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import top.charles7c.continew.admin.auth.model.req.AccountLoginReq;
import top.charles7c.continew.admin.common.constant.SysConstants;
import top.charles7c.continew.admin.monitor.enums.LogStatusEnum;
import top.charles7c.continew.admin.monitor.mapper.LogMapper;
import top.charles7c.continew.admin.monitor.model.entity.LogDO;
import top.charles7c.continew.admin.system.service.UserService;
import top.charles7c.continew.starter.core.constant.StringConstants;
import top.charles7c.continew.starter.core.util.ExceptionUtils;
import top.charles7c.continew.starter.log.common.dao.LogDao;
import top.charles7c.continew.starter.log.common.model.LogRecord;
import top.charles7c.continew.starter.log.common.model.LogRequest;
import top.charles7c.continew.starter.log.common.model.LogResponse;
import top.charles7c.continew.starter.web.autoconfigure.trace.TraceProperties;
import top.charles7c.continew.starter.web.model.R;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

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
        logDO.setModule(StrUtil.isNotBlank(module)
            ? logRecord.getModule().replace("API", StringConstants.EMPTY).trim()
            : null);
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
        if (!requestUri.startsWith(SysConstants.LOGOUT_URI) && MapUtil.isNotEmpty(requestHeaderMap) && requestHeaderMap
            .containsKey(HttpHeaders.AUTHORIZATION)) {
            String authorization = requestHeaderMap.get(HttpHeaders.AUTHORIZATION);
            String token = authorization.replace(SaManager.getConfig()
                .getTokenPrefix() + StringConstants.SPACE, StringConstants.EMPTY);
            logDO.setCreateUser(Convert.toLong(StpUtil.getLoginIdByToken(token)));
        }
        logMapper.insert(logDO);
    }
}
