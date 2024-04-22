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

import top.continew.starter.data.mybatis.plus.base.IBaseEnum;

/**
 * 表单类型枚举
 *
 * @author Charles7c
 * @since 2023/8/6 10:49
 */
@Getter
@RequiredArgsConstructor
public enum FormTypeEnum implements IBaseEnum<Integer> {

    /**
     * 文本框
     */
    TEXT(1, "文本框"),
    /**
     * 文本域
     */
    TEXT_AREA(2, "文本域"),
    /**
     * 下拉框
     */
    SELECT(3, "下拉框"),
    /**
     * 单选框
     */
    RADIO(4, "单选框"),
    /**
     * 日期框
     */
    DATE(5, "日期框"),
    /**
     * 日期时间框
     */
    DATE_TIME(6, "日期时间框"),;

    private final Integer value;
    private final String description;
}
