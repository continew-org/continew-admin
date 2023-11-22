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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import top.charles7c.cnadmin.common.base.BaseReq;
import top.charles7c.cnadmin.common.base.ValidateGroup;
import top.charles7c.cnadmin.common.constant.RegexConstants;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 创建或修改部门信息
 *
 * @author Charles7c
 * @since 2023/1/24 00:21
 */
@Data
@Schema(description = "创建或修改部门信息")
public class DeptReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 上级部门 ID
     */
    @Schema(description = "上级部门 ID", example = "2")
    @NotNull(message = "上级部门不能为空", groups = ValidateGroup.Crud.Add.class)
    private Long parentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "测试部")
    @NotBlank(message = "部门名称不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_NAME, message = "部门名称长度为 2 到 30 位，可以包含中文、字母、数字、下划线，短横线")
    private String name;

    /**
     * 部门排序
     */
    @Schema(description = "部门排序", example = "1")
    @Min(value = 1, message = "部门排序最小值为 {value}")
    private Integer sort;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "测试部描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    private DisEnableStatusEnum status;

    /**
     * 祖级列表
     */
    @Schema(hidden = true)
    private String ancestors;
}
