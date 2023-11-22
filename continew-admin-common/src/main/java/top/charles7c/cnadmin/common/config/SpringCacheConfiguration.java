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

package top.charles7c.cnadmin.common.config;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * Spring Cache 配置
 *
 * @author Charles7c
 * @since 2022/12/28 23:17
 */
@Slf4j
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class SpringCacheConfiguration extends CachingConfigurerSupport {

    private final ObjectMapper objectMapper;

    /**
     * 解决 Spring Cache（@Cacheable）缓存乱码问题
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        ObjectMapper objectMapperCopy = objectMapper.copy();
        objectMapperCopy.activateDefaultTyping(objectMapperCopy.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapperCopy)));
        CacheProperties.Redis redisCacheProperties = cacheProperties.getRedis();
        if (null != redisCacheProperties.getTimeToLive()) {
            redisCacheConfiguration = redisCacheConfiguration.entryTtl(redisCacheProperties.getTimeToLive());
        }
        if (!redisCacheProperties.isCacheNullValues()) {
            redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
        }
        return redisCacheConfiguration;
    }

    /**
     * 自定义缓存 key 生成策略（如果 @Cacheable 不指定 key，则默认使用该策略）
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            String key = StrUtil.toUnderlineCase(method.getName()).toUpperCase();
            Map<String, Object> paramMap = MapUtil.newHashMap(params.length);
            for (int i = 0; i < params.length; i++) {
                paramMap.put(String.valueOf(i), params[i]);
            }
            return String.format("%s:%s", key, DigestUtil.sha256Hex(JSONUtil.toJsonStr(paramMap)));
        };
    }
}
