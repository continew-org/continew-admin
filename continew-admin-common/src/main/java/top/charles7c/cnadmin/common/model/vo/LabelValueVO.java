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

package top.charles7c.cnadmin.common.model.vo;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 键值对信息
 *
 * @param <V>
 * @author Charles7c
 * @since 2023/2/24 22:02
 */
@Data
@NoArgsConstructor
@Schema(description = "键值对信息")
public class LabelValueVO<V> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private String label;

    /**
     * 值
     */
    @Schema(description = "值")
    private V value;

    public LabelValueVO(String label, V value) {
        this.label = label;
        this.value = value;
    }
}
