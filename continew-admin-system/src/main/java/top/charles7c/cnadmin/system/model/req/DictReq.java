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

import top.charles7c.cnadmin.common.base.BaseReq;
import top.charles7c.cnadmin.common.constant.RegexConsts;

/**
 * 创建或修改字典信息
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Data
@Schema(description = "创建或修改字典信息")
public class DictReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称", example = "公告类型")
    @NotBlank(message = "字典名称不能为空")
    @Pattern(regexp = RegexConsts.GENERAL_NAME, message = "字典名称长度为 2 到 30 位，可以包含中文、字母、数字、下划线，短横线")
    private String name;

    /**
     * 字典编码
     */
    @Schema(description = "字典编码", example = "announcement_type")
    @NotBlank(message = "字典编码不能为空")
    @Pattern(regexp = RegexConsts.GENERAL_CODE, message = "字典编码长度为 2 到 30 位，可以包含字母、数字，下划线，以字母开头")
    private String code;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "公告类型描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;
}