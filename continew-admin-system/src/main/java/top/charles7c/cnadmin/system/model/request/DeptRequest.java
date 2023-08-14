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

package top.charles7c.cnadmin.system.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import top.charles7c.cnadmin.common.base.BaseRequest;
import top.charles7c.cnadmin.common.base.ValidateGroup;
import top.charles7c.cnadmin.common.constant.RegexConsts;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 创建或修改部门信息
 *
 * @author Charles7c
 * @since 2023/1/24 00:21
 */
@Data
@Schema(description = "创建或修改部门信息")
public class DeptRequest extends BaseRequest {

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
    @Pattern(regexp = RegexConsts.GENERAL_NAME, message = "部门名称长度为 1 到 20 位，可以包含中文、字母、数字、下划线，短横线")
    private String name;

    /**
     * 部门排序
     */
    @Schema(description = "部门排序", example = "1")
    @NotNull(message = "部门排序不能为空")
    private Integer sort;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "测试部描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态（1启用 2禁用）
     */
    @Schema(description = "状态（1启用 2禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    private DisEnableStatusEnum status;

    /**
     * 祖级列表
     */
    @Schema(description = "祖级列表")
    private String ancestors;
}
