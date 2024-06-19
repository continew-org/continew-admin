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

import cn.hutool.core.lang.RegexPool;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.starter.extension.crud.model.req.BaseReq;
import top.continew.starter.extension.crud.util.ValidateGroup;

import java.io.Serial;

/**
 * 用户导入行数据
 *
 * @author Kils
 * @since 2024-6-17 16:42
 */
@Data
@Schema(description = "用户导入行数据")
public class UserImportRowReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = RegexConstants.USERNAME, message = "用户名长度为 4-64 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String username;

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_NAME, message = "昵称长度为 2-30 个字符，支持中文、字母、数字、下划线，短横线")
    private String nickname;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空", groups = ValidateGroup.Crud.Add.class)
    private String password;

    /**
     * 部门名称
     */
    @NotNull(message = "所属部门不能为空")
    private String deptName;

    /**
     * 角色
     */
    @NotBlank(message = "所属角色不能为空")
    private String roleName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^$|" + RegexPool.EMAIL, message = "邮箱格式错误")
    @Length(max = 255, message = "邮箱长度不能超过 {max} 个字符")
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^$|" + RegexPool.MOBILE, message = "手机号码格式错误")
    private String phone;

    /**
     * 描述
     */
    private String description;
}
