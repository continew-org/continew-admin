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

package top.continew.admin.system.model.req.user;

import cn.hutool.core.lang.RegexPool;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.constant.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Pattern;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户密码修改参数
 *
 * @author Charles7c
 * @since 2023/1/9 23:28
 */
@Data
@Schema(description = "用户密码修改参数")
public class UserPasswordUpdateReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    //    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_NAME, message = "用户名长度为 2-30 个字符，支持中文、字母、数字、下划线，短横线")
    private String username;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "123456789@qq.com")
    //    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = RegexPool.EMAIL, message = "邮箱格式错误")
    private String email;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "888888")
    //    @NotBlank(message = "验证码不能为空")
    @Length(max = 6, message = "验证码非法")
    private String captcha;

    /**
     * 当前密码（加密）
     */
    @Schema(description = "当前密码（加密）", example = "E7c72TH+LDxKTwavjM99W1MdI9Lljh79aPKiv3XB9MXcplhm7qJ1BJCj28yaflbdVbfc366klMtjLIWQGqb0qw==")
//    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;

    /**
     * 新密码（加密）
     */
    @Schema(description = "新密码（加密）", example = "Gzc78825P5baH190lRuZFb9KJxRt/psN2jiyOMPoc5WRcCvneCwqDm3Q33BZY56EzyyVy7vQu7jQwYTK4j1+5w==")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
