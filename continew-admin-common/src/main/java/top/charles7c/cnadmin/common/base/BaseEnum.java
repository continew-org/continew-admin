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

package top.charles7c.cnadmin.common.base;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 枚举基类
 *
 * @param <V>
 *            value 类型
 * @param <D>
 *            description 类型
 * @author Charles7c
 * @since 2023/2/5 20:44
 */
public interface BaseEnum<V extends Serializable> extends IEnum<V> {

    /**
     * 枚举描述
     *
     * @return 枚举描述
     */
    String getDescription();

    /**
     * 颜色
     *
     * @return 颜色
     */
    default String getColor() {
        return null;
    }
}
