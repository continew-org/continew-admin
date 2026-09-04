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

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.config.RefreshTokenProperties;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;
import top.continew.admin.auth.enums.RefreshTokenStatusEnum;
import top.continew.admin.auth.model.RefreshSession;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.RefreshTokenService;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.starter.cache.redisson.util.RedisLockUtils;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.core.util.validation.ValidationUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 基于 Redis 的 Refresh Token 会话实现。
 *
 * <p>旧 Token 在轮换后不会立即删除，而是保留 USED 状态直到自然过期。这样服务端能够
 * 识别旧 Token 的再次使用，并撤销整个 familyId，避免被盗 Token 持续换取新凭证。</p>
 *
 * @author luoqiz
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final String KEY_PREFIX = "AUTH:REFRESH:";
    private static final String FAMILY_PREFIX = KEY_PREFIX + "FAMILY:";
    private static final String REVOKED_FAMILY_PREFIX = KEY_PREFIX + "REVOKED_FAMILY:";
    private static final String USER_INDEX_PREFIX = KEY_PREFIX + "USER:";
    private static final String TENANT_INDEX_PREFIX = KEY_PREFIX + "TENANT:";
    private static final String CLIENT_INDEX_PREFIX = KEY_PREFIX + "CLIENT:";
    private static final String ACCESS_PREFIX = KEY_PREFIX + "ACCESS:";
    /** 轮换结果短时缓存，用于吸收同一旧 Token 的并发重试。 */
    private static final String ROTATION_PREFIX = KEY_PREFIX + "ROTATION:";
    private static final String LOCK_PREFIX = KEY_PREFIX + "LOCK:";
    /** 网络重试窗口；超过窗口再次使用旧 Token 才视为重放攻击。 */
    private static final long ROTATION_GRACE_SECONDS = 5L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenProperties properties;

    @Override
    public boolean isEnabled(ClientResp client) {
        return properties.isEnabled() && client != null
            && Boolean.TRUE.equals(client.getIsEnableRefreshToken());
    }

    @Override
    public long getRefreshTimeout(ClientResp client) {
        return this.getRefreshTimeoutInternal(client);
    }

    @Override
    public RefreshTokenModeEnum getMode(ClientResp client) {
        return client != null && client.getRefreshTokenMode() != null
            ? client.getRefreshTokenMode()
            : RefreshTokenModeEnum.COOKIE;
    }

    @Override
    public String issue(UserContext userContext, ClientResp client, HttpServletRequest request,
        HttpServletResponse response, String accessToken) {
        if (!this.isEnabled(client)) {
            return null;
        }
        long timeout = this.getRefreshTimeoutInternal(client);
        long now = System.currentTimeMillis();
        String familyId = IdUtil.fastSimpleUUID();
        String rawToken = this.generateToken();
        RefreshSession session = this.newSession(userContext, client, request, familyId, now,
            now + timeout * 1000);
        this.saveSession(rawToken, session, timeout);
        this.indexSession(session, this.hash(rawToken), timeout);
        // 映射至少保留到 Refresh Session 绝对过期，便于管理员按 Access Token 踢出设备会话。
        this.bindAccessToken(accessToken, familyId, timeout);
        if (RefreshTokenModeEnum.COOKIE.equals(this.getMode(client))) {
            this.writeCookie(response, rawToken, timeout);
        }
        return rawToken;
    }

    @Override
    public LoginResp rotate(String rawRefreshToken, HttpServletRequest request,
        HttpServletResponse response, Function<RefreshSession, LoginResp> accessTokenIssuer) {
        ValidationUtils.throwIfBlank(rawRefreshToken, "登录状态已失效，请重新登录");
        String tokenHash = this.hash(rawRefreshToken);
        RedisLockUtils lock = this.acquireLock(tokenHash);
        try {
            RefreshSession oldSession = this.getSession(tokenHash);
            ValidationUtils.throwIfNull(oldSession, "登录状态已失效，请重新登录");
            long now = System.currentTimeMillis();
            ValidationUtils.throwIf(this.isExpired(oldSession, now), "登录状态已失效，请重新登录");
            if (this.isFamilyRevoked(oldSession.getFamilyId())) {
                throw new BusinessException("登录状态已失效，请重新登录");
            }
            if (!RefreshTokenStatusEnum.ACTIVE.equals(oldSession.getStatus())) {
                if (RefreshTokenStatusEnum.USED.equals(oldSession.getStatus())) {
                    // 同一旧 Token 的并发请求可能都是合法网络重试，短时返回第一次轮换结果，
                    // 避免第二个请求误触发整条 familyId 撤销。
                    RotationResult rotationResult = this.getRotationResult(tokenHash);
                    if (rotationResult != null) {
                        return this.replayRotation(rotationResult, response);
                    }
                    // 超过宽限期仍使用旧 Token，才按重放攻击处理。
                    this.revokeFamily(oldSession.getFamilyId(), oldSession.getExpiresAt());
                }
                throw new BusinessException("登录状态已失效，请重新登录");
            }

            // 先由统一认证服务校验用户、租户和客户端状态，再提交 Token 轮换。
            LoginResp loginResp = accessTokenIssuer.apply(oldSession);
            String newRawToken = this.generateToken();
            String newJti = IdUtil.fastSimpleUUID();
            long ttlMillis = oldSession.getExpiresAt() - now;
            long ttlSeconds = Math.max(1, (ttlMillis + 999) / 1000);
            RefreshSession newSession = this.copyForRotation(oldSession, newJti, now);

            oldSession.setStatus(RefreshTokenStatusEnum.USED);
            oldSession.setLastUsedAt(now);
            oldSession.setReplacedBy(newJti);
            this.saveSessionByHash(tokenHash, oldSession, ttlSeconds);
            this.saveSession(newRawToken, newSession, ttlSeconds);
            this.indexSession(newSession, this.hash(newRawToken), ttlSeconds);
            this.setFamilyCurrentHash(newSession.getFamilyId(), this.hash(newRawToken), ttlSeconds);
            // 刷新后的 Access Token 仍绑定到同一 familyId，且映射保留到该家族绝对过期。
            this.bindAccessToken(loginResp.getAccessToken(), newSession.getFamilyId(), ttlSeconds);
            // 响应中的时长必须反映当前 familyId 的剩余绝对寿命，而不是重新开始计时。
            loginResp.setRefreshExpiresIn(ttlSeconds);

            if (RefreshTokenModeEnum.BODY.equals(newSession.getMode())) {
                // BODY 模式为未来 App / 小程序返回轮换后的新 Token。
                loginResp.setRefreshToken(newRawToken);
            } else {
                this.writeCookie(response, newRawToken, ttlSeconds);
            }
            this.saveRotationResult(tokenHash,
                RotationResult.from(loginResp, newRawToken, newSession.getMode()), ttlSeconds);
            return loginResp;
        } finally {
            lock.close();
        }
    }

    @Override
    public String resolve(String bodyRefreshToken, HttpServletRequest request) {
        // BODY 优先，便于未来 App / 小程序即使携带了无关 Cookie 也不会被错误覆盖。
        if (bodyRefreshToken != null && !bodyRefreshToken.isBlank()) {
            return bodyRefreshToken;
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (properties.getCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return bodyRefreshToken;
    }

    @Override
    public void revokeCurrent(String accessToken, String refreshToken) {
        if (refreshToken != null) {
            RefreshSession session = this.getSession(this.hash(refreshToken));
            if (session != null) {
                this.revokeFamily(session.getFamilyId(), session.getExpiresAt());
            }
        }
        this.revokeByAccessToken(accessToken);
    }

    @Override
    public void revokeByUser(Long userId) {
        this.revokeByIndex(USER_INDEX_PREFIX + userId);
    }

    @Override
    public void revokeByTenant(Long tenantId) {
        if (tenantId != null) {
            this.revokeByIndex(TENANT_INDEX_PREFIX + tenantId);
        }
    }

    @Override
    public void revokeByClient(String clientId) {
        if (clientId != null) {
            this.revokeByIndex(CLIENT_INDEX_PREFIX + clientId);
        }
    }

    @Override
    public void revokeByAccessToken(String accessToken) {
        if (accessToken == null) {
            return;
        }
        String familyId = RedisUtils.get(ACCESS_PREFIX + this.hash(accessToken));
        if (familyId != null) {
            String currentHash = RedisUtils.get(FAMILY_PREFIX + familyId);
            RefreshSession session = currentHash == null ? null : this.getSession(currentHash);
            long expiresAt = session == null ? System.currentTimeMillis() + 60_000
                : session.getExpiresAt();
            this.revokeFamily(familyId, expiresAt);
        }
    }

    @Override
    public void clearCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(properties.getCookieName(), "")
            .httpOnly(true)
            .secure(properties.isCookieSecure())
            .sameSite(properties.getCookieSameSite())
            .path(properties.getCookiePath())
            .maxAge(Duration.ZERO)
            .build();
        response.addHeader(HttpServletResponseHeader.SET_COOKIE, cookie.toString());
    }

    private RefreshSession newSession(UserContext userContext, ClientResp client,
        HttpServletRequest request, String familyId, long now, long expiresAt) {
        RefreshSession session = new RefreshSession();
        session.setJti(IdUtil.fastSimpleUUID());
        session.setFamilyId(familyId);
        session.setUserId(userContext.getId());
        session.setClientId(client.getClientId());
        session.setClientType(client.getClientType());
        session.setTenantId(userContext.getTenantId());
        session.setMode(this.getMode(client));
        session.setIssuedAt(now);
        session.setExpiresAt(expiresAt);
        session.setLastUsedAt(now);
        session.setStatus(RefreshTokenStatusEnum.ACTIVE);
        session.setIp(request.getRemoteAddr());
        session.setUserAgent(request.getHeader("User-Agent"));
        return session;
    }

    private RefreshSession copyForRotation(RefreshSession oldSession, String jti, long now) {
        RefreshSession session = new RefreshSession();
        session.setJti(jti);
        session.setFamilyId(oldSession.getFamilyId());
        session.setUserId(oldSession.getUserId());
        session.setClientId(oldSession.getClientId());
        session.setClientType(oldSession.getClientType());
        session.setTenantId(oldSession.getTenantId());
        session.setMode(oldSession.getMode());
        session.setIssuedAt(now);
        session.setExpiresAt(oldSession.getExpiresAt());
        session.setLastUsedAt(now);
        session.setStatus(RefreshTokenStatusEnum.ACTIVE);
        session.setIp(oldSession.getIp());
        session.setUserAgent(oldSession.getUserAgent());
        return session;
    }

    /** 使用明文 Refresh Token 计算哈希后保存，Redis 永不保存可直接使用的 Token。 */
    private void saveSession(String rawToken, RefreshSession session, long ttlSeconds) {
        this.saveSessionByHash(this.hash(rawToken), session, ttlSeconds);
    }

    /** 使用哈希值保存会话，避免对已哈希值再次哈希导致无法读取。 */
    private void saveSessionByHash(String tokenHash, RefreshSession session, long ttlSeconds) {
        RedisUtils.set(KEY_PREFIX + tokenHash, JSONUtil.toJsonStr(session),
            Duration.ofSeconds(ttlSeconds));
    }

    private RefreshSession getSession(String tokenHash) {
        Object value = RedisUtils.get(KEY_PREFIX + tokenHash);
        return value == null ? null : JSONUtil.toBean(value.toString(), RefreshSession.class);
    }

    private void indexSession(RefreshSession session, String tokenHash, long ttlSeconds) {
        String userKey = USER_INDEX_PREFIX + session.getUserId();
        RedisUtils.hSet(userKey, session.getFamilyId(), tokenHash);
        this.expireAtLeast(userKey, ttlSeconds);
        if (session.getTenantId() != null) {
            String tenantKey = TENANT_INDEX_PREFIX + session.getTenantId();
            RedisUtils.hSet(tenantKey, session.getFamilyId(), tokenHash);
            this.expireAtLeast(tenantKey, ttlSeconds);
        }
        String clientKey = CLIENT_INDEX_PREFIX + session.getClientId();
        RedisUtils.hSet(clientKey, session.getFamilyId(), tokenHash);
        this.expireAtLeast(clientKey, ttlSeconds);
        this.setFamilyCurrentHash(session.getFamilyId(), tokenHash, ttlSeconds);
    }

    /**
     * 索引可能包含多个会话，续期其中一个会话时只能延长索引寿命，不能覆盖更长的原 TTL。
     * RedisUtils 的剩余 TTL 单位是毫秒，而业务时长统一使用秒。
     */
    private void expireAtLeast(String key, long ttlSeconds) {
        long requestedTtlMillis = ttlSeconds * 1000L;
        long currentTtlMillis = RedisUtils.getTimeToLive(key);
        if (currentTtlMillis < 0 || currentTtlMillis < requestedTtlMillis) {
            RedisUtils.expire(key, Duration.ofSeconds(ttlSeconds));
        }
    }

    private void setFamilyCurrentHash(String familyId, String tokenHash, long ttlSeconds) {
        RedisUtils.set(FAMILY_PREFIX + familyId, tokenHash, Duration.ofSeconds(ttlSeconds));
    }

    private void bindAccessToken(String accessToken, String familyId, Long timeout) {
        if (accessToken == null || familyId == null) {
            return;
        }
        long ttl = timeout == null || timeout <= 0 ? 900 : timeout;
        RedisUtils.set(ACCESS_PREFIX + this.hash(accessToken), familyId, Duration.ofSeconds(ttl));
    }

    private void revokeByIndex(String indexKey) {
        Map<String, Object> entries = RedisUtils.hGetAll(indexKey);
        if (entries == null || entries.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String familyId = entry.getKey();
            String currentHash = RedisUtils.get(FAMILY_PREFIX + familyId);
            RefreshSession session = currentHash == null ? null : this.getSession(currentHash);
            long expiresAt = session == null
                ? System.currentTimeMillis() + properties.getDefaultTimeout() * 1000
                : session.getExpiresAt();
            this.revokeFamily(familyId, expiresAt);
            // 只删除本次快照中的同一条索引，避免无条件删除期间新建的其他会话索引。
            Map<String, Object> latestEntries = RedisUtils.hGetAll(indexKey);
            if (latestEntries != null
                && Objects.equals(entry.getValue(), latestEntries.get(familyId))) {
                RedisUtils.hDel(indexKey, familyId);
            }
        }
    }

    private void saveRotationResult(String tokenHash, RotationResult rotationResult,
        long ttlSeconds) {
        long cacheTtlSeconds = Math.min(ROTATION_GRACE_SECONDS, ttlSeconds);
        RedisUtils.set(ROTATION_PREFIX + tokenHash, JSONUtil.toJsonStr(rotationResult),
            Duration.ofSeconds(cacheTtlSeconds));
    }

    private RotationResult getRotationResult(String tokenHash) {
        Object value = RedisUtils.get(ROTATION_PREFIX + tokenHash);
        return value == null ? null : JSONUtil.toBean(value.toString(), RotationResult.class);
    }

    private LoginResp replayRotation(RotationResult rotationResult, HttpServletResponse response) {
        if (RefreshTokenModeEnum.COOKIE.equals(rotationResult.getMode())) {
            this.writeCookie(response, rotationResult.getRefreshToken(),
                rotationResult.getRefreshExpiresIn());
        }
        return rotationResult.toLoginResp();
    }

    private void revokeFamily(String familyId, long expiresAt) {
        if (familyId == null) {
            return;
        }
        long ttlSeconds = Math.max(1, (expiresAt - System.currentTimeMillis() + 999) / 1000);
        // familyId 是一次登录会话的稳定分组标识；撤销标记可以让当前 Token 和所有后继 Token 一起失效。
        RedisUtils.set(REVOKED_FAMILY_PREFIX + familyId, Boolean.TRUE,
            Duration.ofSeconds(ttlSeconds));
        Object currentHash = RedisUtils.get(FAMILY_PREFIX + familyId);
        if (currentHash != null) {
            RefreshSession current = this.getSession(currentHash.toString());
            if (current != null) {
                current.setStatus(RefreshTokenStatusEnum.REVOKED);
                this.saveSessionByHash(currentHash.toString(), current, ttlSeconds);
            }
        }
        RedisUtils.delete(FAMILY_PREFIX + familyId);
    }

    private boolean isFamilyRevoked(String familyId) {
        return familyId != null && RedisUtils.exists(REVOKED_FAMILY_PREFIX + familyId);
    }

    private boolean isExpired(RefreshSession session, long now) {
        return session.getExpiresAt() <= now;
    }

    private long getRefreshTimeoutInternal(ClientResp client) {
        Long timeout = client.getRefreshTokenTimeout();
        long result = timeout == null || timeout <= 0 ? properties.getDefaultTimeout() : timeout;
        ValidationUtils.throwIf(result <= 0, "系统登录服务异常，请联系管理员");
        return result;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        return SecureUtil.sha256(value);
    }

    private RedisLockUtils acquireLock(String tokenHash) {
        String lockKey = LOCK_PREFIX + tokenHash;
        for (int i = 0; i < 20; i++) {
            RedisLockUtils lock = RedisLockUtils.tryLockWithWatchdog(lockKey);
            if (lock.isLocked()) {
                return lock;
            }
            lock.close();
            try {
                Thread.sleep(25L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("刷新请求被中断");
            }
        }
        throw new BusinessException("刷新请求过于频繁，请稍后重试");
    }

    private void writeCookie(HttpServletResponse response, String rawToken, long ttlSeconds) {
        ResponseCookie cookie = ResponseCookie.from(properties.getCookieName(), rawToken)
            .httpOnly(true)
            .secure(properties.isCookieSecure())
            .sameSite(properties.getCookieSameSite())
            .path(properties.getCookiePath())
            .maxAge(Duration.ofSeconds(ttlSeconds))
            .build();
        response.addHeader(HttpServletResponseHeader.SET_COOKIE, cookie.toString());
    }

    /**
     * 一次 Refresh Token 轮换的响应快照。
     *
     * <p>这里缓存的是同一次轮换的结果而不是再次签发令牌，保证并发请求拿到相同的
     * Access Token 和 Refresh Token；refreshToken 字段只在 BODY 模式返回给客户端。</p>
     */
    @Data
    @NoArgsConstructor
    private static class RotationResult {

        private String refreshToken;
        private String accessToken;
        private String tokenType;
        private Long expiresIn;
        private Long refreshExpiresIn;
        private Boolean refreshTokenEnabled;
        private Long tenantId;
        private RefreshTokenModeEnum mode;

        private static RotationResult from(LoginResp loginResp, String refreshToken,
            RefreshTokenModeEnum mode) {
            RotationResult result = new RotationResult();
            result.refreshToken = refreshToken;
            result.accessToken = loginResp.getAccessToken();
            result.tokenType = loginResp.getTokenType();
            result.expiresIn = loginResp.getExpiresIn();
            result.refreshExpiresIn = loginResp.getRefreshExpiresIn();
            result.refreshTokenEnabled = loginResp.getRefreshTokenEnabled();
            result.tenantId = loginResp.getTenantId();
            result.mode = mode;
            return result;
        }

        private LoginResp toLoginResp() {
            return LoginResp.builder()
                .accessToken(accessToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .refreshExpiresIn(refreshExpiresIn)
                .refreshTokenEnabled(refreshTokenEnabled)
                .refreshToken(RefreshTokenModeEnum.BODY.equals(mode) ? refreshToken : null)
                .tenantId(tenantId)
                .build();
        }
    }

    /** 避免在源码中散落 Header 字符串，同时兼容 Servlet API 的响应头常量表达 */
    private static final class HttpServletResponseHeader {

        private static final String SET_COOKIE = "Set-Cookie";

        private HttpServletResponseHeader() {
        }
    }
}
