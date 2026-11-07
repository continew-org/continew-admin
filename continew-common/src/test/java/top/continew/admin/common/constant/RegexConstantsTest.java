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

package top.continew.admin.common.constant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.hutool.core.util.ReUtil;
import org.junit.jupiter.api.Test;

/**
 * 用户名正则边界测试
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RegexConstantsTest {

    @Test
    void shouldMatchUsernameWithMaxLength64() {
        assertTrue(ReUtil.isMatch(RegexConstants.USERNAME, "a" + "b".repeat(63)));
    }

    @Test
    void shouldRejectUsernameWithLength65() {
        assertFalse(ReUtil.isMatch(RegexConstants.USERNAME, "a" + "b".repeat(64)));
    }

    @Test
    void shouldRejectUsernameWithLength3() {
        assertFalse(ReUtil.isMatch(RegexConstants.USERNAME, "abc"));
    }

    @Test
    void shouldRejectUsernameNotStartingWithLetter() {
        assertFalse(ReUtil.isMatch(RegexConstants.USERNAME, "1abc"));
    }

    @Test
    void shouldMatchCommonUsername() {
        assertTrue(ReUtil.isMatch(RegexConstants.USERNAME, "admin123"));
    }
}
