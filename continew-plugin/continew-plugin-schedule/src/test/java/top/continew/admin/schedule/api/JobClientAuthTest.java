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

package top.continew.admin.schedule.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.redisson.api.RedissonClient;
import top.continew.admin.schedule.constant.JobConstants;
import top.continew.admin.schedule.exception.ScheduleServerException;
import top.continew.starter.cache.redisson.util.RedisUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * 任务调度客户端认证流程测试。
 *
 * <p>覆盖 Token 缓存命中直返、缓存未命中走密码认证并按 JWT 过期时间写缓存
 * （TTL = exp − now − 60），以及认证失败的 HTTP 非 2xx、响应体缺失、业务状态失败、
 * IO 异常四个分支。Token 以真实 HS256 JWT 构造并经真实 JWTUtil 解析，
 * 网络与缓存经 Mockito 静态模拟隔离。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class JobClientAuthTest {

    private static final String AUTH_URL = "http://localhost:8800/auth/login";
    private static final byte[] JWT_SECRET = "snail-job-client-auth-test-secret".getBytes();

    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<RedisUtils> redisUtilsMock;
    private MockedStatic<HttpUtil> httpUtilMock;

    @BeforeEach
    void setUp() {
        // RedisUtils 的静态块经 SpringUtil 取 RedissonClient，纯单测下先桩 SpringUtil 再模拟 RedisUtils
        springUtilMock = mockStatic(SpringUtil.class);
        RedissonClient redissonClient = mock(RedissonClient.class);
        springUtilMock.when(() -> SpringUtil.getBean(RedissonClient.class))
            .thenReturn(redissonClient);
        redisUtilsMock = mockStatic(RedisUtils.class);
        httpUtilMock = mockStatic(HttpUtil.class);
    }

    @AfterEach
    void tearDown() {
        httpUtilMock.close();
        redisUtilsMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldReturnCachedTokenWithoutAuthenticating() {
        redisUtilsMock.when(() -> RedisUtils.get(JobConstants.AUTH_TOKEN_HEADER))
            .thenReturn("cached-token");

        JobClient client = client();

        assertEquals("cached-token", client.getToken());
        redisUtilsMock.verify(() -> RedisUtils.set(anyString(), any(), any(Duration.class)),
            never());
        httpUtilMock.verify(() -> HttpUtil.createPost(anyString()), never());
    }

    @Test
    void shouldAuthenticateAndCacheTokenByJwtExpiresAtOnCacheMiss() {
        long beforeCall = DateUtil.currentSeconds();
        long expiresAt = beforeCall + 3600;
        String token =
            JWTUtil.createToken(Map.of(RegisteredPayload.EXPIRES_AT, expiresAt), JWT_SECRET);
        redisUtilsMock.when(() -> RedisUtils.get(JobConstants.AUTH_TOKEN_HEADER)).thenReturn(null);
        stubAuthResponse(authBody(token), true);

        JobClient client = client();

        assertEquals(token, client.getToken());
        long afterCall = DateUtil.currentSeconds();
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        redisUtilsMock.verify(() -> RedisUtils
            .set(eq(JobConstants.AUTH_TOKEN_HEADER), eq(token), ttlCaptor.capture()), times(1));
        // TTL = exp − now − 60，now 取自调用区间内任一时刻，以区间上下界断言消除时钟推进抖动
        long ttlSeconds = ttlCaptor.getValue().getSeconds();
        assertTrue(ttlSeconds <= expiresAt - beforeCall - 60);
        assertTrue(ttlSeconds >= expiresAt - afterCall - 60);
    }

    @Test
    void shouldThrowWhenAuthResponseNotOk() {
        redisUtilsMock.when(() -> RedisUtils.get(JobConstants.AUTH_TOKEN_HEADER)).thenReturn(null);
        stubAuthResponse(null, false);

        JobClient client = client();

        ScheduleServerException exception =
            assertThrows(ScheduleServerException.class, client::getToken);
        assertEquals("连接任务调度中心异常", exception.getMessage());
    }

    @Test
    void shouldThrowWhenAuthResponseBodyMissing() {
        redisUtilsMock.when(() -> RedisUtils.get(JobConstants.AUTH_TOKEN_HEADER)).thenReturn(null);
        stubAuthResponse(null, true);

        JobClient client = client();

        ScheduleServerException exception =
            assertThrows(ScheduleServerException.class, client::getToken);
        assertEquals("连接任务调度中心异常", exception.getMessage());
    }

    @Test
    void shouldThrowWithResultMessageWhenAuthStatusFailed() {
        redisUtilsMock.when(() -> RedisUtils.get(JobConstants.AUTH_TOKEN_HEADER)).thenReturn(null);
        stubAuthResponse(JSONUtil.toJsonStr(Map.of("status", 0, "message", "用户名或密码错误")), true);

        JobClient client = client();

        ScheduleServerException exception =
            assertThrows(ScheduleServerException.class, client::getToken);
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void shouldThrowWhenAuthRequestIoFailed() {
        redisUtilsMock.when(() -> RedisUtils.get(JobConstants.AUTH_TOKEN_HEADER)).thenReturn(null);
        HttpRequest request = mock(HttpRequest.class);
        httpUtilMock.when(() -> HttpUtil.createPost(AUTH_URL)).thenReturn(request);
        when(request.body(anyString())).thenReturn(request);
        when(request.execute())
            .thenThrow(new IORuntimeException(new IOException("connection refused")));

        JobClient client = client();

        ScheduleServerException exception =
            assertThrows(ScheduleServerException.class, client::getToken);
        assertEquals("无法连接任务调度中心，请检查调度中心服务", exception.getMessage());
    }

    private static JobClient client() {
        return new JobClient("http://localhost:8800", "admin", "admin");
    }

    /**
     * 桩定认证请求链：createPost → body → execute，isOk 与响应体按分支供给
     *
     * @param body 响应体内容（null 模拟缺失响应体）
     * @param ok   响应是否为 2xx
     */
    private void stubAuthResponse(String body, boolean ok) {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        httpUtilMock.when(() -> HttpUtil.createPost(AUTH_URL)).thenReturn(request);
        when(request.body(anyString())).thenReturn(request);
        when(request.execute()).thenReturn(response);
        when(response.isOk()).thenReturn(ok);
        when(response.body()).thenReturn(body);
    }

    private static String authBody(String token) {
        return JSONUtil.toJsonStr(Map.of("status", 1, "data", Map.of("token", token)));
    }
}
