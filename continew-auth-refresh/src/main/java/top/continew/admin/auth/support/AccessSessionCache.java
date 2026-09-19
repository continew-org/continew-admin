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

import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.api.AuthSessionConstants;
import top.continew.admin.auth.config.RefreshTokenProperties;

import java.util.concurrent.TimeUnit;

/**
 * 会话有效结论的本地缓存：热路径 0 Redis、撤销广播失效、TTL 兜底。
 *
 * <p>只缓存「会话有效」结果；未命中一律回源全量校验，正确性不依赖本缓存。撤销通过
 * {@link #invalidate(String)} 同时清除本节点缓存并广播，其余节点收到后清除各自缓存；
 * 广播失败时由本地 TTL（2 秒）兜底，撤销延迟最坏 2 秒。</p>
 *
 * <p>广播 Topic 默认按应用名隔离（{@code auth:access-session-invalid:{spring.application.name}}）：
 * 同一服务的多副本共用 Topic（必须互相失效），不同服务天然隔离（互不串扰）；多个服务
 * 有意共享同一会话域时可通过 {@code auth.refresh-token.access-session-invalid-topic}
 * 显式覆盖为公共 Topic。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Slf4j
@Component
public class AccessSessionCache {

    /** 本地缓存 TTL：同时是撤销延迟的最坏兜底上限 */
    private static final long CACHE_TTL_SECONDS = 2;
    private static final int LOCAL_LIMIT = 100_000;

    private final RefreshTokenProperties properties;
    private final RedissonClient redissonClient;
    private final String topicName;
    private Cache<String, Boolean> validCache;
    private RTopic invalidTopic;

    public AccessSessionCache(RefreshTokenProperties properties, RedissonClient redissonClient,
        @Value("${spring.application.name:unknown}") String applicationName) {
        this.properties = properties;
        this.redissonClient = redissonClient;
        // 未显式配置时按应用名隔离；应用名缺失时退化为 unknown，多服务场景应显式配置
        // topic 或保证 spring.application.name 唯一。
        this.topicName = StrUtil.blankToDefault(properties.getAccessSessionInvalidTopic(),
            AuthSessionConstants.ACCESS_SESSION_INVALID_TOPIC_PREFIX + ":" + StrUtil
                .blankToDefault(applicationName, "unknown"));
    }

    @PostConstruct
    void init() {
        this.validCache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
            .expireAfterWrite(CACHE_TTL_SECONDS, TimeUnit.SECONDS)
            .limit(LOCAL_LIMIT)
            .buildCache();
        if (properties.isAccessSessionCacheEnabled()) {
            this.invalidTopic = redissonClient.getTopic(this.topicName);
            this.invalidTopic.addListener(String.class, (channel, sessionId) -> {
                // 防御性校验：topic 被错误共享时忽略空消息/非法载荷，只处理形如会话 ID 的消息
                if (StrUtil.isBlank(sessionId)) {
                    return;
                }
                validCache.remove(sessionId);
                log.debug("收到会话 [{}] 失效广播，已清除本地缓存", sessionId);
            });
        }
    }

    /** 热路径：会话是否缓存为「有效」。未命中或未启用返回 false，走回源校验。 */
    public boolean isValid(String sessionId) {
        return properties.isAccessSessionCacheEnabled() && StrUtil.isNotBlank(sessionId)
            && Boolean.TRUE.equals(validCache.get(sessionId));
    }

    /** 回源校验通过后写入本地缓存。 */
    public void markValid(String sessionId) {
        if (properties.isAccessSessionCacheEnabled() && StrUtil.isNotBlank(sessionId)) {
            validCache.put(sessionId, Boolean.TRUE);
        }
    }

    /** 会话失效：清除本节点缓存并广播通知其他节点。 */
    public void invalidate(String sessionId) {
        if (!properties.isAccessSessionCacheEnabled() || StrUtil.isBlank(sessionId)) {
            return;
        }
        validCache.remove(sessionId);
        try {
            invalidTopic.publish(sessionId);
        } catch (RuntimeException e) {
            // 广播失败时本地缓存已清除，其他节点由 2s TTL 兜底，不会无限期放行。
            log.warn("广播会话 [{}] 失效失败，将由本地缓存 TTL 兜底", sessionId, e);
        }
    }
}
