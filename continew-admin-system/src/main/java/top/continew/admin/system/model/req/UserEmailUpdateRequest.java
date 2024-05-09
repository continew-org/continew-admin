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
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户邮箱修改信息
 *
 * @author Charles7c
 * @since 2023/1/12 20:18
 */
@Data
@Schema(description = "用户邮箱修改信息")
public class UserEmailUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 新邮箱
     */
    @Schema(description = "新邮箱", example = "123456789@qq.com")
    @NotBlank(message = "新邮箱不能为空")
    @Pattern(regexp = RegexPool.EMAIL, message = "邮箱格式错误")
    private String email;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "888888")
    @NotBlank(message = "验证码不能为空")
    @Length(max = 6, message = "验证码非法")
    private String captcha;

    /**
     * 当前密码（加密）
     */
    @Schema(description = "当前密码（加密）", example = "SYRLSszQGcMv4kP2Yolou9zf28B9GDakR9u91khxmR7V++i5A384kwnNZxqgvT6bjT4zqpIDuMFLWSt92hQJJA==")
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;
}
