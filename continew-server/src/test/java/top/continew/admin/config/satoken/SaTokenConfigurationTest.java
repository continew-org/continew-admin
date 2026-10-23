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

package top.continew.admin.config.satoken;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.exception.PasswordExpiredException;

/**
 * 密码过期拦截分支测试。
 *
 * <p>回归：{@code preHandle} 抛出异常时 {@code afterCompletion} 不会回调，因此必须在抛出前清理
 * 线程本地上下文，否则池化线程会残留上一请求的用户上下文被下一请求复用。</p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
class SaTokenConfigurationTest {

    @Test
    void shouldClearThreadLocalContextBeforeThrowing() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            SaSession saSession = mock(SaSession.class);
            when(saSession.getModel(SaSession.USER, UserContext.class)).thenReturn(null);
            stpUtil.when(StpUtil::getSession).thenReturn(saSession);
            // 只写线程本地，不写 SaSession，避免依赖 Sa-Token 全局上下文
            UserContextHolder.setContext(new UserContext(), false);

            assertThrows(PasswordExpiredException.class,
                SaTokenConfiguration::rejectExpiredPassword);
            // 线程本地已清空：再次读取会回源（此处回源到 mock 的 null）
            assertNull(UserContextHolder.getContext());
        }
    }
}
