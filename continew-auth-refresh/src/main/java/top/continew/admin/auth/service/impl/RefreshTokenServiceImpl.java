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

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
import top.continew.admin.auth.service.RefreshTokenService;
import top.continew.admin.auth.service.RefreshTokenService.LoginAttempt;
import top.continew.admin.auth.support.AccessSessionCache;
import top.continew.admin.auth.support.RefreshSessionStore;
import top.continew.admin.auth.support.RefreshTokenCodec;
import top.continew.admin.auth.support.RefreshTokenRequestGuard;
import top.continew.admin.auth.support.AuthPolicyLock;
import top.continew.admin.auth.support.RefreshTokenCodec.IssuedToken;
import top.continew.admin.auth.support.RefreshTokenCodec.ParsedToken;
import top.continew.admin.auth.api.AuthSessionConstants;
import top.continew.admin.auth.api.AccessSessionValidator;
import top.continew.admin.auth.api.AuthSessionRevocationNotifier;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserExtraContext;
import top.continew.admin.auth.exception.RefreshTokenException;
import top.continew.admin.auth.enums.SessionReplacementScope;
import top.continew.starter.cache.redisson.util.RedisLockUtils;
import top.continew.starter.core.exception.BusinessException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 基于单 Session 状态的 Refresh Token 实现。
 *
 * <p>Refresh Token 使用 {@code sessionId.secret} 格式。轮换时只更新 Session 记录中的
 * current/previous 指纹，并用短时加密快照保证并发请求得到同一组 Token。未知 secret
 * 只会认证失败，不会撤销 Session，避免攻击者利用公开 sessionId 强制用户下线。</p>
 *
 * @author luoqiz
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService, AccessSessionValidator {

    private final RefreshTokenProperties properties;
    private final RefreshTokenCodec tokenCodec;
    private final RefreshSessionStore sessionStore;
    private final RefreshTokenRequestGuard requestGuard;
    private final AuthSessionRevocationNotifier sessionRevocationNotifier;
    private final AccessSessionCache accessSessionCache;

    @Override
    public long getRefreshTimeout(RefreshClientPolicy clientPolicy) {
        return clientPolicy.refreshTokenTimeout();
    }

    @Override
    public RefreshTokenModeEnum getMode(RefreshClientPolicy clientPolicy) {
        return clientPolicy.refreshTokenMode();
    }

    @Override
    public String newSessionId() {
        return tokenCodec.newSessionId();
    }

    @Override
    public <T> T executeLoginPolicy(Long userId, String clientId, Long tenantId,
        Function<AuthSecurityVersion, LoginAttempt<T>> attemptFactory) {
        List<Supplier<AuthPolicyLock>> lockSuppliers = new ArrayList<>(3);
        lockSuppliers.add(() -> sessionStore.lockUserPolicy(userId));
        if (tenantId != null) {
            lockSuppliers.add(() -> sessionStore.lockTenantPolicyRead(tenantId));
        }
        lockSuppliers.add(() -> sessionStore.lockClientPolicyRead(clientId));
        return this.executeWithLocks(lockSuppliers, () -> {
            AuthSecurityVersion securityVersion = sessionStore.getSecurityVersion(userId,
                clientId, tenantId);
            LoginAttempt<T> attempt = attemptFactory.apply(securityVersion);
            this.requireValidLoginAttempt(userId, clientId, attempt);
            this.applyLoginPolicy(attempt);
            return attempt.issuer().get();
        });
    }

    private <T> void applyLoginPolicy(LoginAttempt<T> attempt) {
        Long userId = attempt.userId();
        RefreshClientPolicy client = attempt.clientPolicy();
        // 浏览器新登录会覆盖现有 Cookie。无论客户端是否允许并发，都必须先撤销被
        // 覆盖的会话，避免服务端遗留一个客户端再也无法主动退出的长期凭证。
        Set<String> replacedSessionIds = new LinkedHashSet<>();
        String accessSessionId = this.findAccessSessionId(attempt.currentAccessToken());
        if (accessSessionId != null) {
            replacedSessionIds.add(accessSessionId);
        }
        String refreshSessionId = this.findAuthenticatedSessionId(attempt.currentRefreshToken());
        if (refreshSessionId != null) {
            replacedSessionIds.add(refreshSessionId);
        }
        this.revokeSessions(replacedSessionIds, LogoutReasonEnum.REPLACED);

        List<RefreshSession> activeSessions = this.listActiveSessions(userId);
        if (!client.concurrent()) {
            SessionReplacementScope replacedRange = client.replacementScope();
            if (replacedRange == null) {
                throw new BusinessException("客户端顶人下线范围配置无效");
            }
            Set<String> sessionIds = new LinkedHashSet<>();
            for (RefreshSession session : activeSessions) {
                if (SessionReplacementScope.ALL_CLIENT_TYPES.equals(replacedRange)
                    || Objects.equals(client.clientType(), session.getClientType())) {
                    sessionIds.add(session.getSessionId());
                }
            }
            this.revokeSessions(sessionIds, LogoutReasonEnum.REPLACED);
        } else {
            // Web、App 和小程序分别控制并发数量，避免某一端的登录挤掉其他端。
            List<RefreshSession> sameClientTypeSessions = activeSessions.stream()
                .filter(session -> Objects.equals(client.clientType(), session.getClientType()))
                .toList();
            this.evictOverflowSessions(sameClientTypeSessions, client.maxLoginCount(),
                client.overflowLogoutMode());
        }
    }

    private <T> void requireValidLoginAttempt(Long userId, String clientId,
        LoginAttempt<T> attempt) {
        if (attempt == null || attempt.clientPolicy() == null || attempt.issuer() == null
            || !Objects.equals(userId, attempt.userId())
            || !Objects.equals(clientId, attempt.clientPolicy().clientId())) {
            throw new IllegalStateException("登录状态复查结果与认证锁范围不一致");
        }
    }

    @Override
    public String issue(String sessionId, UserContext userContext, RefreshClientPolicy client,
        UserExtraContext extraContext, AuthSecurityVersion securityVersion,
        HttpServletResponse response, String accessToken, long accessTokenTimeout) {
        long now = System.currentTimeMillis();
        long refreshTimeout = this.getRefreshTimeout(client);
        long expiresAt = this.calculateExpiresAt(now, refreshTimeout);
        IssuedToken token = tokenCodec.issue(sessionId);
        RefreshSession session = this.newSession(token, userContext, client, extraContext,
            securityVersion, expiresAt);
        try (RedisLockUtils ignored = sessionStore.lockSession(sessionId)) {
            if (sessionStore.get(sessionId) != null) {
                throw new BusinessException("登录会话创建失败，请重新登录");
            }
            sessionStore.save(session);
            sessionStore.index(session);
            if (!sessionStore.isSecurityVersionCurrent(session)) {
                this.revokeSessionLocked(session, LogoutReasonEnum.KICKOUT);
                throw this.invalidToken();
            }
        } catch (Exception e) {
            this.revokeAfterFailure(sessionId);
            throw e;
        }
        requestGuard.disableCaching(response);
        if (RefreshTokenModeEnum.COOKIE.equals(session.getMode())) {
            requestGuard.writeCookie(response, token.rawToken(), refreshTimeout);
        }
        return token.rawToken();
    }

    @Override
    public LoginResp rotate(String rawRefreshToken, HttpServletResponse response,
        Function<RefreshSession, LoginResp> accessTokenIssuer) {
        ParsedToken presentedToken = tokenCodec.parse(rawRefreshToken);
        // 在进入 Session 串行化区之前先验证凭证是否可能有效。否则只知道公开 sid 的
        // 攻击者可用随机 secret 长时间争抢该 Session 的锁，影响合法用户刷新。
        RefreshSession candidateSession = sessionStore.get(presentedToken.sessionId());
        if (!this.isKnownToken(candidateSession, presentedToken.fingerprint())
            && sessionStore.getRotation(presentedToken.fingerprint()) == null) {
            throw this.invalidToken();
        }
        // 会话已撤销或过期时，旧代指纹的轮换快照可能仍在宽限期内残留（R0→R1→R2 后
        // 撤销会话只清理 current/previous 两代快照）。此时凭证已无对应会话，必须按
        // 无效令牌处理，否则下方 candidateSession.getUserId() 会触发空指针。
        if (candidateSession == null) {
            throw this.invalidToken();
        }
        List<Supplier<AuthPolicyLock>> lockSuppliers = new ArrayList<>(3);
        lockSuppliers.add(() -> sessionStore.lockUserPolicy(candidateSession.getUserId()));
        if (candidateSession.getTenantId() != null) {
            lockSuppliers
                .add(() -> sessionStore.lockTenantPolicyRead(candidateSession.getTenantId()));
        }
        lockSuppliers.add(() -> sessionStore.lockClientPolicyRead(candidateSession.getClientId()));
        return this.executeWithLocks(lockSuppliers,
            () -> this.rotateLocked(presentedToken, response, accessTokenIssuer));
    }

    private LoginResp rotateLocked(ParsedToken presentedToken, HttpServletResponse response,
        Function<RefreshSession, LoginResp> accessTokenIssuer) {
        try (RedisLockUtils ignored = sessionStore.lockSession(presentedToken.sessionId())) {
            RefreshSession session = this.requireActiveSession(presentedToken.sessionId());
            RefreshRotationResult cached = sessionStore.getRotation(
                presentedToken.fingerprint());
            boolean current = tokenCodec.matches(presentedToken.fingerprint(),
                session.getCurrentTokenFingerprint());
            boolean previous = tokenCodec.matches(presentedToken.fingerprint(),
                session.getPreviousTokenFingerprint());
            // 只有能够证明持有当前/上一代 Token，或命中服务端短时加密快照的请求，才消耗
            // Session 级配额，避免攻击者仅凭公开 sid 耗尽合法会话的刷新次数。
            if (!current && !previous && cached == null) {
                throw this.invalidToken();
            }
            // 宽限期内整个 Session 只允许前进一代。后续请求统一返回最新结果，避免
            // R0→R1→R2 后迟到的 R0 响应把客户端 Cookie 回退到已经失效的 R1。
            RefreshRotationResult latest = sessionStore.getLatestRotation(session.getSessionId());
            if (latest != null && latest.isComplete()
                && this.isCurrentRotationResult(session, latest)) {
                return this.replayRotation(latest, session.getMode(), response);
            }
            if (cached != null && cached.isComplete()) {
                if (this.isCurrentRotationResult(session, cached)) {
                    sessionStore.saveLatestRotation(session.getSessionId(), cached,
                        properties.getRotationGracePeriod());
                    return this.replayRotation(cached, session.getMode(), response);
                }
                // 旧快照的输出已经不是当前代，不能再把陈旧凭证返回给客户端。
                throw this.invalidToken();
            }
            if (previous && cached == null) {
                // 上一代合法 Token 的宽限期已经结束，属于明确重放。撤销不能被会话
                // 限流挡住，否则攻击者可先耗尽配额，再让被盗旧 Token 延迟触发失效。
                this.revokeSessionLocked(session, LogoutReasonEnum.KICKOUT);
                throw this.invalidToken();
            }
            // 完整幂等快照已经优先返回，只有真正产生新 Access Token 的轮换才消耗配额。
            requestGuard.checkSessionRateLimit(session.getSessionId());

            String newRefreshToken;
            if (current) {
                IssuedToken newToken = tokenCodec.issue(session.getSessionId());
                newRefreshToken = newToken.rawToken();
                // 先保存短时密文，再原子切换 Session。进程在任意一步退出，旧 Token 都能
                // 从快照恢复出同一个新 Token，不会产生客户端永远无法获得的会话状态。
                sessionStore.saveRotation(presentedToken.fingerprint(),
                    this.pendingRotation(newRefreshToken),
                    properties.getRotationGracePeriod());
                session.setPreviousTokenFingerprint(session.getCurrentTokenFingerprint());
                session.setCurrentTokenFingerprint(newToken.fingerprint());
                session.setLastRefreshAt(System.currentTimeMillis());
                sessionStore.save(session);
            } else if (previous && cached != null) {
                // 上次进程可能在 Session 已切换、Access Token 尚未签发时退出；从加密快照
                // 恢复完全相同的新 Refresh Token，并继续完成这一轮签发。
                newRefreshToken = tokenCodec.decrypt(cached.getEncryptedRefreshToken());
                this.requireCurrentToken(session, newRefreshToken);
            } else {
                // current/previous/cached 已在锁内完整分类，此分支仅保护未来改动不变量。
                throw this.invalidToken();
            }

            LoginResp loginResp = null;
            try {
                loginResp = accessTokenIssuer.apply(session);
                this.requireCurrentSecurityVersion(session);
                return this.completeRotationLocked(presentedToken, session, newRefreshToken,
                    loginResp, response);
            } catch (RefreshTokenException e) {
                if (loginResp != null && loginResp.getAccessToken() != null) {
                    this.kickoutQuietly(loginResp.getAccessToken());
                }
                if (HttpStatus.UNAUTHORIZED.equals(e.getStatus())) {
                    this.revokeSessionLocked(session, LogoutReasonEnum.KICKOUT);
                }
                throw e;
            } catch (Exception e) {
                if (loginResp != null && loginResp.getAccessToken() != null) {
                    this.kickoutQuietly(loginResp.getAccessToken());
                }
                // 基础设施临时故障不撤销仍然有效的 Session。保留待完成的新 Refresh
                // Token，客户端用旧 Token 重试时可继续同一轮换，而不是被迫重新登录。
                this.preservePendingRotation(presentedToken, session, newRefreshToken);
                throw e;
            }
        }
    }

    private LoginResp completeRotationLocked(ParsedToken presentedToken, RefreshSession session,
        String newRefreshToken, LoginResp loginResp, HttpServletResponse response) {
        long now = System.currentTimeMillis();
        this.requireCurrentToken(session, newRefreshToken);
        loginResp.setRefreshExpiresIn(this.remainingSeconds(session.getExpiresAt(), now));
        RefreshRotationResult completed = this.completedRotation(loginResp, newRefreshToken);
        sessionStore.saveLatestRotation(session.getSessionId(), completed,
            properties.getRotationGracePeriod());
        sessionStore.saveRotation(presentedToken.fingerprint(), completed,
            properties.getRotationGracePeriod());
        if (RefreshTokenModeEnum.BODY.equals(session.getMode())) {
            loginResp.setRefreshToken(newRefreshToken);
        } else {
            requestGuard.writeCookie(response, newRefreshToken, loginResp.getRefreshExpiresIn());
        }
        requestGuard.disableCaching(response);
        return loginResp;
    }

    @Override
    public String resolve(String bodyRefreshToken, HttpServletRequest request) {
        return requestGuard.resolve(bodyRefreshToken, request);
    }

    @Override
    public void checkRequestRateLimit(HttpServletRequest request) {
        requestGuard.checkRequestRateLimit(request);
    }

    @Override
    public void validateRequest(String rawRefreshToken, HttpServletRequest request) {
        requestGuard.validateRequest(rawRefreshToken, request);
    }

    @Override
    public void validateCookieOrigin(HttpServletRequest request) {
        requestGuard.validateCookieOrigin(request);
    }

    @Override
    public void revokeCurrent(String accessToken, String refreshToken) {
        Set<String> sessionIds = new LinkedHashSet<>();
        String accessSessionId = this.findAccessSessionId(accessToken);
        if (accessSessionId != null) {
            sessionIds.add(accessSessionId);
        }
        String refreshSessionId = this.findAuthenticatedSessionId(refreshToken);
        if (refreshSessionId != null) {
            sessionIds.add(refreshSessionId);
        }
        sessionIds.forEach(sessionId -> this.revokeSession(sessionId,
            LogoutReasonEnum.LOGOUT));
    }

    @Override
    public void revokeByUser(Long userId) {
        if (userId != null) {
            this.invalidateSessions(() -> sessionStore.lockUserPolicy(userId),
                () -> sessionStore.incrementUserSecurityVersion(userId),
                () -> sessionStore.findByUser(userId));
        }
    }

    @Override
    public void revokeByTenant(Long tenantId) {
        if (tenantId != null) {
            this.invalidateSessions(() -> sessionStore.lockTenantPolicyWrite(tenantId),
                () -> sessionStore.incrementTenantSecurityVersion(tenantId),
                () -> sessionStore.findByTenant(tenantId));
        }
    }

    @Override
    public void revokeByClient(String clientId) {
        if (clientId != null) {
            this.invalidateSessions(() -> sessionStore.lockClientPolicyWrite(clientId),
                () -> sessionStore.incrementClientSecurityVersion(clientId),
                () -> sessionStore.findByClient(clientId));
        }
    }

    @Override
    public List<RefreshSession> listSessions(Long tenantId) {
        Set<String> sessionIds = tenantId == null ? sessionStore.findAll()
            : sessionStore.findByTenant(tenantId);
        List<RefreshSession> sessions = new ArrayList<>(sessionIds.size());
        for (String sessionId : sessionIds) {
            RefreshSession session = this.getSession(sessionId);
            if (session != null
                && (tenantId == null || Objects.equals(tenantId, session.getTenantId()))) {
                sessions.add(session);
            }
        }
        sessions.sort(Comparator.comparingLong(RefreshSession::getCreatedAt).reversed()
            .thenComparing(RefreshSession::getSessionId));
        return sessions;
    }

    @Override
    public RefreshSession getSession(String sessionId) {
        if (StrUtil.isBlank(sessionId)) {
            return null;
        }
        RefreshSession session = sessionStore.get(sessionId);
        if (session == null) {
            return null;
        }
        if (session.getExpiresAt() <= System.currentTimeMillis()
            || !sessionStore.isSecurityVersionCurrent(session)) {
            this.revokeSession(sessionId, null);
            return null;
        }
        return session;
    }

    @Override
    public void revokeBySessionId(String sessionId) {
        if (StrUtil.isNotBlank(sessionId)) {
            this.revokeSession(sessionId, LogoutReasonEnum.KICKOUT);
        }
    }

    @Override
    public String getInvalidReason(String accessToken) {
        if (StrUtil.isBlank(accessToken)) {
            return null;
        }
        String claimedSessionId = Convert.toStr(StpUtil.getExtra(accessToken,
            AuthSessionConstants.SESSION_ID_CLAIM));
        if (StrUtil.isBlank(claimedSessionId)) {
            return LogoutReasonEnum.LOGOUT.getMessage();
        }
        // 快路径：本地缓存命中「会话有效」，跳过 2 次 Redis 往返；撤销经广播失效并由
        // 本地缓存 TTL 兜底，未命中一律回源全量校验，正确性不依赖缓存。
        if (accessSessionCache.isValid(claimedSessionId)) {
            return null;
        }
        RefreshSession session = sessionStore.get(claimedSessionId);
        Long loginId = Convert.toLong(StpUtil.getLoginIdByToken(accessToken));
        boolean invalid = session == null
            || session.getExpiresAt() <= System.currentTimeMillis()
            || !sessionStore.isSecurityVersionCurrent(session)
            || !Objects.equals(session.getUserId(), loginId);
        if (!invalid) {
            accessSessionCache.markValid(claimedSessionId);
            return null;
        }
        // 只有会话已经失效时才读取撤销原因，健康请求不会因此多一次 Redis 往返。
        LogoutReasonEnum reason = sessionStore.getLogoutReason(claimedSessionId);
        return reason == null ? LogoutReasonEnum.LOGOUT.getMessage() : reason.getMessage();
    }

    @Override
    public void clearCookie(HttpServletResponse response) {
        requestGuard.clearCookie(response);
    }

    private RefreshSession newSession(IssuedToken token, UserContext userContext,
        RefreshClientPolicy client, UserExtraContext extraContext,
        AuthSecurityVersion securityVersion,
        long expiresAt) {
        long now = System.currentTimeMillis();
        RefreshSession session = new RefreshSession();
        session.setSessionId(token.sessionId());
        session.setUserId(userContext.getId());
        session.setUsername(userContext.getUsername());
        session.setNickname(userContext.getNickname());
        session.setClientId(client.clientId());
        session.setClientType(client.clientType());
        session.setTenantId(userContext.getTenantId());
        session.setMode(this.getMode(client));
        session.setCreatedAt(now);
        session.setLastRefreshAt(now);
        session.setIp(extraContext.getIp());
        session.setAddress(extraContext.getAddress());
        session.setBrowser(extraContext.getBrowser());
        session.setOs(extraContext.getOs());
        session.setExpiresAt(expiresAt);
        session.setUserSecurityVersion(securityVersion.userVersion());
        session.setClientSecurityVersion(securityVersion.clientVersion());
        session.setTenantSecurityVersion(securityVersion.tenantVersion());
        session.setCurrentTokenFingerprint(token.fingerprint());
        return session;
    }

    private RefreshSession requireActiveSession(String sessionId) {
        RefreshSession session = sessionStore.get(sessionId);
        if (session == null || session.getExpiresAt() <= System.currentTimeMillis()) {
            throw this.invalidToken();
        }
        if (!sessionStore.isSecurityVersionCurrent(session)) {
            this.revokeSessionLocked(session, LogoutReasonEnum.KICKOUT);
            throw this.invalidToken();
        }
        return session;
    }

    private void requireCurrentSecurityVersion(RefreshSession session) {
        if (session.getExpiresAt() <= System.currentTimeMillis()
            || !sessionStore.isSecurityVersionCurrent(session)) {
            throw this.invalidToken();
        }
    }

    private void requireCurrentToken(RefreshSession session, String rawToken) {
        ParsedToken token = tokenCodec.parse(rawToken);
        if (!Objects.equals(session.getSessionId(), token.sessionId())
            || !tokenCodec.matches(token.fingerprint(), session.getCurrentTokenFingerprint())) {
            throw this.invalidToken();
        }
    }

    private boolean isKnownToken(RefreshSession session, String fingerprint) {
        return session != null && (tokenCodec.matches(fingerprint,
            session.getCurrentTokenFingerprint())
            || tokenCodec.matches(fingerprint,
                session.getPreviousTokenFingerprint()));
    }

    private String findAuthenticatedSessionId(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            return null;
        }
        ParsedToken token;
        try {
            token = tokenCodec.parse(refreshToken);
        } catch (BusinessException e) {
            return null;
        }
        RefreshSession session = sessionStore.get(token.sessionId());
        return (this.isKnownToken(session, token.fingerprint())
            || session != null && sessionStore.getRotation(token.fingerprint()) != null)
                ? token.sessionId()
                : null;
    }

    private String findAccessSessionId(String accessToken) {
        if (StrUtil.isBlank(accessToken)) {
            return null;
        }
        return Convert.toStr(StpUtil.getExtra(accessToken, AuthSessionConstants.SESSION_ID_CLAIM));
    }

    private void invalidateSessions(Supplier<AuthPolicyLock> policyLock,
        Runnable incrementVersion,
        Supplier<Set<String>> sessionIds) {
        this.executeWithLocks(List.of(policyLock), () -> {
            incrementVersion.run();
            this.revokeSessions(sessionIds.get(), LogoutReasonEnum.KICKOUT);
            return null;
        });
    }

    /**
     * 获取一组固定顺序的分布式锁。若处于数据库事务中，锁延迟到事务完成后释放，保证
     * 其他登录只能看到事务提交前的旧状态或提交后的新状态，不能落入中间窗口。
     */
    private <T> T executeWithLocks(List<Supplier<AuthPolicyLock>> lockSuppliers,
        Supplier<T> action) {
        List<AuthPolicyLock> locks = new ArrayList<>(lockSuppliers.size());
        boolean transactionManaged = false;
        try {
            for (Supplier<AuthPolicyLock> lockSupplier : lockSuppliers) {
                locks.add(lockSupplier.get());
            }
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                    throw new IllegalStateException("当前事务不支持认证策略锁同步");
                }
                TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCompletion(int status) {
                            releaseLocks(locks);
                        }
                    });
                transactionManaged = true;
            }
            return action.get();
        } finally {
            if (!transactionManaged) {
                this.releaseLocks(locks);
            }
        }
    }

    private void releaseLocks(List<AuthPolicyLock> locks) {
        for (int i = locks.size() - 1; i >= 0; i--) {
            try {
                locks.get(i).close();
            } catch (RuntimeException e) {
                log.warn("释放认证策略锁失败", e);
            }
        }
    }

    /**
     * 批量撤销会话并记录失效原因。
     *
     * @param sessionIds 待撤销的 Refresh Session ID
     * @param reason     客户端可见的失效原因，null 表示不记录
     */
    private void revokeSessions(Set<String> sessionIds, LogoutReasonEnum reason) {
        RuntimeException firstFailure = null;
        for (String sessionId : sessionIds) {
            try {
                this.revokeSession(sessionId, reason);
            } catch (RuntimeException e) {
                log.warn("撤销 Refresh Session [{}] 失败", sessionId, e);
                if (firstFailure == null) {
                    firstFailure = e;
                }
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }

    /** 加载安全版本仍有效的用户会话，并清理失效记录。 */
    private List<RefreshSession> listActiveSessions(Long userId) {
        List<RefreshSession> sessions = new ArrayList<>();
        for (String sessionId : sessionStore.findByUser(userId)) {
            RefreshSession session = sessionStore.get(sessionId);
            if (session == null || !Objects.equals(userId, session.getUserId())) {
                continue;
            }
            if (session.getExpiresAt() <= System.currentTimeMillis()
                || !sessionStore.isSecurityVersionCurrent(session)) {
                this.revokeSession(sessionId, null);
                continue;
            }
            sessions.add(session);
        }
        sessions.sort(Comparator.comparingLong(RefreshSession::getCreatedAt)
            .thenComparing(RefreshSession::getSessionId));
        return sessions;
    }

    /**
     * 为本次新登录预留一个名额，稳定淘汰创建时间最早的 Refresh Session。
     *
     * <p>被淘汰会话看到的提示由客户端配置的注销模式决定：Sa-Token 不再参与并发控制，
     * 这里必须自行把 {@code overflowLogoutMode} 落成失效原因。</p>
     */
    private void evictOverflowSessions(List<RefreshSession> activeSessions,
        Integer maxLoginCount, LogoutReasonEnum overflowLogoutMode) {
        if (maxLoginCount == null || maxLoginCount == -1) {
            return;
        }
        if (maxLoginCount < 1) {
            throw new BusinessException("客户端最大登录数量必须为 -1 或正整数");
        }
        int overflowCount = activeSessions.size() - maxLoginCount + 1;
        if (overflowCount <= 0) {
            return;
        }
        Set<String> sessionIds = new LinkedHashSet<>();
        activeSessions.stream()
            .limit(overflowCount)
            .map(RefreshSession::getSessionId)
            .forEach(sessionIds::add);
        this.revokeSessions(sessionIds,
            overflowLogoutMode == null ? LogoutReasonEnum.REPLACED : overflowLogoutMode);
    }

    private void revokeSession(String sessionId, LogoutReasonEnum reason) {
        try (RedisLockUtils ignored = sessionStore.lockSession(sessionId)) {
            RefreshSession session = sessionStore.get(sessionId);
            if (session != null) {
                this.revokeSessionLocked(session, reason);
            }
        }
    }

    private void revokeSessionLocked(RefreshSession session, LogoutReasonEnum reason) {
        String currentFingerprint = session.getCurrentTokenFingerprint();
        String previousFingerprint = session.getPreviousTokenFingerprint();
        // 先写失效原因再删会话，保证被撤销的客户端下一次请求能读到成立的原因。
        sessionStore.saveLogoutReason(session.getSessionId(), reason);
        sessionStore.delete(session.getSessionId());
        sessionStore.deleteRotation(currentFingerprint);
        sessionStore.deleteRotation(previousFingerprint);
        sessionStore.deleteLatestRotation(session.getSessionId());
        try {
            sessionStore.removeIndexes(session);
        } catch (RuntimeException e) {
            // Session 已删除；索引清理失败只会产生会自动过期的脏数据，不会让 Token 重新有效。
            log.warn("清理 Refresh Session [{}] 管理索引失败", session.getSessionId(), e);
        }
        sessionRevocationNotifier.notifyRevoked(session.getSessionId());
        // 清除热路径本地缓存并广播通知其他节点，撤销即时性由广播与本地 TTL 共同保证。
        accessSessionCache.invalidate(session.getSessionId());
    }

    private void revokeAfterFailure(String sessionId) {
        try {
            // 登录签发失败时客户端还没拿到任何凭证，不需要记录失效原因。
            this.revokeSession(sessionId, null);
        } catch (RuntimeException revokeException) {
            log.error("失败补偿时撤销 Refresh Session [{}] 失败", sessionId, revokeException);
        }
    }

    private void kickoutQuietly(String accessToken) {
        try {
            StpUtil.kickoutByTokenValue(accessToken);
        } catch (RuntimeException e) {
            log.warn("刷新失败后清理 Access Token 失败", e);
        }
    }

    private RefreshRotationResult pendingRotation(String newRefreshToken) {
        RefreshRotationResult result = new RefreshRotationResult();
        result.setEncryptedRefreshToken(tokenCodec.encrypt(newRefreshToken));
        return result;
    }

    private RefreshRotationResult completedRotation(LoginResp loginResp, String refreshToken) {
        RefreshRotationResult result = this.pendingRotation(refreshToken);
        result.setEncryptedAccessToken(tokenCodec.encrypt(loginResp.getAccessToken()));
        result.setExpiresIn(loginResp.getExpiresIn());
        result.setRefreshExpiresIn(loginResp.getRefreshExpiresIn());
        result.setTenantId(loginResp.getTenantId());
        return result;
    }

    private boolean isCurrentRotationResult(RefreshSession session,
        RefreshRotationResult result) {
        String refreshToken = tokenCodec.decrypt(result.getEncryptedRefreshToken());
        ParsedToken parsedToken = tokenCodec.parse(refreshToken);
        return Objects.equals(session.getSessionId(), parsedToken.sessionId())
            && tokenCodec.matches(parsedToken.fingerprint(),
                session.getCurrentTokenFingerprint());
    }

    private void preservePendingRotation(ParsedToken presentedToken, RefreshSession session,
        String newRefreshToken) {
        RefreshRotationResult pending = this.pendingRotation(newRefreshToken);
        try {
            sessionStore.saveLatestRotation(session.getSessionId(), pending,
                properties.getRotationGracePeriod());
            sessionStore.saveRotation(presentedToken.fingerprint(), pending,
                properties.getRotationGracePeriod());
        } catch (RuntimeException persistenceException) {
            log.warn("保存 Refresh Session [{}] 待恢复轮换状态失败", session.getSessionId(),
                persistenceException);
        }
    }

    private LoginResp replayRotation(RefreshRotationResult result, RefreshTokenModeEnum mode,
        HttpServletResponse response) {
        String accessToken = tokenCodec.decrypt(result.getEncryptedAccessToken());
        String refreshToken = tokenCodec.decrypt(result.getEncryptedRefreshToken());
        LoginResp loginResp = LoginResp.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(result.getExpiresIn())
            .refreshExpiresIn(result.getRefreshExpiresIn())
            .tenantId(result.getTenantId())
            .build();
        if (RefreshTokenModeEnum.BODY.equals(mode)) {
            loginResp.setRefreshToken(refreshToken);
        } else {
            requestGuard.writeCookie(response, refreshToken, result.getRefreshExpiresIn());
        }
        requestGuard.disableCaching(response);
        return loginResp;
    }

    private long calculateExpiresAt(long now, long timeoutSeconds) {
        if (timeoutSeconds > (Long.MAX_VALUE - now) / 1000) {
            throw new BusinessException("令牌有效期超出系统支持范围");
        }
        return now + timeoutSeconds * 1000;
    }

    private long remainingSeconds(long expiresAt, long now) {
        long remainingMillis = expiresAt - now;
        if (remainingMillis <= 0) {
            return 1;
        }
        return remainingMillis / 1000 + (remainingMillis % 1000 == 0 ? 0 : 1);
    }

    private RefreshTokenException invalidToken() {
        return RefreshTokenException.unauthorized("登录状态已失效，请重新登录");
    }

}
