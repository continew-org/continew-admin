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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import top.continew.admin.auth.config.RefreshTokenProperties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 热路径会话缓存：命中/失效/广播隔离/载荷防御测试。 */
class AccessSessionCacheTest {

    private RefreshTokenProperties properties;
    private RedissonClient redissonClient;
    private RTopic invalidTopic;
    private AccessSessionCache cache;

    @BeforeEach
    void setUp() {
        properties = new RefreshTokenProperties();
        redissonClient = mock(RedissonClient.class);
        invalidTopic = mock(RTopic.class);
        when(redissonClient.getTopic(anyString())).thenReturn(invalidTopic);
        cache = new AccessSessionCache(properties, redissonClient, "test-app");
        cache.init();
    }

    @Test
    void shouldReturnFalseBeforeMarkedValid() {
        assertFalse(cache.isValid("sid-1"));
    }

    @Test
    void shouldReturnTrueAfterMarkedValid() {
        cache.markValid("sid-1");
        assertTrue(cache.isValid("sid-1"));
    }

    @Test
    void shouldInvalidateLocallyAndBroadcast() {
        cache.markValid("sid-1");
        cache.invalidate("sid-1");
        assertFalse(cache.isValid("sid-1"));
        verify(invalidTopic).publish("sid-1");
    }

    @Test
    void shouldNotCacheOrPublishWhenDisabled() {
        properties.setAccessSessionCacheEnabled(false);
        cache.markValid("sid-1");
        assertFalse(cache.isValid("sid-1"));
        cache.invalidate("sid-1");
        verify(invalidTopic, never()).publish(anyString());
    }

    @Test
    void shouldUseAppNameScopedTopicByDefault() {
        verify(redissonClient).getTopic("auth:access-session-invalid:test-app");
    }

    @Test
    void shouldUseExplicitTopicWhenConfigured() {
        properties.setAccessSessionInvalidTopic("shared-topic");
        AccessSessionCache sharedCache = new AccessSessionCache(properties, redissonClient,
            "test-app");
        sharedCache.init();
        verify(redissonClient).getTopic("shared-topic");
    }

    @Test
    void shouldFallbackToUnknownAppName() {
        AccessSessionCache unnamedCache = new AccessSessionCache(properties, redissonClient, "");
        unnamedCache.init();
        verify(redissonClient).getTopic("auth:access-session-invalid:unknown");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldIgnoreBlankBroadcastMessage() {
        cache.markValid("sid-1");
        ArgumentCaptor<MessageListener<String>> captor = ArgumentCaptor
            .forClass(MessageListener.class);
        verify(invalidTopic).addListener(any(Class.class), captor.capture());
        MessageListener<String> listener = captor.getValue();
        listener.onMessage("channel", "");
        assertTrue(cache.isValid("sid-1"));
        listener.onMessage("channel", "sid-1");
        assertFalse(cache.isValid("sid-1"));
    }
}
