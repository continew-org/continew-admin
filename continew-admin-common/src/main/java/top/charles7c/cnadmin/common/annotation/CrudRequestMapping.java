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

/**
 * 增删改查请求映射器注解
 *
 * @author Charles7c
 * @since 2023/1/27 9:54
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CrudRequestMapping {

    /**
     * 路径映射 URI（等同于：@RequestMapping("/foo1")）
     */
    String value() default "";

    /**
     * API 列表
     */
    Api[] api() default {Api.PAGE, Api.DETAIL, Api.CREATE, Api.UPDATE, Api.DELETE};

    /**
     * API 枚举
     */
    enum Api {
        /**
         * 所有 API
         */
        ALL,
        /**
         * 分页
         */
        PAGE,
        /**
         * 列表
         */
        LIST,
        /**
         * 详情
         */
        DETAIL,
        /**
         * 新增
         */
        CREATE,
        /**
         * 修改
         */
        UPDATE,
        /**
         * 删除
         */
        DELETE,;
    }
}
