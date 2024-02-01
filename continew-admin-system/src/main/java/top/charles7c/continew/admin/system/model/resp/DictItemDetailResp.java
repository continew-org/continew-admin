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

package top.charles7c.continew.admin.system.model.resp;

import java.io.Serial;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import top.charles7c.continew.starter.extension.crud.model.resp.BaseDetailResp;

/**
 * 字典项详情信息
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "字典项详情信息")
public class DictItemDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签
     */
    @Schema(description = "标签", example = "通知")
    @ExcelProperty(value = "标签", order = 2)
    private String label;

    /**
     * 值
     */
    @Schema(description = "值", example = "1")
    @ExcelProperty(value = "值", order = 3)
    private String value;

    /**
     * 标签颜色
     */
    @Schema(description = "标签颜色", example = "blue")
    @ExcelProperty(value = "标签颜色", order = 4)
    private String color;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序", order = 5)
    private Integer sort;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "通知描述信息")
    @ExcelProperty(value = "描述", order = 6)
    private String description;

    /**
     * 字典 ID
     */
    @Schema(description = "字典 ID", example = "1")
    private Long dictId;

    /**
     * 字典名称
     */
    @Schema(hidden = true)
    @ExcelProperty(value = "字典名称", order = 7)
    private String dictName;

    /**
     * 字典编码
     */
    @Schema(hidden = true)
    @ExcelProperty(value = "字典编码", order = 8)
    private String dictCode;
}