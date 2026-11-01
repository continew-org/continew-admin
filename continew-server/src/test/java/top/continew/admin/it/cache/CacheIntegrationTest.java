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

package top.continew.admin.it.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.starter.cache.multicache.util.CacheUtils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多级缓存集成测试
 *
 * <p>
 * 验证 multicache（Caffeine L1 + Redis L2）端到端行为：命令式写入落 Redis、回源加载
 * 后二次读取不再打源、evict 双层失效、evictAll 清空整个 cacheName。CacheUtils 命令式
 * 入口与 {@code @Cacheable} 注解共用同一 MultiLevelCacheManager，等价覆盖注解路径的
 * 底层语义。
 * </p>
 * <p>
 * 每个用例使用独立 cacheName 隔离 L1 残留；容器随 JVM 退出销毁，Redis 键不产生外部
 * 污染。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class CacheIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 写入后 L2 落 Redis，且经统一入口可读回
     */
    @Test
    void putShouldLandInRedis() {
        String cacheName = uniqueCacheName();
        CacheUtils.put(cacheName, "string_key", "v1");

        Set<String> keys = stringRedisTemplate.keys(cacheName + "*");
        assertThat(keys).as("写入后 Redis 应出现对应键").isNotEmpty();
        assertThat(CacheUtils.<String>get(cacheName, "string_key")).isEqualTo("v1");
    }

    /**
     * 未命中回源加载并回填，二次读取命中缓存不再执行加载器
     */
    @Test
    void getShouldLoadThroughAndHitOnSecondRead() {
        String cacheName = uniqueCacheName();
        AtomicInteger loadCount = new AtomicInteger();

        String first = CacheUtils.get(cacheName, "load_key", () -> {
            loadCount.incrementAndGet();
            return "loaded";
        });
        String second = CacheUtils.get(cacheName, "load_key", () -> {
            loadCount.incrementAndGet();
            return "reloaded";
        });

        assertThat(first).isEqualTo("loaded");
        assertThat(second).as("二次读取应命中缓存而非回源").isEqualTo("loaded");
        Integer loadTimes = loadCount.get();
        assertThat(loadTimes).as("加载器只应执行一次").isEqualTo(1);
    }

    /**
     * evict 同时清除 L1 与 L2：统一入口读不到，Redis 键消失
     */
    @Test
    void evictShouldClearBothLevels() {
        String cacheName = uniqueCacheName();
        CacheUtils.put(cacheName, "evict_key", "v1");

        CacheUtils.evict(cacheName, "evict_key");

        String evicted = CacheUtils.get(cacheName, "evict_key");
        assertThat(evicted).as("evict 后不应命中缓存").isNull();
        Set<String> keys = stringRedisTemplate.keys(cacheName + "*");
        assertThat(keys).as("evict 后 Redis 键应消失").isEmpty();
    }

    /**
     * evictAll 清空整个 cacheName 的全部键
     */
    @Test
    void evictAllShouldClearWholeCache() {
        String cacheName = uniqueCacheName();
        CacheUtils.put(cacheName, "k1", "v1");
        CacheUtils.put(cacheName, "k2", "v2");

        CacheUtils.evictAll(cacheName);

        String k1AfterClear = CacheUtils.get(cacheName, "k1");
        String k2AfterClear = CacheUtils.get(cacheName, "k2");
        assertThat(k1AfterClear).isNull();
        assertThat(k2AfterClear).isNull();
        assertThat(stringRedisTemplate.keys(cacheName + "*")).isEmpty();
    }

    /**
     * 生成用例级唯一 cacheName，隔离用例间 L1 残留
     *
     * @return cacheName
     */
    private static String uniqueCacheName() {
        return "it_cache_" + System.nanoTime();
    }
}
