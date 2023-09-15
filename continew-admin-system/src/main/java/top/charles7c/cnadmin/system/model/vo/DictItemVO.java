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

package top.charles7c.cnadmin.system.model.vo;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.cnadmin.common.base.BaseVO;

/**
 * 字典项信息
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Data
@Schema(description = "字典项信息")
public class DictItemVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * 字典标签
     */
    @Schema(description = "字典标签", example = "通知")
    private String label;

    /**
     * 字典值
     */
    @Schema(description = "字典值", example = "1")
    private String value;

    /**
     * 背景颜色
     */
    @Schema(description = "背景颜色", example = "blue")
    private String color;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "通知描述信息")
    private String description;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;
}