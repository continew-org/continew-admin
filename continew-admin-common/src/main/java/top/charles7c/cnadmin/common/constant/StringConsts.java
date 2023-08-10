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

package top.charles7c.cnadmin.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import cn.hutool.core.text.StrPool;

/**
 * 字符串相关常量
 *
 * @author Charles7c
 * @since 2023/2/10 20:14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringConsts implements StrPool {

    /**
     * 空字符串
     */
    public static final String EMPTY = "";

    /**
     * 空格
     */
    public static final String SPACE = " ";

    /**
     * 分号
     */
    public static final String SEMICOLON = ";";

    /**
     * 星号
     */
    public static final String ASTERISK = "*";

    /**
     * 问号
     */
    public static final String QUESTION_MARK = "?";

    /**
     * 中文逗号
     */
    public static final String CHINESE_COMMA = "，";
}
