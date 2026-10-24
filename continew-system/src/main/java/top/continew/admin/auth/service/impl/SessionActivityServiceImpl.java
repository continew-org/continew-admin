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

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBatch;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.service.SessionActivityService;
import top.continew.admin.auth.support.ClientPolicyCache;
import top.continew.starter.cache.redisson.util.RedisUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话活跃时间业务实现
 *
 * <p>活跃时间存放在独立的缓存键中，不写回 Refresh Session：会话在令牌轮换时是「读取-修改-
 * 写回」的，混进去会互相覆盖。键的命名空间也刻意不使用 {@code AUTH:REFRESH:V1:}，
 * 避免与认证组件自身的键空间冲突。</p>
 *
 * <p>值统一按字符串存放：项目 Redis 使用 JSON 编解码，小整数读回可能变成 Integer，
 * 直接存 long 会带来类型转换的意外。</p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionActivityServiceImpl implements SessionActivityService {

    /**
     * 活跃时间缓存键前缀
     */
    private static final String KEY_PREFIX = "AUTH:ACTIVITY:";

    /**
     * 写入节流窗口（毫秒），同一会话在该窗口内只落一次缓存
     */
    private static final long THROTTLE_MILLIS = 60_000;

    /**
     * 本地写入记录上限，超出后清理陈旧条目
     */
    private static final int MAX_LOCAL_ENTRIES = 10_000;

    /**
     * 本地记录视为陈旧的时长（毫秒）
     */
    private static final long STALE_MILLIS = 10 * 60_000;

    /**
     * 缓存键的最小存活时间（秒）
     */
    private static final long MIN_TTL_SECONDS = 86_400;

    /**
     * 批量读取的单批会话数
     */
    private static final int BATCH_SIZE = 500;

    private final ClientPolicyCache clientPolicyCache;

    /**
     * 本实例最近一次写入缓存的时间，用于节流与判闲置快路径
     *
     * <p>必须记录「上次写入时间」而不是「上次请求时间」：若按请求时间节流，持续操作的用户
     * 每次请求都会刷新基准，缓存永远不会被更新，活跃时间会一直停在第一次写入的时刻。</p>
     */
    private final Map<String, Long> lastWriteAt = new ConcurrentHashMap<>();

    @Override
    public void touch(String sessionId, String clientId) {
        if (StrUtil.isBlank(sessionId)) {
            return;
        }
        final ClientPolicyCache.ClientPolicy policy = clientPolicyCache.get(clientId);
        long now = System.currentTimeMillis();
        Long lastWrite = lastWriteAt.get(sessionId);
        if (lastWrite != null && now - lastWrite < throttleMillis(policy)) {
            return;
        }
        lastWriteAt.put(sessionId, now);
        if (lastWriteAt.size() > MAX_LOCAL_ENTRIES) {
            this.evictStale(now);
        }
        // 缓存键要活得比会话久：键先过期会让活跃用户被判成「没有活跃记录」。
        long ttl = Math.max(policy.refreshTokenTimeoutSeconds(), MIN_TTL_SECONDS);
        try {
            this.writeLastActive(sessionId, now, ttl);
        } catch (Exception e) {
            // 活跃时间是旁路信息，写失败不能影响业务请求：无法记录时列表会回退为
            // 按最近刷新时间估算，而认证本身只依赖会话记录。
            log.warn("写入会话活跃时间失败：sessionId={}，{}", sessionId, e.getMessage());
        }
    }

    @Override
    public Long getLastActiveTime(String sessionId) {
        if (StrUtil.isBlank(sessionId)) {
            return null;
        }
        // 以缓存为准：本实例的写入记录可能落后于其他实例，用它会让展示和判定都偏旧。
        return this.readLastActive(sessionId);
    }

    @Override
    public Map<String, Long> getLastActiveTimes(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> validIds = new ArrayList<>(sessionIds.size());
        for (String sessionId : sessionIds) {
            if (StrUtil.isNotBlank(sessionId)) {
                validIds.add(sessionId);
            }
        }
        if (validIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> result = new HashMap<>(validIds.size());
        // 会话数量等于在线设备数，一次 pipeline 上千条会放大单次响应；分批读取，
        // 单批失败只丢这一批的活跃时间，列表其余部分照常展示。
        for (int from = 0; from < validIds.size(); from += BATCH_SIZE) {
            List<String> chunk = validIds.subList(from, Math.min(from + BATCH_SIZE, validIds
                .size()));
            try {
                RBatch batch = RedisUtils.getClient().createBatch();
                for (String sessionId : chunk) {
                    batch.getBucket(this.key(sessionId)).getAsync();
                }
                List<?> responses = batch.execute().getResponses();
                for (int i = 0; i < chunk.size() && i < responses.size(); i++) {
                    Long lastActive = this.parse(responses.get(i));
                    if (lastActive != null) {
                        result.put(chunk.get(i), lastActive);
                    }
                }
            } catch (Exception e) {
                // 读不到活跃时间只会让列表回退为「按最近刷新时间估算」，不能影响列表本身。
                log.warn("批量查询会话活跃时间失败（偏移 {}）：{}", from, e.getMessage());
            }
        }
        return result;
    }

    @Override
    public boolean isIdleExpired(String sessionId, long idleTimeoutSeconds) {
        if (idleTimeoutSeconds <= 0 || StrUtil.isBlank(sessionId)) {
            return false;
        }
        long now = System.currentTimeMillis();
        long idleMillis = idleTimeoutSeconds * 1000;
        Long lastWrite = lastWriteAt.get(sessionId);
        // 快路径：本实例在闲置阈值内写过缓存，说明用户一直在操作，直接放行。
        if (lastWrite != null && now - lastWrite <= idleMillis) {
            return false;
        }
        // 慢路径：本实例没写过或已超过阈值，回源确认。多实例部署时请求可能被负载均衡
        // 分到别的实例，只看本地记录会把「在别的实例操作」误判成闲置。
        Long lastActive;
        try {
            lastActive = this.readLastActive(sessionId);
        } catch (Exception e) {
            // 读不到就无法判断，按「不判定」处理：缓存抖动时宁可让请求通过，
            // 也不能把正在操作的用户全部拒之门外。
            log.warn("读取会话活跃时间失败，跳过闲置判定：sessionId={}，{}", sessionId, e.getMessage());
            return false;
        }
        if (lastActive == null) {
            // 没有活跃记录（新会话尚未产生业务请求、升级前的存量会话、或缓存键已过期）时不做判断：
            // 「无法判断」不能当成「长期未活跃」，否则升级瞬间会踢掉一批正常会话。
            return false;
        }
        return now - lastActive > idleMillis;
    }

    /**
     * 读取会话的最后活跃时间
     *
     * @param sessionId 登录会话 ID
     * @return 最后活跃时间（epoch 毫秒），无记录时返回 null
     */
    protected Long readLastActive(String sessionId) {
        return this.parse(RedisUtils.get(this.key(sessionId)));
    }

    /**
     * 写入会话的最后活跃时间
     *
     * @param sessionId  登录会话 ID
     * @param value      最后活跃时间（epoch 毫秒）
     * @param ttlSeconds 缓存存活时间（秒）
     */
    protected void writeLastActive(String sessionId, long value, long ttlSeconds) {
        RedisUtils.set(this.key(sessionId), String.valueOf(value), Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 写入节流窗口
     *
     * <p>窗口必须明显小于闲置阈值：缓存里的活跃时间最多落后一个窗口，若窗口接近阈值，
     * 持续操作的用户也会被判成已闲置。</p>
     *
     * @param policy 客户端认证策略
     * @return 节流窗口（毫秒）
     */
    private static long throttleMillis(ClientPolicyCache.ClientPolicy policy) {
        long idleMillis = policy.idleTimeoutSeconds() * 1000;
        return idleMillis > 0 ? Math.min(THROTTLE_MILLIS, idleMillis / 4) : THROTTLE_MILLIS;
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private Long parse(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void evictStale(long now) {
        lastWriteAt.entrySet().removeIf(entry -> now - entry.getValue() > STALE_MILLIS);
    }
}
