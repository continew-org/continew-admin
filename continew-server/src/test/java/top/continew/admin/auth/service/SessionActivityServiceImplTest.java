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

package top.continew.admin.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.continew.admin.auth.service.impl.SessionActivityServiceImpl;
import top.continew.admin.auth.support.ClientPolicyCache;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话活跃时间与闲置判定测试
 *
 * @author luoqiz
 * @since 5.0.0
 */
class SessionActivityServiceImplTest {

    private static final String SESSION_ID = "session-1";

    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;

    private TestableSessionActivityService service;

    @BeforeEach
    void setUp() {
        ClientPolicyCache clientPolicyCache = mock(ClientPolicyCache.class);
        when(clientPolicyCache.get(any()))
            .thenReturn(new ClientPolicyCache.ClientPolicy(3600L, 2_592_000L));
        service = new TestableSessionActivityService(clientPolicyCache);
    }

    @Test
    void shouldWriteOnceWithinThrottleWindow() {
        service.touch(SESSION_ID, "web");
        service.touch(SESSION_ID, "web");
        service.touch(SESSION_ID, "web");

        // 节流以「上次写入时间」为基准，窗口内的重复请求不应再写缓存
        assertEquals(1, service.writes);
    }

    @Test
    void shouldWriteAgainAfterThrottleWindow() throws Exception {
        service.touch(SESSION_ID, "web");
        // 把上次写入时间推到窗口之外，模拟「用户持续操作、窗口已过」
        Field field = SessionActivityServiceImpl.class.getDeclaredField("lastWriteAt");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Long> lastWriteAt = (Map<String, Long>) field.get(service);
        lastWriteAt.put(SESSION_ID, System.currentTimeMillis() - 2 * 60_000L);

        service.touch(SESSION_ID, "web");

        // 若节流基准误用「上次请求时间」，这里会一直是 1 次，缓存永远不更新
        assertEquals(2, service.writes);
    }

    @Test
    void shouldNotJudgeIdleWithoutActivityRecord() {
        assertFalse(service.isIdleExpired(SESSION_ID, 3600));
    }

    @Test
    void shouldJudgeIdleWhenRecordExceedsThreshold() {
        service.store.put(SESSION_ID, System.currentTimeMillis() - 2 * ONE_HOUR_MILLIS);

        assertTrue(service.isIdleExpired(SESSION_ID, 3600));
    }

    @Test
    void shouldNotJudgeIdleWithinThreshold() {
        service.store.put(SESSION_ID, System.currentTimeMillis() - 60_000L);

        assertFalse(service.isIdleExpired(SESSION_ID, 3600));
    }

    @Test
    void shouldSkipCacheLookupAfterLocalWrite() {
        service.touch(SESSION_ID, "web");
        int readsBefore = service.reads;

        assertFalse(service.isIdleExpired(SESSION_ID, 3600));
        // 本实例刚写过缓存，判闲置走快路径，不应再回源
        assertEquals(readsBefore, service.reads);
    }

    @Test
    void shouldNeverJudgeIdleWhenThresholdDisabled() {
        assertFalse(service.isIdleExpired(SESSION_ID, 0));
        assertEquals(0, service.reads);
    }

    /**
     * 用内存映射替换缓存读写，便于脱离 Redis 验证节流与判定逻辑
     */
    private static class TestableSessionActivityService extends SessionActivityServiceImpl {

        private final Map<String, Long> store = new ConcurrentHashMap<>();

        private int reads;

        private int writes;

        TestableSessionActivityService(ClientPolicyCache clientPolicyCache) {
            super(clientPolicyCache);
        }

        @Override
        protected Long readLastActive(String sessionId) {
            reads++;
            return store.get(sessionId);
        }

        @Override
        protected void writeLastActive(String sessionId, long value, long ttlSeconds) {
            writes++;
            store.put(sessionId, value);
        }
    }
}
