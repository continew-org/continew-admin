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

package top.continew.admin.generator.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 表单类型枚举
 *
 * @author Charles7c
 * @since 1.1.0
 */
@Getter
@RequiredArgsConstructor
public enum FormTypeEnum implements BaseEnum<Integer> {

    /**
     * 输入框
     */
    INPUT(1, "输入框"),

    /**
     * 数字输入框
     */
    INPUT_NUMBER(2, "数字输入框"),

    /**
     * 密码输入框
     */
    INPUT_PASSWORD(3, "密码输入框"),

    /**
     * 文本域
     */
    TEXT_AREA(4, "文本域"),

    /**
     * 下拉框
     */
    SELECT(5, "下拉框"),

    /**
     * 单选框
     */
    RADIO(6, "单选框"),

    /**
     * 开关
     */
    SWITCH(7, "开关"),

    /**
     * 复选框
     */
    CHECK_BOX(8, "复选框"),

    /**
     * 树形选择
     */
    TREE_SELECT(9, "树选择"),

    /**
     * 时间框
     */
    TIME(10, "时间框"),

    /**
     * 日期框
     */
    DATE(11, "日期框"),

    /**
     * 时间框
     */
    DATE_TIME(12, "日期时间框"),;

    private final Integer value;
    private final String description;
}
