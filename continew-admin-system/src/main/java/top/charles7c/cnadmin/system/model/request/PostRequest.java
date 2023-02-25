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
import javax.validation.constraints.Null;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import top.charles7c.cnadmin.common.base.BaseRequest;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 创建或修改岗位信息
 *
 * @author Charles7c
 * @since 2023/2/25 22:31
 */
@Data
@Schema(description = "创建或修改岗位信息")
public class PostRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位 ID
     */
    @Schema(description = "岗位 ID")
    @Null(message = "新增时，ID 必须为空", groups = Create.class)
    @NotNull(message = "修改时，ID 不能为空", groups = Update.class)
    private Long postId;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称")
    @NotBlank(message = "岗位名称不能为空")
    private String postName;

    /**
     * 岗位排序
     */
    @Schema(description = "岗位排序")
    @NotNull(message = "岗位排序不能为空")
    private Integer postSort;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态（1启用 2禁用）
     */
    @Schema(description = "状态（1启用 2禁用）", type = "Integer", allowableValues = {"1", "2"})
    private DisEnableStatusEnum status;
}
