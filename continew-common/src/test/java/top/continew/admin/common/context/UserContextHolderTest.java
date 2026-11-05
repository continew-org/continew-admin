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

package top.continew.admin.common.context;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * 用户上下文归属校验测试。
 *
 * <p>回归：容器线程会被复用，上一请求在其清理逻辑执行前抛异常时，写入的用户上下文残留在
 * ThreadLocal 中。{@code getContext()} 若不比对当前登录态，下一个请求（包括未登录请求）会把
 * 他人的上下文当作自己的读取，造成跨用户串用与越权。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class UserContextHolderTest {

    /**
     * 线程本地中残留的上下文（属用户 2，模拟上一请求写入）
     */
    private UserContext residualContext;

    /**
     * 当前会话中的上下文（属用户 3，模拟当前请求的登录用户）
     */
    private UserContext sessionContext;

    @BeforeEach
    void setUp() {
        residualContext = context(2L);
        sessionContext = context(3L);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clearContext();
    }

    private static UserContext context(Long id) {
        UserContext context = new UserContext();
        context.setId(id);
        return context;
    }

    private static SaSession sessionOf(UserContext context) {
        SaSession session = mock(SaSession.class);
        when(session.getModel(SaSession.USER, UserContext.class)).thenReturn(context);
        return session;
    }

    @Test
    void shouldReturnCachedContextWhenOwnedByCurrentLogin() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(2L);
            UserContextHolder.setContext(residualContext, false);

            assertSame(residualContext, UserContextHolder.getContext());

            // 同一请求内重复读取仍命中线程本地，不回源会话
            UserContextHolder.getContext();
            stpUtil.verify(StpUtil::getSession, never());
        }
    }

    @Test
    void shouldReloadCurrentSessionContextWhenResidualBelongsToAnotherUser() {
        SaSession currentSession = sessionOf(sessionContext);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(3L);
            stpUtil.when(StpUtil::getSession).thenReturn(currentSession);
            UserContextHolder.setContext(residualContext, false);

            assertSame(sessionContext, UserContextHolder.getContext());

            // 回源结果写回缓存：再次读取直接命中缓存，仍是当前用户上下文
            assertSame(sessionContext, UserContextHolder.getContext());
            stpUtil.verify(StpUtil::getSession, times(1));
        }
    }

    @Test
    void shouldNotReturnResidualContextWhenCurrentRequestIsNotLoggedIn() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(null);
            stpUtil.when(StpUtil::getSession)
                .thenThrow(new NotLoginException("sys", "无效凭证", NotLoginException.NOT_TOKEN));
            UserContextHolder.setContext(residualContext, false);
            UserExtraContext residualExtraContext = new UserExtraContext();
            UserContextHolder.setExtraContext(residualExtraContext);

            assertThrows(NotLoginException.class, UserContextHolder::getContext);

            // 额外上下文一并丢弃，未登录线程取不到任何他人数据
            assertNull(UserContextHolder.getUserId());
            assertNotSame(residualExtraContext, UserContextHolder.getExtraContext());
        }
    }

    @Test
    void shouldKeepCachedContextWhenLoginStateIsUnavailable() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            // 异步线程没有 Sa-Token 请求上下文，其缓存多为代码经 TransmittableThreadLocal
            // 显式传递而来（或线程创建时刻自动快照自父线程）
            stpUtil.when(StpUtil::getLoginIdDefaultNull)
                .thenThrow(new SaTokenContextException("未获取到 Request 对象"));
            UserContextHolder.setContext(residualContext, false);

            assertSame(residualContext, UserContextHolder.getContext());

            stpUtil.verify(StpUtil::getSession, never());
        }
    }

    @Test
    void shouldDiscardCachedContextWhenCredentialsAreInvalid() {
        NotLoginException invalidToken = new NotLoginException("sys", "token 无效",
            NotLoginException.INVALID_TOKEN);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            // 请求上下文存在但凭证校验失败（SaJwtException 亦此形态）：不属于"上下文缺失"，
            // 无有效登录态可比对，走归属不符分支：清除缓存并回源
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenThrow(invalidToken);
            stpUtil.when(StpUtil::getSession).thenThrow(invalidToken);
            UserContextHolder.setContext(residualContext, false);

            assertThrows(NotLoginException.class, UserContextHolder::getContext);

            // 残留缓存不会被保留：再次读取仍会回源并抛出，getUserId 返回 null（getSession 共调用 2 次）
            assertNull(UserContextHolder.getUserId());
            stpUtil.verify(StpUtil::getSession, times(2));
        }
    }

    @Test
    void shouldMatchLoginIdRegardlessOfItsNumericType() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            // JWT 载荷解析出的登录标识可能是 String 或 Integer，与 Long 型 userId 不同源
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn("2");
            UserContextHolder.setContext(residualContext, false);

            assertSame(residualContext, UserContextHolder.getContext());

            stpUtil.verify(StpUtil::getSession, never());
        }
    }

    @Test
    void shouldUpdateSessionWithoutTouchingThreadLocal() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            SaSession session = mock(SaSession.class);
            stpUtil.when(() -> StpUtil.getSessionByLoginId(3L, false)).thenReturn(session);
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(2L);
            UserContextHolder.setContext(residualContext, false);

            UserContextHolder.updateSessionContext(sessionContext);

            // 他人会话被同步，当前线程仍持有自己的上下文
            verify(session).set(SaSession.USER, sessionContext);
            assertSame(residualContext, UserContextHolder.getContext());
        }
    }

    @Test
    void shouldNotCreateSessionWhenTargetUserAlreadyLoggedOut() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            // 判空与刷新之间的间隙里目标用户登出：按需创建会话会留下无归属的孤儿会话
            stpUtil.when(() -> StpUtil.getSessionByLoginId(3L, false)).thenReturn(null);

            UserContextHolder.updateSessionContext(sessionContext);

            stpUtil.verify(() -> StpUtil.getSessionByLoginId(3L), never());
        }
    }
}
