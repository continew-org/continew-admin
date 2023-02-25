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

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import top.charles7c.cnadmin.common.base.BaseDetailVO;
import top.charles7c.cnadmin.common.config.easyexcel.ExcelBaseEnumConverter;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 岗位详情信息
 *
 * @author Charles7c
 * @since 2023/2/25 22:35
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "岗位详情信息")
public class PostDetailVO extends BaseDetailVO {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位 ID
     */
    @Schema(description = "岗位 ID")
    @ExcelProperty(value = "岗位ID")
    private Long postId;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称")
    @ExcelProperty(value = "岗位名称")
    private String postName;

    /**
     * 岗位排序
     */
    @Schema(description = "岗位排序")
    @ExcelProperty(value = "岗位排序")
    private Integer postSort;

    /**
     * 状态（1启用 2禁用）
     */
    @Schema(description = "状态（1启用 2禁用）")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class)
    private DisEnableStatusEnum status;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @ExcelProperty(value = "描述")
    private String description;
}
