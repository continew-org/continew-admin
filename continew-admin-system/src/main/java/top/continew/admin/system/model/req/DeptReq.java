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

package top.continew.admin.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.starter.extension.crud.model.req.BaseReq;

import java.io.Serial;

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
    @NotNull(message = "上级部门不能为空")
    private Long parentId;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "测试部")
    @NotBlank(message = "名称不能为空")
    @Length(max = 30, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @Min(value = 1, message = "排序最小值为 {value}")
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
