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

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.continew.admin.auth.config.RefreshTokenProperties;
import top.continew.admin.auth.enums.LogoutReasonEnum;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;
import top.continew.admin.auth.model.AuthSecurityVersion;
import top.continew.admin.auth.model.RefreshClientPolicy;
import top.continew.admin.auth.model.RefreshRotationResult;
import top.continew.admin.auth.model.RefreshSession;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.api.AuthSessionConstants;
import top.continew.admin.auth.enums.SessionReplacementScope;
import top.continew.admin.auth.service.impl.RefreshTokenServiceImpl;
import top.continew.admin.auth.service.RefreshTokenService.LoginAttempt;
import top.continew.admin.auth.support.AccessSessionCache;
import top.continew.admin.auth.support.RefreshSessionStore;
import top.continew.admin.auth.support.RefreshTokenCodec;
import top.continew.admin.auth.support.RefreshTokenRequestGuard;
import top.continew.admin.auth.support.AuthPolicyLock;
import top.continew.admin.auth.support.RefreshTokenCodec.IssuedToken;
import top.continew.admin.auth.api.AuthSessionRevocationNotifier;
import top.continew.admin.auth.exception.RefreshTokenException;
import top.continew.starter.cache.redisson.util.RedisLockUtils;
import top.continew.starter.core.exception.BusinessException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Refresh Token 轮换、重放和传输边界测试。 */
class RefreshTokenServiceImplTest {

    private final Map<String, RefreshRotationResult> rotations = new HashMap<>();
    private final Map<String, RefreshRotationResult> latestRotations = new HashMap<>();
    private RefreshSessionStore sessionStore;
    private RefreshTokenProperties properties;
    private RefreshTokenCodec codec;
    private RefreshTokenServiceImpl service;
    private AuthSessionRevocationNotifier sessionRevocationNotifier;
    private AccessSessionCache accessSessionCache;
    private RefreshSession session;
    private IssuedToken initialToken;

    @BeforeEach
    void setUp() {
        properties = new RefreshTokenProperties();
        properties.setSecret("test-only-refresh-token-secret-with-32-bytes");
        properties.setIpRateLimit(0);
        properties.setSessionRateLimit(10);
        properties.setRotationGracePeriod(5);
        codec = new RefreshTokenCodec(properties);

        sessionStore = mock(RefreshSessionStore.class);
        RedisLockUtils sessionLock = mock(RedisLockUtils.class);
        AuthPolicyLock policyLock = mock(AuthPolicyLock.class);
        when(sessionStore.lockSession(anyString())).thenReturn(sessionLock);
        when(sessionStore.lockUserPolicy(anyLong())).thenReturn(policyLock);
        when(sessionStore.lockTenantPolicyRead(anyLong())).thenReturn(policyLock);
        when(sessionStore.lockTenantPolicyWrite(anyLong())).thenReturn(policyLock);
        when(sessionStore.lockClientPolicyRead(anyString())).thenReturn(policyLock);
        when(sessionStore.lockClientPolicyWrite(anyString())).thenReturn(policyLock);
        when(sessionStore.tryAcquireRateLimit(anyString(), anyInt(), any(Duration.class)))
            .thenReturn(true);
        when(sessionStore.getSecurityVersion(anyLong(), anyString(), anyLong()))
            .thenReturn(new AuthSecurityVersion(0, 0, 0));
        when(sessionStore.isSecurityVersionCurrent(any(RefreshSession.class))).thenReturn(true);
        when(sessionStore.getRotation(anyString())).thenAnswer(invocation -> rotations
            .get(invocation.getArgument(0, String.class)));
        org.mockito.Mockito.doAnswer(invocation -> {
            rotations.put(invocation.getArgument(0, String.class),
                invocation.getArgument(1, RefreshRotationResult.class));
            return null;
        }).when(sessionStore).saveRotation(anyString(), any(RefreshRotationResult.class),
            anyLong());
        when(sessionStore.getLatestRotation(anyString())).thenAnswer(invocation -> latestRotations
            .get(invocation.getArgument(0, String.class)));
        org.mockito.Mockito.doAnswer(invocation -> {
            latestRotations.put(invocation.getArgument(0, String.class),
                invocation.getArgument(1, RefreshRotationResult.class));
            return null;
        }).when(sessionStore).saveLatestRotation(anyString(), any(RefreshRotationResult.class),
            anyLong());

        initialToken = codec.issue(codec.newSessionId());
        session = new RefreshSession();
        session.setSessionId(initialToken.sessionId());
        session.setUserId(1L);
        session.setClientId("web");
        session.setMode(RefreshTokenModeEnum.BODY);
        session.setExpiresAt(System.currentTimeMillis() + Duration.ofDays(1).toMillis());
        session.setCurrentTokenFingerprint(initialToken.fingerprint());
        when(sessionStore.get(initialToken.sessionId())).thenAnswer(invocation -> session);

        sessionRevocationNotifier = mock(AuthSessionRevocationNotifier.class);
        accessSessionCache = mock(AccessSessionCache.class);
        RefreshTokenRequestGuard requestGuard = new RefreshTokenRequestGuard(properties, codec,
            sessionStore);
        service = new RefreshTokenServiceImpl(properties, codec, sessionStore, requestGuard,
            sessionRevocationNotifier, accessSessionCache);
    }

