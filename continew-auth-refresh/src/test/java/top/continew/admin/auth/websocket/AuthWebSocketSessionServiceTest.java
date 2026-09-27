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

package top.continew.admin.auth.websocket;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import top.continew.admin.auth.api.AuthSessionConstants;
import top.continew.admin.auth.api.AccessSessionValidator;
import top.continew.admin.auth.config.RefreshTokenProperties;
import top.continew.starter.messaging.websocket.dao.WebSocketSessionDao;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Refresh Session 撤销与 WebSocket 连接联动测试。 */
class AuthWebSocketSessionServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldCloseLocalConnectionAndPublishClusterRevocation() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RTopic topic = mock(RTopic.class);
        ObjectProvider<WebSocketSessionDao> sessionDaoProvider = mock(ObjectProvider.class);
        ObjectProvider<AccessSessionValidator> authSessionApiProvider = mock(ObjectProvider.class);
        WebSocketSessionDao sessionDao = mock(WebSocketSessionDao.class);
        WebSocketSession webSocketSession = mock(WebSocketSession.class);
        when(redissonClient.getTopic(anyString())).thenReturn(topic);
        when(topic.addListener(eq(String.class), any(MessageListener.class))).thenReturn(1);
        when(sessionDaoProvider.getIfAvailable()).thenReturn(sessionDao);
        when(sessionDao.listAllSessionIds()).thenReturn(Set.of("access-token"));
        when(sessionDao.get("access-token")).thenReturn(webSocketSession);
        when(webSocketSession.isOpen()).thenReturn(true);

        AuthWebSocketSessionService service = this.service(redissonClient, sessionDaoProvider,
            authSessionApiProvider);
        service.subscribe();
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getExtra("access-token",
                AuthSessionConstants.SESSION_ID_CLAIM)).thenReturn("session-1");

            service.notifyRevoked("session-1");
        } finally {
            service.unsubscribe();
        }

        verify(webSocketSession).close(CloseStatus.POLICY_VIOLATION);
        verify(sessionDao).delete("access-token");
        verify(topic).publish("session-1");
        verify(topic).removeListener(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldStillCloseLocalConnectionWhenClusterPublishFails() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RTopic topic = mock(RTopic.class);
        ObjectProvider<WebSocketSessionDao> sessionDaoProvider = mock(ObjectProvider.class);
        ObjectProvider<AccessSessionValidator> authSessionApiProvider = mock(ObjectProvider.class);
        WebSocketSessionDao sessionDao = mock(WebSocketSessionDao.class);
        WebSocketSession webSocketSession = mock(WebSocketSession.class);
        when(redissonClient.getTopic(anyString())).thenReturn(topic);
        when(topic.addListener(eq(String.class), any(MessageListener.class))).thenReturn(1);
        when(topic.publish("session-1")).thenThrow(new IllegalStateException("Redis unavailable"));
        when(sessionDaoProvider.getIfAvailable()).thenReturn(sessionDao);
        when(sessionDao.listAllSessionIds()).thenReturn(Set.of("access-token"));
        when(sessionDao.get("access-token")).thenReturn(webSocketSession);
        when(webSocketSession.isOpen()).thenReturn(true);

        AuthWebSocketSessionService service = this.service(redissonClient, sessionDaoProvider,
            authSessionApiProvider);
        service.subscribe();
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getExtra("access-token",
                AuthSessionConstants.SESSION_ID_CLAIM)).thenReturn("session-1");

            service.notifyRevoked("session-1");
        } finally {
            service.unsubscribe();
        }

        verify(webSocketSession).close(CloseStatus.POLICY_VIOLATION);
        verify(sessionDao).delete("access-token");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCloseOnlyInvalidConnectionsDuringPeriodicValidation() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        ObjectProvider<WebSocketSessionDao> sessionDaoProvider = mock(ObjectProvider.class);
        ObjectProvider<AccessSessionValidator> authSessionApiProvider = mock(ObjectProvider.class);
        WebSocketSessionDao sessionDao = mock(WebSocketSessionDao.class);
        AccessSessionValidator authSessionApi = mock(AccessSessionValidator.class);
        WebSocketSession invalidSession = mock(WebSocketSession.class);
        when(sessionDaoProvider.getIfAvailable()).thenReturn(sessionDao);
        when(authSessionApiProvider.getIfAvailable()).thenReturn(authSessionApi);
        when(sessionDao.listAllSessionIds()).thenReturn(Set.of("invalid-token", "valid-token"));
        when(authSessionApi.isInvalid("invalid-token")).thenReturn(true);
        when(authSessionApi.isInvalid("valid-token")).thenReturn(false);
        when(sessionDao.get("invalid-token")).thenReturn(invalidSession);
        when(invalidSession.isOpen()).thenReturn(true);

        AuthWebSocketSessionService service = this.service(redissonClient, sessionDaoProvider,
            authSessionApiProvider);
        service.validateLocalSessions();

        verify(invalidSession).close(CloseStatus.POLICY_VIOLATION);
        verify(sessionDao).delete("invalid-token");
        verify(sessionDao, never()).delete("valid-token");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCloseAllConnectionsOfSameTokenViaMultiDao() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        ObjectProvider<WebSocketSessionDao> sessionDaoProvider = mock(ObjectProvider.class);
        ObjectProvider<AccessSessionValidator> authSessionApiProvider = mock(ObjectProvider.class);
        MultiWebSocketSessionDao sessionDao = mock(MultiWebSocketSessionDao.class);
        WebSocketSession firstTab = mock(WebSocketSession.class);
        WebSocketSession secondTab = mock(WebSocketSession.class);
        when(sessionDaoProvider.getIfAvailable()).thenReturn(sessionDao);
        when(sessionDao.listAllSessionIds()).thenReturn(Set.of("access-token"));
        when(sessionDao.listByKey("access-token")).thenReturn(List.of(firstTab, secondTab));
        when(firstTab.isOpen()).thenReturn(true);
        when(secondTab.isOpen()).thenReturn(true);

        AuthWebSocketSessionService service = this.service(redissonClient, sessionDaoProvider,
            authSessionApiProvider);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getExtra("access-token",
                AuthSessionConstants.SESSION_ID_CLAIM)).thenReturn("session-1");

            service.notifyRevoked("session-1");
        }

        verify(firstTab).close(CloseStatus.POLICY_VIOLATION);
        verify(secondTab).close(CloseStatus.POLICY_VIOLATION);
        verify(sessionDao).removeAll("access-token");
        verify(sessionDao, never()).delete(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReuseLocalSessionCacheAcrossBatchRevocations() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        ObjectProvider<WebSocketSessionDao> sessionDaoProvider = mock(ObjectProvider.class);
        ObjectProvider<AccessSessionValidator> authSessionApiProvider = mock(ObjectProvider.class);
        WebSocketSessionDao sessionDao = mock(WebSocketSessionDao.class);
        WebSocketSession sessionA = mock(WebSocketSession.class);
        WebSocketSession sessionB = mock(WebSocketSession.class);
        WebSocketSession sessionC = mock(WebSocketSession.class);
        when(sessionDaoProvider.getIfAvailable()).thenReturn(sessionDao);
        // 首次撤销后 token-a 的连接被关闭并从 DAO 移除，后续扫描只剩 b、c。
        when(sessionDao.listAllSessionIds())
            .thenReturn(Set.of("token-a", "token-b", "token-c"),
                Set.of("token-b", "token-c"));
        when(sessionDao.get("token-a")).thenReturn(sessionA);
        when(sessionDao.get("token-b")).thenReturn(sessionB);
        when(sessionDao.get("token-c")).thenReturn(sessionC);
        when(sessionA.isOpen()).thenReturn(true);
        when(sessionB.isOpen()).thenReturn(true);
        when(sessionC.isOpen()).thenReturn(true);

        AuthWebSocketSessionService service = this.service(redissonClient, sessionDaoProvider,
            authSessionApiProvider);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getExtra("token-a",
                AuthSessionConstants.SESSION_ID_CLAIM)).thenReturn("session-a");
            stpUtil.when(() -> StpUtil.getExtra("token-b",
                AuthSessionConstants.SESSION_ID_CLAIM)).thenReturn("session-b");
            stpUtil.when(() -> StpUtil.getExtra("token-c",
                AuthSessionConstants.SESSION_ID_CLAIM)).thenReturn("session-c");

            // 首次撤销：三个 Token 的会话声明全部冷启动回源并写入本地索引。
            service.notifyRevoked("session-a");
            verify(sessionA).close(CloseStatus.POLICY_VIOLATION);
            verify(sessionB, never()).close(any());
            stpUtil.verify(() -> StpUtil.getExtra(anyString(),
                anyString()), times(3));

            // 第二次撤销：token-b/token-c 命中本地索引，不再回源 Redis。
            service.notifyRevoked("session-b");
            verify(sessionB).close(CloseStatus.POLICY_VIOLATION);
            stpUtil.verify(() -> StpUtil.getExtra(anyString(),
                anyString()), times(3));
        }
    }

    private AuthWebSocketSessionService service(RedissonClient redissonClient,
        ObjectProvider<WebSocketSessionDao> sessionDaoProvider,
        ObjectProvider<AccessSessionValidator> accessSessionValidatorProvider) {
        return new AuthWebSocketSessionService(redissonClient, sessionDaoProvider,
            accessSessionValidatorProvider, new RefreshTokenProperties());
    }

    @Test
    void daoShouldKeepOtherTabsWhenOneConnectionCloses() throws Exception {
        ConcurrentWebSocketSessionDao dao = new ConcurrentWebSocketSessionDao();
        WebSocketSession closed = mock(WebSocketSession.class);
        WebSocketSession alive = mock(WebSocketSession.class);
        when(closed.getId()).thenReturn("conn-1");
        when(alive.getId()).thenReturn("conn-2");
        when(closed.isOpen()).thenReturn(false);
        when(alive.isOpen()).thenReturn(true);

        dao.add("access-token", closed);
        dao.add("access-token", alive);
        List<WebSocketSession> registered = List.copyOf(dao.listByKey("access-token"));
        assertEquals(2, registered.size());
        assertTrue(registered.containsAll(List.of(closed, alive)));
        assertEquals(alive, dao.get("access-token"));
        assertEquals(Set.of("access-token"), dao.listAllSessionIds());

        // Starter 关闭回调只携带 Key：只移除已关闭的连接，保留存活的标签页。
        dao.delete("access-token");
        assertEquals(List.of(alive), dao.listByKey("access-token"));
        assertEquals(alive, dao.get("access-token"));

        dao.removeAll("access-token");
        assertEquals(List.of(), dao.listByKey("access-token"));
        assertNull(dao.get("access-token"));
    }
}
