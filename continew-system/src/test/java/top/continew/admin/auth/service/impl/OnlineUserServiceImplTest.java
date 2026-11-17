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

package top.continew.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import top.continew.admin.auth.config.OnlineUserProperties;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.support.ClientPolicyCache;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.auth.refresh.token.model.RefreshSession;
import top.continew.starter.auth.refresh.token.model.SessionView;
import top.continew.starter.auth.refresh.token.service.SessionInvalidationService;
import top.continew.starter.auth.refresh.token.service.SessionQueryService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.tenant.autoconfigure.TenantProperties;
import top.continew.starter.extension.tenant.context.TenantContext;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 在线用户列表派生口径测试。
 *
 * <p>覆盖租户过滤、绝对有效期过滤、在线/闲置派生（活跃记录缺失时按最近刷新估算且不给预计失效）、
 * 查询条件匹配（昵称、客户端 ID、登录时间区间）与踢下线委托。全部为纯单元测试：
 * SessionQueryService 等依赖用 Mockito 模拟，TenantContextHolder/RedisUtils 走真实或静态桩，
 * 不依赖 Spring 上下文与外部 Redis。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class OnlineUserServiceImplTest {

    private static final long TENANT_ID = 1L;
    private static final String SESSION_ID = "session-1";
    private static final String CLIENT_ID = "client-pc";

    /**
     * 当前时刻基准：会话有效期过滤使用真实时钟，会话时间统一按相对偏移构造
     *
     * @return 当前时刻（epoch 毫秒）
     */
    private static long now() {
        return System.currentTimeMillis();
    }

    private SessionQueryService sessionQueryService;
    private SessionInvalidationService sessionInvalidationService;
    private SessionActivityServiceImpl sessionActivityService;
    private ClientPolicyCache clientPolicyCache;
    private OnlineUserServiceImpl onlineUserService;
    private MockedStatic<SpringUtil> springUtilMock;

    /**
     * 桩 SpringUtil 返回的租户配置 Bean，控制多租户开关
     *
     * @param enabled 租户是否启用
     */
    private void stubTenantEnabled(boolean enabled) {
        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setEnabled(enabled);
        springUtilMock.when(() -> SpringUtil.getBean(TenantProperties.class))
            .thenReturn(tenantProperties);
    }

    @BeforeEach
    void setUp() {
        sessionQueryService = mock(SessionQueryService.class);
        sessionInvalidationService = mock(SessionInvalidationService.class);
        sessionActivityService = mock(SessionActivityServiceImpl.class);
        clientPolicyCache = mock(ClientPolicyCache.class);
        OnlineUserProperties properties = new OnlineUserProperties();
        properties.setPresenceWindow(Duration.ofMinutes(5));
        onlineUserService = new OnlineUserServiceImpl(sessionInvalidationService,
            sessionQueryService, sessionActivityService, clientPolicyCache, properties);
        // TenantContextHolder.isTenantEnabled 经 SpringUtil 取 TenantProperties Bean，
        // 纯单测不启动 Spring，改由静态桩供给
        springUtilMock = Mockito.mockStatic(SpringUtil.class);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
        UserContextHolder.clearContext();
    }

    @Test
    void shouldFilterByTenantForNonSuperAdminWhenTenantEnabled() {
        SessionView tenantSession = session(SESSION_ID, 1L, "zhangsan", CLIENT_ID);
        SessionView otherSession = session("session-2", 2L, "lisi", CLIENT_ID);
        when(sessionQueryService.listSessions(TENANT_ID)).thenReturn(List
            .of(tenantSession, otherSession));
        try (MockedStatic<StpUtil> stpUtil = Mockito.mockStatic(StpUtil.class)) {
            // setContext(isUpdate=false) 会比对当前登录态，纯单测下桩一个固定登录 ID
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(1L);
            UserContextHolder.setContext(superAdminContext(false), false);
            stubTenantEnabled(true);
            TenantContext tenantContext = new TenantContext();
            tenantContext.setTenantId(TENANT_ID);
            TenantContextHolder.setContext(tenantContext);

            try {
                onlineUserService.list(new OnlineUserQuery());
            } finally {
                TenantContextHolder.clear();
            }

            // 非超管且租户启用时会话查询必须带租户 ID，查询层按租户隔离
            verify(sessionQueryService).listSessions(TENANT_ID);
        } finally {
            UserContextHolder.clearContext();
        }
    }

    @Test
    void shouldListAllTenantsForSuperAdmin() {
        SessionView tenantSession = session(SESSION_ID, 1L, "zhangsan", CLIENT_ID);
        SessionView otherSession = session("session-2", 2L, "lisi", CLIENT_ID);
        when(sessionQueryService.listSessions(null))
            .thenReturn(List.of(tenantSession, otherSession));
        try (MockedStatic<StpUtil> stpUtil = Mockito.mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(1L);
            UserContextHolder.setContext(superAdminContext(true), false);
            stubTenantEnabled(true);

            List<OnlineUserResp> result = onlineUserService.list(new OnlineUserQuery());

            assertEquals(2, result.size());
        } finally {
            UserContextHolder.clearContext();
        }
    }

    @Test
    void shouldHideSessionExpiredByAbsoluteTimeout() {
        stubTenantEnabled(false);
        SessionView expired = session(SESSION_ID, 1L, "zhangsan", CLIENT_ID, now() - 1);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(expired));

        List<OnlineUserResp> result = onlineUserService.list(new OnlineUserQuery());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldEstimateFromLastRefreshWhenNoActiveRecord() {
        stubTenantEnabled(false);
        SessionView view = session(SESSION_ID, 1L, "zhangsan", CLIENT_ID);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(view));
        when(sessionActivityService.getLastActiveTimes(any())).thenReturn(Map.of());
        when(clientPolicyCache.all())
            .thenReturn(Map.of(CLIENT_ID, new ClientPolicyCache.ClientPolicy(600L, 86_400L)));

        List<OnlineUserResp> result = onlineUserService.list(new OnlineUserQuery());

        assertEquals(1, result.size());
        OnlineUserResp resp = result.get(0);
        // 活跃记录缺失时时间是按最近刷新推算的估算值
        assertFalse(resp.getActiveTimeExact());
        assertNull(resp.getIdleExpireTime());
        assertFalse(resp.getIdleExpired());
        assertTrue(resp.getOnline());
    }

    @Test
    void shouldMarkIdleExpiredWhenActiveTimeBeyondIdleTimeout() {
        stubTenantEnabled(false);
        SessionView view = session(SESSION_ID, 1L, "zhangsan", CLIENT_ID);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(view));
        // 有活跃记录但已超过闲置阈值：不计入在线，给出预计失效时间
        long lastActive = now() - 700_000L;
        when(sessionActivityService.getLastActiveTimes(any())).thenReturn(Map.of(SESSION_ID,
            lastActive));
        when(clientPolicyCache.all())
            .thenReturn(Map.of(CLIENT_ID, new ClientPolicyCache.ClientPolicy(600L, 86_400L)));

        List<OnlineUserResp> result = onlineUserService.list(new OnlineUserQuery());

        assertEquals(1, result.size());
        OnlineUserResp resp = result.get(0);
        assertTrue(resp.getActiveTimeExact());
        assertTrue(resp.getIdleExpired());
        assertFalse(resp.getOnline());
        long expectedExpire = lastActive + 600_000L;
        assertEquals(LocalDateTime.ofInstant(java.time.Instant
            .ofEpochMilli(expectedExpire), java.time.ZoneId.systemDefault()), resp
                .getIdleExpireTime());
    }

    @Test
    void shouldFilterByOnlineFlag() {
        stubTenantEnabled(false);
        SessionView active = session("session-1", 1L, "zhangsan", CLIENT_ID);
        SessionView idle = session("session-2", 2L, "lisi", CLIENT_ID);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(active, idle));
        long lastActive = now() - 1000L;
        long idleActive = now() - 700_000L;
        when(sessionActivityService.getLastActiveTimes(any())).thenReturn(Map
            .of("session-1", lastActive, "session-2", idleActive));
        when(clientPolicyCache.all())
            .thenReturn(Map.of(CLIENT_ID, new ClientPolicyCache.ClientPolicy(600L, 86_400L)));
        OnlineUserQuery query = new OnlineUserQuery();
        query.setOnline(Boolean.TRUE);

        List<OnlineUserResp> result = onlineUserService.list(query);

        assertEquals(1, result.size());
        assertEquals("zhangsan", result.get(0).getUsername());
    }

    @Test
    void shouldMatchNicknameAgainstUsernameOrNickname() {
        stubTenantEnabled(false);
        SessionView byUsername = session("session-1", 1L, "zhangsan", CLIENT_ID);
        // 用户名为拼音、昵称为中文：只有昵称含查询词的会话被匹配
        RefreshSession nicknameSession = new RefreshSession();
        nicknameSession.setSessionId("session-2");
        nicknameSession.setUserId(2L);
        nicknameSession.setUsername("lisi");
        nicknameSession.setNickname("张三三");
        nicknameSession.setClientType("PC");
        nicknameSession.setClientId(CLIENT_ID);
        nicknameSession.setCreatedAt(now() - 60_000L);
        nicknameSession.setLastRefreshAt(now() - 30_000L);
        nicknameSession.setExpiresAt(now() + 3_600_000L);
        SessionView byNickname = SessionView.from(nicknameSession);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(byUsername, byNickname));
        OnlineUserQuery query = new OnlineUserQuery();
        query.setNickname("张三");

        List<OnlineUserResp> result = onlineUserService.list(query);

        assertEquals(1, result.size());
        assertEquals("张三三", result.get(0).getNickname());
    }

    @Test
    void shouldRejectMalformedLoginTimeRange() {
        stubTenantEnabled(false);
        SessionView view = session(SESSION_ID, 1L, "zhangsan", CLIENT_ID);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(view));
        // 时间区间只给一个时间点：查询参数来自外部，防御性拒绝而不是宽松匹配
        OnlineUserQuery query = new OnlineUserQuery();
        query.setLoginTime(List.of(LocalDateTime.now().minusDays(1)));

        List<OnlineUserResp> result = onlineUserService.list(query);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPageBuiltFromFilteredList() {
        stubTenantEnabled(false);
        SessionView view = session(SESSION_ID, 1L, "zhangsan", CLIENT_ID);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(view));
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(1);
        pageQuery.setSize(10);

        PageResp<OnlineUserResp> page = onlineUserService.page(new OnlineUserQuery(), pageQuery);

        assertEquals(1, page.getList().size());
    }

    @Test
    void shouldDelegateKickOutToSessionInvalidation() {
        onlineUserService.kickOut(1L);

        verify(sessionInvalidationService).invalidateUser(1L);
        verify(sessionQueryService, never()).getSession(any());
    }

    private static SessionView session(String sessionId, Long userId, String username,
        String clientId) {
        return session(sessionId, userId, username, clientId, now() + 3_600_000L);
    }

    private static SessionView session(String sessionId, Long userId, String username,
        String clientId,
        long expiresAt) {
        RefreshSession refreshSession = new RefreshSession();
        refreshSession.setSessionId(sessionId);
        refreshSession.setUserId(userId);
        refreshSession.setUsername(username);
        refreshSession.setNickname(username);
        refreshSession.setClientType("PC");
        refreshSession.setClientId(clientId);
        refreshSession.setCreatedAt(now() - 60_000L);
        refreshSession.setLastRefreshAt(now() - 30_000L);
        refreshSession.setExpiresAt(expiresAt);
        refreshSession.setIp("127.0.0.1");
        return SessionView.from(refreshSession);
    }

    private static UserContext superAdminContext(boolean superAdmin) {
        UserContext context = new UserContext();
        context.setId(1L);
        if (superAdmin) {
            context.setRoleCodes(java.util.Set.of("super_admin"));
        } else {
            context.setRoleCodes(java.util.Set.of("regular"));
        }
        return context;
    }
}
