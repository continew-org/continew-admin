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

package top.continew.admin.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.auth.config.OnlineUserProperties;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.service.impl.OnlineUserServiceImpl;
import top.continew.admin.auth.support.ClientPolicyCache;
import top.continew.starter.auth.refresh.token.model.RefreshSession;
import top.continew.starter.auth.refresh.token.model.SessionView;
import top.continew.starter.auth.refresh.token.service.SessionInvalidationService;
import top.continew.starter.auth.refresh.token.service.SessionQueryService;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * 在线用户以 Refresh Session 为事实源测试
 *
 * @author luoqiz
 * @since 4.2.0
 */
class OnlineUserServiceImplTest {

    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;

    private MockedStatic<TenantContextHolder> tenantContextHolder;

    @BeforeEach
    void setUp() {
        // 会话查询前会判断多租户是否启用，静态上下文在单元测试里没有 Spring 容器可用
        tenantContextHolder = mockStatic(TenantContextHolder.class);
        tenantContextHolder.when(TenantContextHolder::isTenantEnabled).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        tenantContextHolder.close();
    }

    @Test
    void shouldListRefreshSessionAfterAccessTokenExpires() {
        long now = System.currentTimeMillis();
        SessionQueryService sessionQueryService = mock(SessionQueryService.class);
        SessionView session = this.session("session-1", now - ONE_DAY_MILLIS, now
            + ONE_DAY_MILLIS);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(session));

        List<OnlineUserResp> result = this.service(sessionQueryService, Map.of(), Map.of())
            .list(new OnlineUserQuery());

        assertEquals(1, result.size());
        OnlineUserResp resp = result.get(0);
        assertEquals("session-1", resp.getSessionId());
        assertEquals(this.toLocalDateTime(session.getCreatedAt()), resp.getLoginTime());
        assertEquals(this.toLocalDateTime(session.getLastRefreshAt()), resp.getLastRefreshTime());
        // 没有活跃记录时回退到最近刷新时间，并标记为估算值
        assertEquals(this.toLocalDateTime(session.getLastRefreshAt()), resp.getLastActiveTime());
        assertFalse(resp.getActiveTimeExact());
        assertEquals(this.toLocalDateTime(session.getExpiresAt()), resp.getSessionExpireTime());
        assertNull(resp.getIdleExpireTime());
        assertFalse(resp.getIdleExpired());
        assertFalse(resp.getOnline());
    }

    @Test
    void shouldMarkOnlineWhenActiveWithinPresenceWindow() {
        long now = System.currentTimeMillis();
        SessionQueryService sessionQueryService = mock(SessionQueryService.class);
        SessionView session = this.session("session-1", now - ONE_DAY_MILLIS, now
            + ONE_DAY_MILLIS);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(session));

        List<OnlineUserResp> result = this.service(sessionQueryService, Map
            .of("session-1", now), Map.of())
            .list(new OnlineUserQuery());

        OnlineUserResp resp = result.get(0);
        assertTrue(resp.getOnline());
        assertTrue(resp.getActiveTimeExact());
        assertEquals(this.toLocalDateTime(now), resp.getLastActiveTime());
    }

    @Test
    void shouldMarkIdleExpiredWhenExceedingClientIdleTimeout() {
        long now = System.currentTimeMillis();
        SessionQueryService sessionQueryService = mock(SessionQueryService.class);
        SessionView session = this.session("session-1", now - ONE_DAY_MILLIS, now
            + ONE_DAY_MILLIS);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(session));

        // 闲置阈值（2 分钟）比在线展示窗口（5 分钟）还短：最后一个活跃点仍在窗口内，
        // 但已经闲置超时，此时不能再标记为在线，否则「在线」与「闲置超时待失效」会同时成立。
        long lastActive = now - 130_000L;
        List<OnlineUserResp> result = this.service(sessionQueryService, Map
            .of("session-1", lastActive),
            Map
                .of("web", new ClientPolicyCache.ClientPolicy(120L, 2_592_000L)))
            .list(new OnlineUserQuery());

        OnlineUserResp resp = result.get(0);
        assertTrue(resp.getIdleExpired());
        assertFalse(resp.getOnline());
        assertEquals(this.toLocalDateTime(lastActive + 120 * 1000L), resp.getIdleExpireTime());
    }

    @Test
    void shouldFilterByOnlineFlag() {
        long now = System.currentTimeMillis();
        SessionQueryService sessionQueryService = mock(SessionQueryService.class);
        SessionView online = this.session("session-online", now - 60_000, now + ONE_DAY_MILLIS);
        SessionView offline = this.session("session-offline", now - ONE_DAY_MILLIS, now
            + ONE_DAY_MILLIS);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(online, offline));

        OnlineUserServiceImpl service = this.service(sessionQueryService, Map
            .of("session-online", now), Map.of());
        OnlineUserQuery query = new OnlineUserQuery();
        query.setOnline(true);
        List<OnlineUserResp> onlineOnly = service.list(query);
        query.setOnline(false);
        List<OnlineUserResp> offlineOnly = service.list(query);

        assertEquals(1, onlineOnly.size());
        assertEquals("session-online", onlineOnly.get(0).getSessionId());
        assertEquals(1, offlineOnly.size());
        assertEquals("session-offline", offlineOnly.get(0).getSessionId());
    }

    @Test
    void shouldHideSessionAfterAbsoluteExpiry() {
        long now = System.currentTimeMillis();
        SessionQueryService sessionQueryService = mock(SessionQueryService.class);
        SessionView expired = this.session("session-expired", now - ONE_DAY_MILLIS, now - 1);
        when(sessionQueryService.listSessions(null)).thenReturn(List.of(expired));

        List<OnlineUserResp> result = this.service(sessionQueryService, Map.of(), Map.of())
            .list(new OnlineUserQuery());

        assertTrue(result.isEmpty());
    }

    private OnlineUserServiceImpl service(SessionQueryService sessionQueryService,
        Map<String, Long> lastActiveTimes,
        Map<String, ClientPolicyCache.ClientPolicy> policies) {
        SessionActivityService sessionActivityService = mock(SessionActivityService.class);
        when(sessionActivityService.getLastActiveTimes(anyCollection()))
            .thenReturn(lastActiveTimes);
        ClientPolicyCache clientPolicyCache = mock(ClientPolicyCache.class);
        when(clientPolicyCache.all()).thenReturn(policies);
        return new OnlineUserServiceImpl(mock(SessionInvalidationService.class),
            sessionQueryService, sessionActivityService, clientPolicyCache,
            new OnlineUserProperties());
    }

    private SessionView session(String sessionId, long createdAt, long expiresAt) {
        RefreshSession refreshSession = new RefreshSession();
        refreshSession.setSessionId(sessionId);
        refreshSession.setUserId(1L);
        refreshSession.setUsername("tester");
        refreshSession.setNickname("Tester");
        refreshSession.setClientId("web");
        refreshSession.setClientType("WEB");
        refreshSession.setCreatedAt(createdAt);
        refreshSession.setLastRefreshAt(createdAt);
        refreshSession.setExpiresAt(expiresAt);
        return SessionView.from(refreshSession);
    }

    private LocalDateTime toLocalDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
}
