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

package top.charles7c.cnadmin.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询注解
 *
 * @author Charles7c
 * @since 2023/1/15 18:01
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * 属性名（默认和使用该注解的属性的名称一致）
     */
    String property() default "";

    /**
     * 查询类型（等值查询、模糊查询、范围查询等）
     */
    Type type() default Type.EQUAL;

    /**
     * 多属性模糊查询，仅支持 String 类型属性，多个属性之间用逗号分隔
     * <p>
     * 例如：@Query(blurry = "username,email") 表示根据用户名和邮箱模糊查询
     * </p>
     */
    String blurry() default "";

    /**
     * 查询类型
     */
    enum Type {
        /**
         * 等值查询，例如：WHERE `age` = 18
         */
        EQUAL,
        /**
         * 非等值查询，例如：WHERE `age` != 18
         */
        NOT_EQUAL,
        /**
         * 大于查询，例如：WHERE `age` > 18
         */
        GREATER_THAN,
        /**
         * 小于查询，例如：WHERE `age` < 18
         */
        LESS_THAN,
        /**
         * 大于等于查询，例如：WHERE `age` >= 18
         */
        GREATER_THAN_OR_EQUAL,
        /**
         * 小于等于查询，例如：WHERE `age` <= 18
         */
        LESS_THAN_OR_EQUAL,
        /**
         * 范围查询，例如：WHERE `age` BETWEEN 10 AND 18
         */
        BETWEEN,
        /**
         * 左模糊查询，例如：WHERE `nickname` LIKE '%张'
         */
        LEFT_LIKE,
        /**
         * 中模糊查询，例如：WHERE `nickname` LIKE '%雪%'
         */
        INNER_LIKE,
        /**
         * 右模糊查询，例如：WHERE `nickname` LIKE '雪%'
         */
        RIGHT_LIKE,
        /**
         * 包含查询，例如：WHERE `age` IN (10, 20, 30)
         */
        IN,
        /**
         * 不包含查询，例如：WHERE `age` NOT IN (20, 30)
         */
        NOT_IN,
        /**
         * 空查询，例如：WHERE `email` IS NULL
         */
        IS_NULL,
        /**
         * 非空查询，例如：WHERE `email` IS NOT NULL
         */
        IS_NOT_NULL,;
    }
}
