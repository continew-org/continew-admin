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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RedissonClient;
import top.continew.admin.auth.service.impl.SessionActivityServiceImpl;
import top.continew.admin.auth.support.ClientPolicyCache;
import top.continew.starter.cache.redisson.util.RedisUtils;

import java.util.List;
import java.util.Map;

/**
 * 会话活跃时间批量读取测试。
 *
 * <p>覆盖批量查询的空白 ID 过滤、单批失败降级（丢本批不影响整体）与
 * 多批响应的按位对齐。Redisson 批处理链以 Mockito 模拟。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class SessionActivityBatchReadTest {

    private ClientPolicyCache clientPolicyCache;
    private MockedStatic<RedisUtils> redisUtilsMock;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        clientPolicyCache = mock(ClientPolicyCache.class);
        when(clientPolicyCache.get(any()))
            .thenReturn(new ClientPolicyCache.ClientPolicy(3600L, 2_592_000L));
        // RedisUtils 静态块经 SpringUtil 取 RedissonClient，先桩 SpringUtil 再模拟 RedisUtils
        springUtilMock = mockStatic(SpringUtil.class);
        redisUtilsMock = mockStatic(RedisUtils.class);
    }

    @AfterEach
    void tearDown() {
        redisUtilsMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldReturnEmptyMapForNullOrBlankSessionIds() {
        SessionActivityServiceImpl service = new SessionActivityServiceImpl(clientPolicyCache);

        assertEquals(Map.of(), service.getLastActiveTimes(null));
        assertEquals(Map.of(), service.getLastActiveTimes(List.of("", "  ")));
    }

    @Test
    void shouldMapResponsesByPositionAndSkipNullEntries() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RBatch batch = mock(RBatch.class);
        RBucketAsync<Object> bucket = mock(RBucketAsync.class);
        @SuppressWarnings("rawtypes")
        BatchResult batchResult = mock(BatchResult.class);
        redisUtilsMock.when(RedisUtils::getClient).thenReturn(redissonClient);
        when(redissonClient.createBatch()).thenReturn(batch);
        when(batch.getBucket(any())).thenReturn(bucket);
        when(batch.execute()).thenReturn(batchResult);
        when(batchResult.getResponses())
            .thenReturn(java.util.Arrays.asList("1700000000000", null, "1700000005000"));

        SessionActivityServiceImpl service = new SessionActivityServiceImpl(clientPolicyCache);

        Map<String, Long> result = service.getLastActiveTimes(List.of("s1", "s2", "s3"));

        assertEquals(2, result.size());
        assertEquals(1700000000000L, result.get("s1"));
        assertEquals(1700000005000L, result.get("s3"));
    }

    @Test
    void shouldDegradeToPartialResultWhenBatchFails() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RBatch failingBatch = mock(RBatch.class);
        RBucketAsync<Object> bucket = mock(RBucketAsync.class);
        redisUtilsMock.when(RedisUtils::getClient).thenReturn(redissonClient);
        when(redissonClient.createBatch()).thenReturn(failingBatch, failingBatch);
        when(failingBatch.getBucket(any())).thenReturn(bucket);
        // 单批失败只丢本批：3 个 ID 同批，全部降级为空结果，不影响调用方
        when(failingBatch.execute()).thenThrow(new RuntimeException("redis down"));

        SessionActivityServiceImpl service = new SessionActivityServiceImpl(clientPolicyCache);

        Map<String, Long> result = service.getLastActiveTimes(List.of("bad1", "bad2", "bad3"));

        assertEquals(Map.of(), result);
    }
}
