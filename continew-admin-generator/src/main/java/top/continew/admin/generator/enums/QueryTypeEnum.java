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
 * 查询类型枚举
 *
 * @author Charles7c
 * @since 2023/8/6 10:49
 */
@Getter
@RequiredArgsConstructor
public enum QueryTypeEnum implements IBaseEnum<Integer> {

    /**
     * 等于 =，例如：WHERE age = 18
     */
    EQ(1, "="),

    /**
     * 不等于 !=，例如：WHERE age != 18
     */
    NE(2, "!="),

    /**
     * 大于 >，例如：WHERE age > 18
     */
    GT(3, ">"),

    /**
     * 大于等于 >= ，例如：WHERE age >= 18
     */
    GE(4, ">="),

    /**
     * 小于 <，例如：WHERE age < 18
     */
    LT(5, "<"),

    /**
     * 小于等于 <=，例如：WHERE age <= 18
     */
    LE(6, "<="),

    /**
     * 范围查询，例如：WHERE age BETWEEN 10 AND 18
     */
    BETWEEN(7, "BETWEEN"),

    /**
     * LIKE '%值%'，例如：WHERE nickname LIKE '%s%'
     */
    LIKE(8, "LIKE '%s%'"),

    /**
     * LIKE '%值'，例如：WHERE nickname LIKE '%s'
     */
    LIKE_LEFT(9, "LIKE '%s'"),

    /**
     * LIKE '值%'，例如：WHERE nickname LIKE 's%'
     */
    LIKE_RIGHT(10, "LIKE 's%'"),

    /**
     * 包含查询，例如：WHERE age IN (10, 20, 30)
     */
    IN(11, "IN"),

    /**
     * 不包含查询，例如：WHERE age NOT IN (20, 30)
     */
    NOT_IN(12, "NOT IN"),

    /**
     * 空查询，例如：WHERE email IS NULL
     */
    IS_NULL(13, "IS NULL"),

    /**
     * 非空查询，例如：WHERE email IS NOT NULL
     */
    IS_NOT_NULL(14, "IS NOT NULL"),;

    private final Integer value;
    private final String description;
}
