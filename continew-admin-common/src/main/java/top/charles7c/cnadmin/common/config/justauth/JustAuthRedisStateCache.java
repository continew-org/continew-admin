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

package top.charles7c.cnadmin.common.config.justauth;

import java.time.Duration;

import top.charles7c.cnadmin.common.constant.CacheConstants;
import top.charles7c.continew.starter.cache.redisson.util.RedisUtils;

import me.zhyd.oauth.cache.AuthStateCache;

/**
 * Just Auth 自定义 State 缓存实现（Redis）
 *
 * @author Charles7c
 * @since 2023/10/8 22:17
 */
public class JustAuthRedisStateCache implements AuthStateCache {

    /**
     * 存入缓存
     *
     * @param key
     *            缓存 key
     * @param value
     *            缓存内容
     */
    @Override
    public void cache(String key, String value) {
        // 参考：在 JustAuth 中，内置了一个基于 map 的 state 缓存器，默认缓存有效期为 3 分钟
        RedisUtils.set(RedisUtils.formatKey(CacheConstants.SOCIAL_AUTH_STATE_KEY_PREFIX, key), value,
            Duration.ofMinutes(3));
    }

    /**
     * 存入缓存
     *
     * @param key
     *            缓存 key
     * @param value
     *            缓存内容
     * @param timeout
     *            指定缓存过期时间（毫秒）
     */
    @Override
    public void cache(String key, String value, long timeout) {
        RedisUtils.set(RedisUtils.formatKey(CacheConstants.SOCIAL_AUTH_STATE_KEY_PREFIX, key), value,
            Duration.ofMillis(timeout));
    }

    /**
     * 获取缓存内容
     *
     * @param key
     *            缓存 key
     * @return 缓存内容
     */
    @Override
    public String get(String key) {
        return RedisUtils.get(RedisUtils.formatKey(CacheConstants.SOCIAL_AUTH_STATE_KEY_PREFIX, key));
    }

    /**
     * 是否存在 key，如果对应 key 的 value 值已过期，也返回 false
     *
     * @param key
     *            缓存 key
     * @return true：存在 key，并且 value 没过期；false：key 不存在或者已过期
     */
    @Override
    public boolean containsKey(String key) {
        return RedisUtils.hasKey(RedisUtils.formatKey(CacheConstants.SOCIAL_AUTH_STATE_KEY_PREFIX, key));
    }
}