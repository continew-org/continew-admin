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

package top.charles7c.cnadmin.common.util;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.redisson.api.*;
import org.redisson.config.Config;

import cn.hutool.extra.spring.SpringUtil;

/**
 * Redis 工具类
 *
 * @author Charles7c
 * @since 2022/12/11 12:00
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisUtils {

    private static final RedissonClient REDISSON_CLIENT = SpringUtil.getBean(RedissonClient.class);

    /* ################ 查询操作 ################ */
    /**
     * 获取缓存的基本对象列表
     *
     * @param keyPattern
     *            缓存键表达式
     * @return 基本对象列表
     */
    public static Collection<String> keys(final String keyPattern) {
        Stream<String> stream = REDISSON_CLIENT.getKeys().getKeysStreamByPattern(getNameMapper().map(keyPattern));
        return stream.map(key -> getNameMapper().unmap(key)).collect(Collectors.toList());
    }

    /**
     * 是否存在指定缓存
     *
     * @param key
     *            缓存键
     */
    public static Boolean hasKey(String key) {
        RKeys rKeys = REDISSON_CLIENT.getKeys();
        return rKeys.countExists(getNameMapper().map(key)) > 0;
    }

    /**
     * 获取缓存剩余存活时间
     *
     * @param key
     *            缓存键
     * @return 剩余存活时间（单位：毫秒）
     */
    public static <T> long getTimeToLive(final String key) {
        RBucket<T> rBucket = REDISSON_CLIENT.getBucket(key);
        return rBucket.remainTimeToLive();
    }

    /**
     * 获取缓存的基本对象
     *
     * @param key
     *            缓存键
     * @return 缓存值
     */
    public static <T> T getCacheObject(final String key) {
        RBucket<T> rBucket = REDISSON_CLIENT.getBucket(key);
        return rBucket.get();
    }

    /* ################ 操作有效期 ################ */
    /**
     * 设置过期时间
     *
     * @param key
     *            缓存键
     * @param timeout
     *            过期时间
     * @return true 设置成功；false 设置失败
     */
    public static boolean expire(final String key, final long timeout) {
        return expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 设置过期时间
     *
     * @param key
     *            缓存键
     * @param duration
     *            过期时间
     * @return true 设置成功；false 设置失败
     */
    public static boolean expire(final String key, final Duration duration) {
        RBucket rBucket = REDISSON_CLIENT.getBucket(key);
        return rBucket.expire(duration);
    }

    /* ################ 操作基本对象 ################ */
    /**
     * 缓存基本对象（Integer、String、实体类等）
     *
     * @param key
     *            缓存键
     * @param value
     *            缓存值
     */
    public static <T> void setCacheObject(final String key, final T value) {
        setCacheObject(key, value, false);
    }

    /**
     * 缓存基本对象，保留当前对象 TTL 有效期
     *
     * @param key
     *            缓存键
     * @param value
     *            缓存值
     * @param isSaveTtl
     *            是否保留 TTL 有效期(例如: set 之前 ttl 剩余 90，set 之后还是为 90)
     * @since Redis 6.X 以上使用 setAndKeepTTL 兼容 5.X 方案
     */
    public static <T> void setCacheObject(final String key, final T value, final boolean isSaveTtl) {
        RBucket<T> bucket = REDISSON_CLIENT.getBucket(key);
        if (isSaveTtl) {
            try {
                bucket.setAndKeepTTL(value);
            } catch (Exception e) {
                long timeToLive = bucket.remainTimeToLive();
                setCacheObject(key, value, Duration.ofMillis(timeToLive));
            }
        } else {
            bucket.set(value);
        }
    }

    /**
     * 缓存基本对象（Integer、String、实体类等）
     *
     * @param key
     *            缓存键
     * @param value
     *            缓存值
     * @param duration
     *            时间
     */
    public static <T> void setCacheObject(final String key, final T value, final Duration duration) {
        RBatch batch = REDISSON_CLIENT.createBatch();
        RBucketAsync<T> bucket = batch.getBucket(key);
        bucket.setAsync(value);
        bucket.expireAsync(duration);
        batch.execute();
    }

    /**
     * 删除缓存的基本对象
     *
     * @param key
     *            缓存键
     */
    public static boolean deleteCacheObject(final String key) {
        return REDISSON_CLIENT.getBucket(key).delete();
    }

    /**
     * 删除缓存的基本对象列表
     *
     * @param keyPattern
     *            缓存键表达式
     */
    public static void deleteKeys(final String keyPattern) {
        REDISSON_CLIENT.getKeys().deleteByPattern(getNameMapper().map(keyPattern));
    }

    /**
     * 格式化缓存键，将各子键用 : 拼接起来
     *
     * @param subKeys
     *            子键列表
     * @return 缓存键
     */
    public static String formatKey(String... subKeys) {
        return String.join(":", subKeys);
    }

    /**
     * 根据集群或单机配置，获取名称映射器
     *
     * @return 名称映射器
     */
    private static NameMapper getNameMapper() {
        Config config = REDISSON_CLIENT.getConfig();
        if (config.isClusterConfig()) {
            return config.useClusterServers().getNameMapper();
        }
        return config.useSingleServer().getNameMapper();
    }
}