    @Test
    void shouldRotateAndReplayExactlyTheSameResult() {
        LoginResp first = service.rotate(initialToken.rawToken(), response(),
            issuer("access-1"));

        assertEquals("access-1", first.getAccessToken());
        assertEquals(initialToken.sessionId(), codec.parse(first.getRefreshToken()).sessionId());
        assertNotEquals(initialToken.rawToken(), first.getRefreshToken());
        assertEquals(initialToken.fingerprint(), session.getPreviousTokenFingerprint());
        assertTrue(codec.matches(codec.parse(first.getRefreshToken()).fingerprint(),
            session.getCurrentTokenFingerprint()));
        LoginResp replay =
            service.rotate(initialToken.rawToken(), response(), ignored -> {
                throw new AssertionError("幂等重放不应再次签发 Access Token");
            });
        assertEquals(first.getAccessToken(), replay.getAccessToken());
        assertEquals(first.getRefreshToken(), replay.getRefreshToken());
        verify(sessionStore, times(1)).tryAcquireRateLimit(anyString(), anyInt(),
            any(Duration.class));
    }

    @Test
    void shouldAcquirePolicyLocksBeforeSessionLockWhenRotating() {
        session.setTenantId(2L);

        service.rotate(initialToken.rawToken(), response(), issuer("access-1"));

        InOrder order = inOrder(sessionStore);
        order.verify(sessionStore).lockUserPolicy(1L);
        order.verify(sessionStore).lockTenantPolicyRead(2L);
        order.verify(sessionStore).lockClientPolicyRead("web");
        order.verify(sessionStore).lockSession(initialToken.sessionId());
    }

    @Test
    void shouldNotAdvanceAnotherGenerationInsideGracePeriod() {
        LoginResp first = service.rotate(initialToken.rawToken(), response(), issuer("access-1"));

        LoginResp replay = service.rotate(first.getRefreshToken(), response(), ignored -> {
            throw new AssertionError("宽限期内不应再次签发 Access Token");
        });

        assertEquals(first.getAccessToken(), replay.getAccessToken());
        assertEquals(first.getRefreshToken(), replay.getRefreshToken());
        verify(sessionStore, times(1)).tryAcquireRateLimit(anyString(), anyInt(),
            any(Duration.class));
    }

    @Test
    void shouldNotRevokeSessionForUnknownSecret() {
        IssuedToken unknown = codec.issue(initialToken.sessionId());

        assertThrows(BusinessException.class,
            () -> service.rotate(unknown.rawToken(), response(), issuer("unused")));

        verify(sessionStore, never()).lockSession(anyString());
        verify(sessionStore, never()).save(session);
        verify(sessionStore, never()).delete(session.getSessionId());
        verify(sessionStore, never()).removeIndexes(session);
    }

