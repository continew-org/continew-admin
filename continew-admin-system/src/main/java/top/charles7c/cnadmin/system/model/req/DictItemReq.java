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

package top.charles7c.cnadmin.system.model.req;

import java.io.Serial;

import jakarta.validation.constraints.*;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import top.charles7c.continew.starter.extension.crud.base.BaseReq;

/**
 * 创建或修改字典项信息
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Data
@Schema(description = "创建或修改字典项信息")
public class DictItemReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典标签
     */
    @Schema(description = "字典标签", example = "通知")
    @NotBlank(message = "字典标签不能为空")
    @Length(max = 30, message = "字典标签长度不能超过 {max} 个字符")
    private String label;

    /**
     * 字典值
     */
    @Schema(description = "字典值", example = "1")
    @NotBlank(message = "字典值不能为空")
    @Length(max = 30, message = "字典值长度不能超过 {max} 个字符")
    private String value;

    /**
     * 背景颜色
     */
    @Schema(description = "背景颜色", example = "blue")
    @Length(max = 30, message = "背景颜色长度不能超过 {max} 个字符")
    private String color;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @Min(value = 1, message = "排序最小值为 {value}")
    private Integer sort;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "通知描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 所属字典
     */
    @Schema(description = "所属字典", example = "1")
    @NotNull(message = "所属字典不能为空")
    private Long dictId;
}