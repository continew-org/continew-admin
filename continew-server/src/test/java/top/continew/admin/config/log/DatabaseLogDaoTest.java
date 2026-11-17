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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.AccountLoginReq;
import top.continew.admin.system.enums.LogStatusEnum;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.model.entity.LogDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.service.UserService;
import top.continew.starter.extension.tenant.autoconfigure.TenantProperties;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.extension.tenant.util.TenantUtils;
import top.continew.starter.log.model.LogRecord;
import top.continew.starter.log.model.LogRequest;
import top.continew.starter.log.model.LogResponse;
import top.continew.starter.trace.autoconfigure.TraceProperties;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库日志持久化单元测试。
 *
 * <p>覆盖请求/响应字段映射、操作人三级解析（退出响应 → 登录请求体 → Token 头）、
 * 登录报文不落库约束与租户上下文写入路径。Mapper 与用户服务用 Mockito 模拟，
 * 租户判定按「先 SpringUtil、后 TenantContextHolder」的顺序模拟。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class DatabaseLogDaoTest {

    private static final String LOGIN_URI = "/auth/login";
    private static final String LOGOUT_URI = "/auth/logout";

    private UserService userService;
    private LogMapper logMapper;
    private DatabaseLogDao logDao;
    private TraceProperties traceProperties;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<TenantContextHolder> tenantContextHolderMock;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        logMapper = mock(LogMapper.class);
        when(logMapper.insert(any(LogDO.class))).thenReturn(1);
        traceProperties = new TraceProperties();
        traceProperties.setTraceIdName("X-Trace-Id");
        logDao = new DatabaseLogDao(userService, logMapper, traceProperties);
        // add() 入口即经 SpringUtil 判定租户开关，两个静态桩为全部用例的公共前置
        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setEnabled(false);
        tenantProperties.setTenantIdHeader("X-Tenant-Id");
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(TenantProperties.class))
            .thenReturn(tenantProperties);
        tenantContextHolderMock = mockStatic(TenantContextHolder.class);
        tenantContextHolderMock.when(TenantContextHolder::isTenantEnabled).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        tenantContextHolderMock.close();
        springUtilMock.close();
        TenantContextHolder.clear();
    }

    @Test
    void shouldRecordFailureWithDefaultStatusWhenResponseMissing() {
        logDao.add(record(LOGIN_URI, null, null, null));

        LogDO entity = capturedEntity();
        assertEquals(500, entity.getStatusCode());
        assertEquals(LogStatusEnum.FAILURE, entity.getStatus());
        assertNull(entity.getCreateUser());
        assertEquals("登录测试模块", entity.getModule());
        assertNotNull(entity.getCreateTime());
        assertEquals(120L, entity.getTimeTaken());
    }

    @Test
    void shouldNotPersistLoginRequestBody() {
        logDao.add(record(LOGIN_URI, "{\"username\":\"admin\",\"password\":\"encrypted\"}", 200,
            okBody()));

        LogDO entity = capturedEntity();
        assertNull(entity.getRequestBody());
        assertEquals(LogStatusEnum.SUCCESS, entity.getStatus());
    }

    @Test
    void shouldPersistRequestBodyForNonLoginUri() {
        logDao.add(record("/system/user/page", "{\"deptId\":1}", 200, okBody()));

        LogDO entity = capturedEntity();
        assertEquals("{\"deptId\":1}", entity.getRequestBody());
        assertNull(entity.getCreateUser());
    }

    @Test
    void shouldMarkFailureWhenHttpStatusIsServerError() {
        logDao.add(record("/system/user/page", null, 502, okBody()));

        assertEquals(LogStatusEnum.FAILURE, capturedEntity().getStatus());
    }

    @Test
    void shouldMarkFailureWhenBizCodeIsNotSuccess() {
        logDao.add(record("/system/user/page", null, 200, failBody()));

        LogDO entity = capturedEntity();
        assertEquals(LogStatusEnum.FAILURE, entity.getStatus());
        assertEquals("操作失败", entity.getErrorMsg());
    }

    @Test
    void shouldMarkFailureWhenResponseHeadersMissing() {
        LogRecord record = record("/system/user/page", null, 200, okBody());
        when(record.getResponse().getHeaders()).thenReturn(null);

        logDao.add(record);

        LogDO entity = capturedEntity();
        assertNull(entity.getTraceId());
        assertEquals(LogStatusEnum.SUCCESS, entity.getStatus());
    }

    @Test
    void shouldResolveTraceIdFromResponseHeaders() {
        LogRecord record = record("/system/user/page", null, 200, okBody());
        when(record.getResponse().getHeaders()).thenReturn(Map.of("X-Trace-Id", "trace-1"));

        logDao.add(record);

        assertEquals("trace-1", capturedEntity().getTraceId());
    }

    @Test
    void shouldResolveCreateUserFromLogoutResponseBody() {
        logDao.add(record(LOGOUT_URI, null, 200, logoutBody(234L), Map
            .of("Authorization", "Bearer token-x")));

        assertEquals(234L, capturedEntity().getCreateUser());
    }

    @Test
    void shouldSkipLogoutParseWhenResponseBodyBlank() {
        logDao.add(record(LOGOUT_URI, null, 200, null, Map
            .of("Authorization", "Bearer raw-token")));

        LogDO entity = capturedEntity();
        assertNull(entity.getCreateUser());
    }

    @Test
    void shouldResolveCreateUserFromAccountLoginRequest() {
        AccountLoginReq req = new AccountLoginReq();
        req.setAuthType(AuthTypeEnum.ACCOUNT);
        req.setUsername("admin");
        when(userService.getByUsername("admin")).thenReturn(user(1L));

        logDao.add(record(LOGIN_URI, JSONUtil.toJsonStr(req), 200, okBody()));

        LogDO entity = capturedEntity();
        assertEquals(1L, entity.getCreateUser());
        assertEquals("账号登录", entity.getDescription());
    }

    @Test
    void shouldKeepLoginDescriptionWhenAuthTypeMissing() {
        logDao.add(record(LOGIN_URI, "{\"username\":\"admin\"}", 200, okBody()));

        LogDO entity = capturedEntity();
        assertEquals("登录", entity.getDescription());
        assertNull(entity.getCreateUser());
    }

    @Test
    void shouldFallThroughToTokenParseForSocialLoginRequest() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenPrefix("Bearer");

        try (MockedStatic<SaManager> saManager = mockStatic(SaManager.class);
            MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            saManager.when(SaManager::getConfig).thenReturn(config);
            stpUtil.when(() -> StpUtil.getLoginIdByToken("raw-token")).thenReturn(7L);
            logDao.add(record(LOGIN_URI, "{\"authType\":\"SOCIAL\"}", 200, okBody(), Map
                .of("Authorization", "Bearer raw-token")));
        }

        assertEquals(7L, capturedEntity().getCreateUser());
    }

    @Test
    void shouldResolveCreateUserFromTokenHeaderWhenNotLoginOrLogout() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenPrefix("Bearer");

        try (MockedStatic<SaManager> saManager = mockStatic(SaManager.class);
            MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            saManager.when(SaManager::getConfig).thenReturn(config);
            stpUtil.when(() -> StpUtil.getLoginIdByToken("raw-token")).thenReturn(66L);
            logDao.add(record("/system/user/page", null, 200, okBody(), Map
                .of("Authorization", "Bearer raw-token")));
        }

        assertEquals(66L, capturedEntity().getCreateUser());
    }

    @Test
    void shouldReadLowercaseAuthorizationHeader() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenPrefix("Bearer");

        try (MockedStatic<SaManager> saManager = mockStatic(SaManager.class);
            MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            saManager.when(SaManager::getConfig).thenReturn(config);
            stpUtil.when(() -> StpUtil.getLoginIdByToken("lower-token")).thenReturn(88L);
            logDao.add(record("/system/user/page", null, 200, okBody(), Map
                .of("authorization", "Bearer lower-token")));
        }

        assertEquals(88L, capturedEntity().getCreateUser());
    }

    @Test
    void shouldSkipTokenParseWhenAuthorizationHeaderAbsent() {
        logDao.add(record("/system/user/page", null, 200, okBody(), Map.of("X-Other", "v")));

        assertNull(capturedEntity().getCreateUser());
    }

    @Test
    void shouldSkipTokenParseWhenAuthorizationValueBlank() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenPrefix("Bearer");

        try (MockedStatic<SaManager> saManager = mockStatic(SaManager.class)) {
            saManager.when(SaManager::getConfig).thenReturn(config);
            logDao.add(record("/system/user/page", null, 200, okBody(), Map
                .of("Authorization", " ")));
        }

        assertNull(capturedEntity().getCreateUser());
    }

    @Test
    void shouldInsertWithinTenantContextWhenTenantHeaderPresent() {
        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setEnabled(true);
        tenantProperties.setTenantIdHeader("X-Tenant-Id");
        springUtilMock.when(() -> SpringUtil.getBean(TenantProperties.class))
            .thenReturn(tenantProperties);
        tenantContextHolderMock.when(TenantContextHolder::isTenantEnabled).thenReturn(true);

        try (MockedStatic<TenantUtils> tenantUtils = mockStatic(TenantUtils.class)) {
            // TenantUtils.execute 的语义是在指定租户上下文内同步执行，桩中直接透传被代理动作
            tenantUtils.when(() -> TenantUtils.execute(eq(999L), any(Runnable.class)))
                .thenAnswer(passThroughRunnable());
            logDao.add(record("/system/user/page", null, 200, okBody(), Map
                .of("X-Tenant-Id", "999")));
        }

        verify(logMapper).insert(any(LogDO.class));
    }

    @Test
    void shouldInsertWithoutTenantContextWhenTenantHeaderBlank() {
        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setEnabled(true);
        tenantProperties.setTenantIdHeader("X-Tenant-Id");
        springUtilMock.when(() -> SpringUtil.getBean(TenantProperties.class))
            .thenReturn(tenantProperties);
        tenantContextHolderMock.when(TenantContextHolder::isTenantEnabled).thenReturn(true);

        logDao.add(record("/system/user/page", null, 200, okBody(), Map.of("X-Tenant-Id", " ")));

        verify(logMapper).insert(any(LogDO.class));
    }

    @Test
    void shouldInsertDirectlyWhenTenantDisabled() {
        logDao.add(record("/system/user/page", null, 200, okBody(), Map.of("X-Tenant-Id", "999")));

        verify(logMapper).insert(any(LogDO.class));
    }

    @Test
    void shouldTruncateOsNameBeforePlatformSeparator() {
        logDao.add(record("/system/user/page", null, 200, okBody(), null, "Windows 10 or newer"));

        assertEquals("Windows 10", capturedEntity().getOs());
    }

    private LogDO capturedEntity() {
        ArgumentCaptor<LogDO> captor = ArgumentCaptor.forClass(LogDO.class);
        verify(logMapper).insert(captor.capture());
        return captor.getValue();
    }

    private Answer<Void> passThroughRunnable() {
        return invocation -> {
            ((Runnable) invocation.getArgument(1)).run();
            return null;
        };
    }

    private LogRecord record(String requestUri, String body, Integer status, String responseBody) {
        return record(requestUri, body, status, responseBody, null, "Windows");
    }

    private LogRecord record(String requestUri, String body, Integer status, String responseBody,
        Map<String, String> requestHeaders) {
        return record(requestUri, body, status, responseBody, requestHeaders, "Windows");
    }

    private LogRecord record(String requestUri, String body, Integer status, String responseBody,
        Map<String, String> requestHeaders, String os) {
        LogRequest request = mock(LogRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getUrl()).thenReturn(URI.create("http://localhost:8000" + requestUri));
        when(request.getIp()).thenReturn("127.0.0.1");
        when(request.getBrowser()).thenReturn("Chrome");
        when(request.getOs()).thenReturn(os);
        when(request.getBody()).thenReturn(body);
        when(request.getHeaders()).thenReturn(requestHeaders);
        LogResponse response = null;
        if (status != null || responseBody != null) {
            response = mock(LogResponse.class);
            when(response.getStatus()).thenReturn(status);
            when(response.getHeaders()).thenReturn(new HashMap<>());
            when(response.getBody()).thenReturn(responseBody);
        }
        LogRecord record = new LogRecord(Instant.now(), request, response, Duration
            .ofMillis(120));
        record.setDescription("登录测试");
        record.setModule("登录测试模块API");
        return record;
    }

    private String okBody() {
        JSONObject json = new JSONObject();
        json.set("code", "0");
        json.set("msg", "ok");
        return json.toString();
    }

    private String failBody() {
        JSONObject json = new JSONObject();
        json.set("code", "1");
        json.set("msg", "操作失败");
        return json.toString();
    }

    private String logoutBody(Long userId) {
        JSONObject json = new JSONObject();
        json.set("code", "0");
        json.set("data", userId);
        return json.toString();
    }

    private UserDO user(Long id) {
        UserDO userDO = new UserDO();
        userDO.setId(id);
        return userDO;
    }
}