    @Test
    void shouldRejectOldGenerationTokenWhenSessionAlreadyRevoked() {
        // R0→R1→R2 后会话被撤销（强退/顶人），R0 指纹的轮换快照仍在宽限期内残留。
        // 凭证已无对应会话，必须按无效令牌处理，不能进入候选会话空指针。
        rotations.put(initialToken.fingerprint(), new RefreshRotationResult());
        when(sessionStore.get(initialToken.sessionId())).thenReturn(null);

        RefreshTokenException exception = assertThrows(RefreshTokenException.class,
            () -> service.rotate(initialToken.rawToken(), response(), issuer("unused")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(sessionStore, never()).lockSession(anyString());
        verify(sessionStore, never()).lockUserPolicy(anyLong());
    }

    @Test
    void shouldRevokePreviousTokenWhenGraceSnapshotIsGone() {
        service.rotate(initialToken.rawToken(), response(), issuer("access-1"));
        rotations.clear();
        latestRotations.clear();
        // 超出宽限期的明确重放必须直接撤销，不能被已耗尽的刷新配额挡住。
        when(sessionStore.tryAcquireRateLimit(anyString(), anyInt(), any(Duration.class)))
            .thenReturn(false);

        assertThrows(BusinessException.class,
            () -> service.rotate(initialToken.rawToken(), response(), issuer("unused")));

        verify(sessionStore).delete(session.getSessionId());
        verify(sessionStore).removeIndexes(session);
        verify(sessionStore, times(1)).tryAcquireRateLimit(anyString(), anyInt(),
            any(Duration.class));
    }

    @Test
    void shouldPreserveSessionAndResumeSameRotationAfterTemporaryFailure() {
        RuntimeException failure = new RuntimeException("temporary issuer failure");

        assertThrows(RuntimeException.class,
            () -> service.rotate(initialToken.rawToken(), response(), ignored -> {
                throw failure;
            }));

        verify(sessionStore, never()).delete(session.getSessionId());
        RefreshRotationResult pending = rotations.get(initialToken.fingerprint());
        assertTrue(pending != null && !pending.isComplete());
        String pendingRefreshToken = codec.decrypt(pending.getEncryptedRefreshToken());

        LoginResp recovered = service.rotate(initialToken.rawToken(), response(),
            issuer("access-recovered"));

        assertEquals("access-recovered", recovered.getAccessToken());
        assertEquals(pendingRefreshToken, recovered.getRefreshToken());
    }

    @Test
    void shouldRevokeWhenSecurityVersionChangesDuringRefresh() {
        when(sessionStore.isSecurityVersionCurrent(session)).thenReturn(true, false);

        RefreshTokenException exception = assertThrows(RefreshTokenException.class,
            () -> service.rotate(initialToken.rawToken(), response(), issuer("access-1")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(sessionStore).delete(session.getSessionId());
        verify(sessionStore).removeIndexes(session);
    }

    @Test
    void shouldRejectCookieAndBodyConflict() {
        MockHttpServletRequest request = request();
        request.setCookies(new Cookie("refresh_token", initialToken.rawToken()));

        RefreshTokenException exception = assertThrows(RefreshTokenException.class,
            () -> service.resolve(initialToken.rawToken(), request));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void shouldRequireTrustedOriginForCookieMode() {
        session.setMode(RefreshTokenModeEnum.COOKIE);
        MockHttpServletRequest request = request();
        request.setCookies(new Cookie("refresh_token", initialToken.rawToken()));
        request.addHeader("Origin", "https://attacker.example");

        assertThrows(BusinessException.class,
            () -> service.validateRequest(initialToken.rawToken(), request));

        request.removeHeader("Origin");
        request.addHeader("Origin", "https://admin.example");
        service.validateRequest(initialToken.rawToken(), request);
    }

    @Test
    void shouldValidateCookieOriginBeforeParsingMalformedToken() {
        MockHttpServletRequest request = request();
        request.setCookies(new Cookie("refresh_token", "malformed"));
        request.addHeader("Origin", "https://admin.example");

        service.validateCookieOrigin(request);
        assertThrows(BusinessException.class,
            () -> service.validateRequest("malformed", request));

        request.removeHeader("Origin");
        request.addHeader("Origin", "https://attacker.example");
        RefreshTokenException exception = assertThrows(RefreshTokenException.class,
            () -> service.validateCookieOrigin(request));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void shouldRequireTrustedOriginForCachedOlderCookieToken() {
        session.setMode(RefreshTokenModeEnum.COOKIE);
        session.setCurrentTokenFingerprint(codec.issue(session.getSessionId()).fingerprint());
        RefreshRotationResult cached = new RefreshRotationResult();
        cached.setEncryptedAccessToken(codec.encrypt("access-1"));
        rotations.put(initialToken.fingerprint(), cached);
        MockHttpServletRequest request = request();
        request.setCookies(new Cookie("refresh_token", initialToken.rawToken()));
        request.addHeader("Origin", "https://attacker.example");

        assertThrows(BusinessException.class,
            () -> service.validateRequest(initialToken.rawToken(), request));
    }

    @Test
    void shouldRejectWildcardCookieOriginConfiguration() {
        properties.setCookieAllowedOrigins(List.of("*"));

        assertFalse(properties.isCookieAllowedOriginsValid());
    }

    @Test
    void shouldAllowCookieOriginWildcardForConfiguredSubdomains() {
        properties.setCookieAllowedOrigins(List.of("http://*.luoqiz.top"));
        assertTrue(properties.isCookieAllowedOriginsValid());
        RefreshTokenRequestGuard requestGuard = new RefreshTokenRequestGuard(properties, codec,
            sessionStore);
        MockHttpServletRequest request = request();
        request.setCookies(new Cookie("refresh_token", initialToken.rawToken()));
        request.addHeader("Origin", "http://admin.luoqiz.top");

        assertDoesNotThrow(() -> requestGuard.validateCookieOrigin(request));

        request.removeHeader("Origin");
        request.addHeader("Origin", "http://admin.luoqiz.top.attacker.example");
        assertThrows(RefreshTokenException.class, () -> requestGuard.validateCookieOrigin(request));

        request.removeHeader("Origin");
        request.addHeader("Origin", "http://nested.admin.luoqiz.top");
        assertThrows(RefreshTokenException.class, () -> requestGuard.validateCookieOrigin(request));
    }

    @Test
    void shouldRevokeRefreshSessionEvenWhenAccessTokenIndexAlreadyExpired() {
        RefreshSession oldSession = session("old-web", "WEB", 1L);
        when(sessionStore.findByUser(1L)).thenReturn(Set.of(oldSession.getSessionId()));
        when(sessionStore.get(oldSession.getSessionId())).thenReturn(oldSession);

        RefreshClientPolicy client = client(false, SessionReplacementScope.ALL_CLIENT_TYPES, -1);
        String result = service.executeLoginPolicy(1L, client.clientId(), 2L,
            version -> new LoginAttempt<>(1L, client, null, null, () -> "issued"));

        assertEquals("issued", result);
        verify(sessionStore).delete(oldSession.getSessionId());
    }

    @Test
    void shouldOnlyRevokeSameClientTypeForCurrentDevicePolicy() {
        RefreshSession webSession = session("old-web", "WEB", 1L);
        RefreshSession appSession = session("old-app", "APP", 2L);
        when(sessionStore.findByUser(1L))
            .thenReturn(Set.of(webSession.getSessionId(), appSession.getSessionId()));
        when(sessionStore.get(webSession.getSessionId())).thenReturn(webSession);
        when(sessionStore.get(appSession.getSessionId())).thenReturn(appSession);

        RefreshClientPolicy client = client(false, SessionReplacementScope.CURRENT_CLIENT_TYPE, -1);
        service.executeLoginPolicy(1L, client.clientId(), 2L,
            version -> new LoginAttempt<>(1L, client, null, null, () -> null));

        verify(sessionStore).delete(webSession.getSessionId());
        verify(sessionStore, never()).delete(appSession.getSessionId());
    }

    @Test
    void shouldApplyMaxLoginCountOnlyToCurrentClientType() {
        RefreshSession oldest = session("oldest", "WEB", 1L);
        RefreshSession newest = session("newest", "WEB", 2L);
        RefreshSession app = session("app", "APP", 3L);
        when(sessionStore.findByUser(1L))
            .thenReturn(Set.of(newest.getSessionId(), oldest.getSessionId(), app.getSessionId()));
        when(sessionStore.get(oldest.getSessionId())).thenReturn(oldest);
        when(sessionStore.get(newest.getSessionId())).thenReturn(newest);
        when(sessionStore.get(app.getSessionId())).thenReturn(app);

        RefreshClientPolicy client = client(true, null, 2);
        service.executeLoginPolicy(1L, client.clientId(), 2L,
            version -> new LoginAttempt<>(1L, client, null, null, () -> null));

        verify(sessionStore).delete(oldest.getSessionId());
        verify(sessionStore, never()).delete(newest.getSessionId());
        verify(sessionStore, never()).delete(app.getSessionId());
    }

    @Test
    void shouldAcquireLoginLocksInFixedOrderAndReleaseInReverseOrder() {
        AuthPolicyLock userLock = mock(AuthPolicyLock.class);
        AuthPolicyLock tenantLock = mock(AuthPolicyLock.class);
        AuthPolicyLock clientLock = mock(AuthPolicyLock.class);
        when(sessionStore.lockUserPolicy(1L)).thenReturn(userLock);
        when(sessionStore.lockTenantPolicyRead(2L)).thenReturn(tenantLock);
        when(sessionStore.lockClientPolicyRead("web")).thenReturn(clientLock);

        RefreshClientPolicy client = client(true, null, -1);
        String result = service.executeLoginPolicy(1L, "web", 2L,
            version -> new LoginAttempt<>(1L, client, null, null, () -> "issued"));

        assertEquals("issued", result);
        org.mockito.InOrder order = inOrder(sessionStore, userLock, tenantLock, clientLock);
        order.verify(sessionStore).lockUserPolicy(1L);
        order.verify(sessionStore).lockTenantPolicyRead(2L);
        order.verify(sessionStore).lockClientPolicyRead("web");
        order.verify(clientLock).close();
        order.verify(tenantLock).close();
        order.verify(userLock).close();
    }

    @Test
    void shouldHoldInvalidationLockUntilTransactionCompletion() {
        AuthPolicyLock userLock = mock(AuthPolicyLock.class);
        RefreshSession oldSession = session("old-web", "WEB", 1L);
        when(sessionStore.lockUserPolicy(1L)).thenReturn(userLock);
        when(sessionStore.findByUser(1L)).thenReturn(Set.of(oldSession.getSessionId()));
        when(sessionStore.get(oldSession.getSessionId())).thenReturn(oldSession);

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.revokeByUser(1L);

            verify(sessionStore, times(1)).incrementUserSecurityVersion(1L);
            verify(userLock, never()).close();
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager
                .getSynchronizations()) {
                synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            }
            verify(userLock).close();
            // 不再依赖 afterCommit 二次执行 Redis 失效操作。
            verify(sessionStore, times(1)).incrementUserSecurityVersion(1L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void shouldFailClosedWhenTransactionSynchronizationIsUnavailable() {
        AuthPolicyLock userLock = mock(AuthPolicyLock.class);
        when(sessionStore.lockUserPolicy(1L)).thenReturn(userLock);
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            assertThrows(IllegalStateException.class, () -> service.revokeByUser(1L));

            verify(sessionStore, never()).incrementUserSecurityVersion(1L);
            verify(userLock).close();
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void shouldNotifyWebSocketClusterWhenSessionIsRevoked() {
        RefreshSession oldSession = session("old-web", "WEB", 1L);
        when(sessionStore.findByUser(1L)).thenReturn(Set.of(oldSession.getSessionId()));
        when(sessionStore.get(oldSession.getSessionId())).thenReturn(oldSession);

        service.revokeByUser(1L);

        verify(sessionRevocationNotifier).notifyRevoked(oldSession.getSessionId());
        // 用户级强制下线在 Redis 中记录失效原因，让被踢方下次请求看到准确提示。
        verify(sessionStore).saveLogoutReason(oldSession.getSessionId(), LogoutReasonEnum.KICKOUT);
    }

    @Test
    void shouldSkipSessionStoreWhenAccessSessionCacheHit() {
        String sessionId = initialToken.sessionId();
        when(accessSessionCache.isValid(sessionId)).thenReturn(true);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getExtra("access-token-1",
                AuthSessionConstants.SESSION_ID_CLAIM)).thenReturn(sessionId);
            assertNull(service.getInvalidReason("access-token-1"));
        }
        verify(sessionStore, never()).get(anyString());
    }

    @Test
    void shouldMarkValidAfterFullValidation() {
        String sessionId = initialToken.sessionId();
        when(accessSessionCache.isValid(sessionId)).thenReturn(false);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getExtra("access-token-1",
                AuthSessionConstants.SESSION_ID_CLAIM)).thenReturn(sessionId);
            stpUtil.when(() -> StpUtil.getLoginIdByToken("access-token-1")).thenReturn(1L);
            assertNull(service.getInvalidReason("access-token-1"));
        }
        verify(sessionStore).get(sessionId);
        verify(accessSessionCache).markValid(sessionId);
    }

    @Test
    void shouldInvalidateAccessSessionCacheWhenSessionRevoked() {
        service.revokeBySessionId(initialToken.sessionId());
        verify(accessSessionCache).invalidate(initialToken.sessionId());
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("admin.example");
        request.setServerPort(443);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }

    private RefreshSession session(String sessionId, String clientType, long createdAt) {
        RefreshSession value = new RefreshSession();
        value.setSessionId(sessionId);
        value.setUserId(1L);
        value.setClientId(clientType.toLowerCase());
        value.setClientType(clientType);
        value.setCreatedAt(createdAt);
        value.setExpiresAt(System.currentTimeMillis() + Duration.ofDays(1).toMillis());
        return value;
    }

    private RefreshClientPolicy client(boolean concurrent, SessionReplacementScope replacementScope,
        int maxLoginCount) {
        return new RefreshClientPolicy("web", "WEB", 2592000L,
            RefreshTokenModeEnum.COOKIE, concurrent, replacementScope, maxLoginCount,
            LogoutReasonEnum.REPLACED);
    }

    private MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }

    private Function<RefreshSession, LoginResp> issuer(String accessToken) {
        return ignored -> LoginResp.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(900L)
            .tenantId(1L)
            .build();
    }
}
