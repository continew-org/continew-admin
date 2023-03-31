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

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import top.charles7c.cnadmin.common.base.BaseRequest;
import top.charles7c.cnadmin.common.constant.RegexConsts;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.enums.GenderEnum;

/**
 * 创建或修改用户信息
 *
 * @author Charles7c
 * @since 2023/2/20 21:03
 */
@Data
@Schema(description = "创建或修改用户信息")
public class UserRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = RegexConsts.USERNAME, message = "用户名长度为 4 到 16 位，可以包含字母、数字，下划线，以字母开头")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @NotBlank(message = "昵称不能为空")
    @Pattern(regexp = RegexConsts.GENERAL_NAME, message = "昵称长度为 1 到 20 位，可以包含中文、字母、数字、下划线，短横线")
    private String nickname;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    @Pattern(regexp = RegexConsts.EMAIL, message = "邮箱格式错误")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    @Pattern(regexp = RegexConsts.MOBILE, message = "手机号码格式错误")
    private String phone;

    /**
     * 性别（0：未知，1：男，2：女）
     */
    @Schema(description = "性别（0：未知，1：男，2：女）", type = "Integer", allowableValues = {"0", "1", "2"})
    @NotNull(message = "性别非法")
    private GenderEnum gender;

    /**
     * 所属部门
     */
    @Schema(description = "所属部门")
    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    /**
     * 所属角色
     */
    @Schema(description = "所属角色")
    @NotEmpty(message = "所属角色不能为空")
    private List<Long> roleIds;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态（1：启用，2：禁用）
     */
    @Schema(description = "状态（1：启用，2：禁用）", type = "Integer", allowableValues = {"1", "2"})
    private DisEnableStatusEnum status;
}
