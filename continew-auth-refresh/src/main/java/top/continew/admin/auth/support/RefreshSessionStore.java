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

package top.continew.admin.auth.support;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.enums.LogoutReasonEnum;
import top.continew.admin.auth.model.AuthSecurityVersion;
import top.continew.admin.auth.model.RefreshRotationResult;
import top.continew.admin.auth.model.RefreshSession;
import top.continew.admin.auth.exception.RefreshTokenException;
import top.continew.starter.cache.redisson.util.RedisLockUtils;
import top.continew.starter.cache.redisson.util.RedisUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Refresh Session Redis 仓储。
 *
 * <p>轮换状态全部集中在 {@code SESSION:{sid}} 单条记录中。业务层持有 Session 锁时，
 * 一次 Redis SET 即完成当前/上一个 Token 的原子切换，不再维护 Token 链和 family 指针。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshSessionStore {

    /** 新部署会话存储命名空间；不读取任何历史 Refresh Token 状态。 */
    private static final String KEY_PREFIX = "AUTH:REFRESH:V1:";
    private static final String SESSION_PREFIX = KEY_PREFIX + "DATA:";
    private static final String ALL_INDEX_KEY = KEY_PREFIX + "ALL";
    private static final String USER_INDEX_PREFIX = KEY_PREFIX + "USER:";
    private static final String TENANT_INDEX_PREFIX = KEY_PREFIX + "TENANT:";
    private static final String CLIENT_INDEX_PREFIX = KEY_PREFIX + "CLIENT:";
    private static final String ROTATION_PREFIX = KEY_PREFIX + "ROTATION:";
    private static final String LOGOUT_REASON_PREFIX = KEY_PREFIX + "REASON:";
    private static final String LATEST_ROTATION_PREFIX = KEY_PREFIX + "ROTATION:LATEST:";
    private static final String SESSION_LOCK_PREFIX = KEY_PREFIX + "LOCK:SESSION:";
    private static final String USER_POLICY_LOCK_PREFIX = KEY_PREFIX + "LOCK:USER:";
    private static final String TENANT_POLICY_LOCK_PREFIX = KEY_PREFIX + "LOCK:TENANT:";
    private static final String CLIENT_POLICY_LOCK_PREFIX = KEY_PREFIX + "LOCK:CLIENT:";
    private static final String USER_VERSION_PREFIX = KEY_PREFIX + "VERSION:USER:";
    private static final String TENANT_VERSION_PREFIX = KEY_PREFIX + "VERSION:TENANT:";
    private static final String CLIENT_VERSION_PREFIX = KEY_PREFIX + "VERSION:CLIENT:";
    private static final long LOCK_WAIT_MILLIS = 5000L;
    /**
     * 失效原因标记保留时长（秒）。
     *
     * <p>标记只用于在会话被撤销后向客户端返回更有针对性的提示，不影响任何鉴权判定；
     * 过期后请求回落到默认提示，因此不需要跟随 Refresh Token 的生命周期。</p>
     */
    private static final long LOGOUT_REASON_TTL_SECONDS = 300L;
    private static final String RATE_LIMIT_SCRIPT = "local current = redis.call('INCR', KEYS[1]); "
        + "if current == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]); end; "
        + "return current;";

    private final RedissonClient redissonClient;

    /** 按 Session ID 读取会话。 */
    public RefreshSession get(String sessionId) {
        Object value = RedisUtils.get(SESSION_PREFIX + sessionId);
        if (value == null) {
            return null;
        }
        try {
            return JSONUtil.toBean(value.toString(), RefreshSession.class);
        } catch (RuntimeException e) {
            // 记录损坏（手工改库、滚动发布跨版本写入等）时按「会话不存在」处理，
            // 配合 getSession/revokeSession 的懒清理删除脏记录，避免热路径 500。
            log.warn("反序列化 Refresh Session [{}] 失败，按失效会话处理", sessionId, e);
            return null;
        }
    }

    /** 保存完整 Session；Redis SET 本身是单 Key 原子操作。 */
    public void save(RefreshSession session) {
        String key = SESSION_PREFIX + session.getSessionId();
        String value = JSONUtil.toJsonStr(session);
        RedisUtils.set(key, value,
            Duration.ofSeconds(this.remainingSeconds(session.getExpiresAt())));
    }

    /** 删除 Session；Session 不存在即代表该登录会话失效。 */
    public void delete(String sessionId) {
        RedisUtils.delete(SESSION_PREFIX + sessionId);
    }

    /** 建立用户、租户和客户端到 Session 的管理索引。 */
    public void index(RefreshSession session) {
        this.addToIndex(ALL_INDEX_KEY, session.getSessionId(), session.getExpiresAt());
        this.addToIndex(USER_INDEX_PREFIX + session.getUserId(), session.getSessionId(),
            session.getExpiresAt());
        if (session.getTenantId() != null) {
            this.addToIndex(TENANT_INDEX_PREFIX + session.getTenantId(), session.getSessionId(),
                session.getExpiresAt());
        }
        this.addToIndex(CLIENT_INDEX_PREFIX + session.getClientId(), session.getSessionId(),
            session.getExpiresAt());
    }

    /** 查询指定用户的全部 Refresh Session ID。 */
    public Set<String> findByUser(Long userId) {
        return this.findByIndex(USER_INDEX_PREFIX + userId);
    }

    /** 查询全部有效 Refresh Session ID。 */
    public Set<String> findAll() {
        return this.findByIndex(ALL_INDEX_KEY);
    }

    /** 查询指定租户的全部 Refresh Session ID。 */
    public Set<String> findByTenant(Long tenantId) {
        return this.findByIndex(TENANT_INDEX_PREFIX + tenantId);
    }

    /** 查询指定客户端的全部 Refresh Session ID。 */
    public Set<String> findByClient(String clientId) {
        return this.findByIndex(CLIENT_INDEX_PREFIX + clientId);
    }

    /** 从所有管理索引移除指定 Session。 */
    public void removeIndexes(RefreshSession session) {
        this.getIndex(ALL_INDEX_KEY).remove(session.getSessionId());
        this.getIndex(USER_INDEX_PREFIX + session.getUserId()).remove(session.getSessionId());
        if (session.getTenantId() != null) {
            this.getIndex(TENANT_INDEX_PREFIX + session.getTenantId())
                .remove(session.getSessionId());
        }
        this.getIndex(CLIENT_INDEX_PREFIX + session.getClientId()).remove(session.getSessionId());
    }

    /** 保存短时加密轮换结果。 */
    public void saveRotation(String oldTokenFingerprint, RefreshRotationResult result,
        long ttlSeconds) {
        RedisUtils.set(ROTATION_PREFIX + oldTokenFingerprint, JSONUtil.toJsonStr(result),
            Duration.ofSeconds(Math.max(1, ttlSeconds)));
    }

    /** 读取指定旧 Token 的短时轮换结果。 */
    public RefreshRotationResult getRotation(String oldTokenFingerprint) {
        Object value = RedisUtils.get(ROTATION_PREFIX + oldTokenFingerprint);
        return value == null ? null
            : JSONUtil.toBean(value.toString(), RefreshRotationResult.class);
    }

    /** 删除指定 Token 的短时轮换结果。 */
    public void deleteRotation(String tokenFingerprint) {
        if (tokenFingerprint != null) {
            RedisUtils.delete(ROTATION_PREFIX + tokenFingerprint);
        }
    }

    /** 保存 Session 在当前宽限期内的最新轮换结果。 */
    public void saveLatestRotation(String sessionId, RefreshRotationResult result,
        long ttlSeconds) {
        RedisUtils.set(LATEST_ROTATION_PREFIX + sessionId, JSONUtil.toJsonStr(result),
            Duration.ofSeconds(Math.max(1, ttlSeconds)));
    }

    /** 读取 Session 在当前宽限期内的最新轮换结果。 */
    public RefreshRotationResult getLatestRotation(String sessionId) {
        Object value = RedisUtils.get(LATEST_ROTATION_PREFIX + sessionId);
        return value == null ? null
            : JSONUtil.toBean(value.toString(), RefreshRotationResult.class);
    }

    /** 删除 Session 的最新轮换结果。 */
    public void deleteLatestRotation(String sessionId) {
        RedisUtils.delete(LATEST_ROTATION_PREFIX + sessionId);
    }

    /** 记录会话失效原因，供客户端下次请求读取更有针对性的提示。 */
    public void saveLogoutReason(String sessionId, LogoutReasonEnum reason) {
        if (sessionId == null || reason == null) {
            return;
        }
        RedisUtils.set(LOGOUT_REASON_PREFIX + sessionId, reason.name(),
            Duration.ofSeconds(LOGOUT_REASON_TTL_SECONDS));
    }

    /** 读取会话失效原因；标记不存在或已过期时返回 null。 */
    public LogoutReasonEnum getLogoutReason(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        Object value = RedisUtils.get(LOGOUT_REASON_PREFIX + sessionId);
        if (value == null) {
            return null;
        }
        try {
            return LogoutReasonEnum.valueOf(value.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 删除会话失效原因标记。 */
    public void deleteLogoutReason(String sessionId) {
        if (sessionId != null) {
            RedisUtils.delete(LOGOUT_REASON_PREFIX + sessionId);
        }
    }

    /** 获取指定 Session 的分布式互斥锁。 */
    public RedisLockUtils lockSession(String sessionId) {
        return this.acquireLock(SESSION_LOCK_PREFIX + sessionId);
    }

    /** 获取用户登录策略锁，串行执行同一用户的新登录及 Session 数量控制。 */
    public AuthPolicyLock lockUserPolicy(Long userId) {
        return this.acquirePolicyLock(redissonClient.getLock(USER_POLICY_LOCK_PREFIX + userId));
    }

    /** 获取租户安全策略读锁，不同用户可在同一租户内并发登录。 */
    public AuthPolicyLock lockTenantPolicyRead(Long tenantId) {
        return this.acquirePolicyLock(redissonClient
            .getReadWriteLock(TENANT_POLICY_LOCK_PREFIX + tenantId)
            .readLock());
    }

    /** 获取租户安全策略写锁，与租户内的新登录严格串行。 */
    public AuthPolicyLock lockTenantPolicyWrite(Long tenantId) {
        return this.acquirePolicyLock(redissonClient
            .getReadWriteLock(TENANT_POLICY_LOCK_PREFIX + tenantId)
            .writeLock());
    }

    /** 获取客户端安全策略读锁，不同用户可通过同一客户端并发登录。 */
    public AuthPolicyLock lockClientPolicyRead(String clientId) {
        return this.acquirePolicyLock(redissonClient
            .getReadWriteLock(CLIENT_POLICY_LOCK_PREFIX + clientId)
            .readLock());
    }

    /** 获取客户端安全策略写锁，与该客户端的新登录严格串行。 */
    public AuthPolicyLock lockClientPolicyWrite(String clientId) {
        return this.acquirePolicyLock(redissonClient
            .getReadWriteLock(CLIENT_POLICY_LOCK_PREFIX + clientId)
            .writeLock());
    }

    /** 尝试消耗一次指定维度的刷新配额。 */
    public boolean tryAcquireRateLimit(String key, int limit, Duration period) {
        Long current = redissonClient.getScript(StringCodec.INSTANCE)
            .eval(RScript.Mode.READ_WRITE, RATE_LIMIT_SCRIPT, RScript.ReturnType.INTEGER,
                List.<Object>of(key), period.toMillis());
        return current != null && current <= limit;
    }

    /** 读取创建新会话时需要固化的安全版本。 */
    public AuthSecurityVersion getSecurityVersion(Long userId, String clientId, Long tenantId) {
        RBatch batch = redissonClient.createBatch();
        RFuture<Long> userVersion = batch.getAtomicLong(USER_VERSION_PREFIX + userId).getAsync();
        RFuture<Long> clientVersion = batch.getAtomicLong(CLIENT_VERSION_PREFIX + clientId)
            .getAsync();
        RFuture<Long> tenantVersion = tenantId == null ? null
            : batch.getAtomicLong(TENANT_VERSION_PREFIX + tenantId).getAsync();
        batch.execute();
        return new AuthSecurityVersion(this.awaitVersion(userVersion),
            this.awaitVersion(clientVersion),
            tenantVersion == null ? 0 : this.awaitVersion(tenantVersion));
    }

    /** 读取批量结果中的安全版本；批量已执行完成，读取不会阻塞。 */
    private long awaitVersion(RFuture<Long> version) {
        return version.toCompletableFuture().join();
    }

    /** 判断 Session 固化的安全版本是否仍然有效。 */
    public boolean isSecurityVersionCurrent(RefreshSession session) {
        AuthSecurityVersion current = this.getSecurityVersion(session.getUserId(),
            session.getClientId(), session.getTenantId());
        return session.getUserSecurityVersion() == current.userVersion()
            && session.getClientSecurityVersion() == current.clientVersion()
            && session.getTenantSecurityVersion() == current.tenantVersion();
    }

    public void incrementUserSecurityVersion(Long userId) {
        redissonClient.getAtomicLong(USER_VERSION_PREFIX + userId).incrementAndGet();
    }

    public void incrementTenantSecurityVersion(Long tenantId) {
        redissonClient.getAtomicLong(TENANT_VERSION_PREFIX + tenantId).incrementAndGet();
    }

    public void incrementClientSecurityVersion(String clientId) {
        redissonClient.getAtomicLong(CLIENT_VERSION_PREFIX + clientId).incrementAndGet();
    }

    private void addToIndex(String key, String sessionId, long expiresAt) {
        RScoredSortedSet<String> index = this.getIndex(key);
        Instant expiration = Instant.ofEpochMilli(expiresAt);
        // 先延长已有索引，避免旧 TTL 恰好在新增成员与设置 TTL 之间到期；新增 Key
        // 再用 expireIfNotSet 补上 TTL，最后一次 GT 保证并发登录只能延长、不能缩短。
        index.expireIfGreater(expiration);
        index.add(expiresAt, sessionId);
        index.expireIfNotSet(expiration);
        index.expireIfGreater(expiration);
    }

    private Set<String> findByIndex(String key) {
        long now = System.currentTimeMillis();
        RScoredSortedSet<String> index = this.getIndex(key);
        index.removeRangeByScore(Double.NEGATIVE_INFINITY, true, now, true);
        Collection<String> values = index.valueRange(now, false, Double.POSITIVE_INFINITY, true);
        return new LinkedHashSet<>(values);
    }

    private RScoredSortedSet<String> getIndex(String key) {
        return redissonClient.getScoredSortedSet(key);
    }

    private RedisLockUtils acquireLock(String key) {
        RedisLockUtils lock = RedisLockUtils.tryLockWithWatchdog(key, LOCK_WAIT_MILLIS);
        if (lock.isLocked()) {
            return lock;
        }
        lock.close();
        throw RefreshTokenException.tooManyRequests("认证请求正在处理中，请稍后重试");
    }

    private AuthPolicyLock acquirePolicyLock(org.redisson.api.RLock lock) {
        return AuthPolicyLock.acquire(lock, LOCK_WAIT_MILLIS);
    }

    private long remainingSeconds(long expiresAt) {
        long remainingMillis = expiresAt - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return 1;
        }
        return remainingMillis / 1000 + (remainingMillis % 1000 == 0 ? 0 : 1);
    }
}
