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
 * 树结构字段
 *
 * @see cn.hutool.core.lang.tree.TreeNodeConfig
 * @author Charles7c
 * @since 2023/2/26 23:50
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TreeField {

    /**
     * ID 字段名
     *
     * @return ID 字段名
     */
    String value() default "key";

    /**
     * 父 ID 字段名
     *
     * @return 父 ID 字段名
     */
    String parentIdKey() default "parentId";

    /**
     * 名称字段名
     *
     * @return 名称字段名
     */
    String nameKey() default "title";

    /**
     * 排序字段名
     *
     * @return 排序字段名
     */
    String weightKey() default "sort";

    /**
     * 子列表字段名
     *
     * @return 子列表字段名
     */
    String childrenKey() default "children";

    /**
     * 递归深度（< 0 不限制）
     *
     * @return 递归深度
     */
    int deep() default -1;
}
