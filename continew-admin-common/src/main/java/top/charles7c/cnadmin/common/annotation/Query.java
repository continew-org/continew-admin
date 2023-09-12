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

import java.lang.annotation.*;

import top.charles7c.cnadmin.common.enums.QueryTypeEnum;

/**
 * 查询注解
 *
 * @author Zheng Jie（ELADMIN）
 * @author Charles7c
 * @since 2023/1/15 18:01
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Query {

    /**
     * 属性名（默认和使用该注解的属性的名称一致）
     */
    String property() default "";

    /**
     * 查询类型（等值查询、模糊查询、范围查询等）
     */
    QueryTypeEnum type() default QueryTypeEnum.EQUAL;

    /**
     * 多属性模糊查询，仅支持 String 类型属性
     * <p>
     * 例如：@Query(blurry = {"username", "email"}) 表示根据用户名和邮箱模糊查询
     * </p>
     */
    String[] blurry() default {};
}
