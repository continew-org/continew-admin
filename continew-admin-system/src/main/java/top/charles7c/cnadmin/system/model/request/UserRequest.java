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

import javax.validation.constraints.*;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import cn.hutool.core.lang.RegexPool;

import top.charles7c.cnadmin.common.base.BaseRequest;
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
     * 用户 ID
     */
    @Schema(description = "角色 ID")
    @Null(message = "新增时，ID 必须为空", groups = Create.class)
    @NotNull(message = "修改时，ID 不能为空", groups = Update.class)
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @Length(max = 32, message = "昵称长度不能超过 {max} 个字符")
    private String nickname;

    /**
     * 性别（0未知 1男 2女）
     */
    @Schema(description = "性别（0未知 1男 2女）", type = "Integer", allowableValues = {"0", "1", "2"})
    @NotNull(message = "性别非法")
    private GenderEnum gender;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    @Pattern(regexp = RegexPool.EMAIL, message = "邮箱格式错误")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    @Pattern(regexp = RegexPool.MOBILE, message = "手机号码格式错误")
    private String phone;

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

    /**
     * 部门 ID
     */
    @Schema(description = "所属部门")
    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    /**
     * 岗位 ID 列表
     */
    @Schema(description = "所属岗位")
    private List<Long> postIds;

    /**
     * 角色 ID 列表
     */
    @Schema(description = "所属角色")
    @NotEmpty(message = "所属角色不能为空")
    private List<Long> roleIds;
}
