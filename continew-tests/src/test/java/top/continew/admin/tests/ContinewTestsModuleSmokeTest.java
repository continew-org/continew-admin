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

package top.continew.admin.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.hutool.core.util.ReUtil;
import org.junit.jupiter.api.Test;
import top.continew.admin.common.constant.RegexConstants;

/**
 * 集中测试模块冒烟测试
 *
 * <p>验证模块可编译、测试可执行，并确认被测模块的依赖在测试类路径上可用。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class ContinewTestsModuleSmokeTest {

    @Test
    void shouldLoadCommonModuleConstantsOnTestClasspath() {
        assertEquals("^[a-zA-Z][a-zA-Z0-9_]{3,63}$", RegexConstants.USERNAME);
    }

    @Test
    void shouldUseDeclaredDependenciesInAssertion() {
        assertTrue(ReUtil.isMatch(RegexConstants.USERNAME, "admin123"));
        assertFalse(ReUtil.isMatch(RegexConstants.USERNAME, "1admin"));
    }
}
