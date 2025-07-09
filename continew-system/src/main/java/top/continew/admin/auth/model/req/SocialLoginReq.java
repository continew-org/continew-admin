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

package top.continew.admin.auth.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;

/**
 * 第三方账号登录请求参数
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/25 15:43
 */
@Data
@Schema(description = "第三方账号登录请求参数")
public class SocialLoginReq extends LoginReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 第三方登录平台
     */
    @Schema(description = "第三方登录平台", example = "gitee")
    @NotBlank(message = "第三方登录平台不能为空")
    private String source;

    /**
     * 授权码
     */
    @Schema(description = "授权码", example = "a08d33e9e577fb339de027499784ed4e871d6f62ae65b459153e906ab546bd56")
    @NotBlank(message = "授权码不能为空")
    private String code;

    /**
     * 状态码
     */
    @Schema(description = "状态码", example = "2ca8d8baf437eb374efaa1191a3d")
    @NotBlank(message = "状态码不能为空")
    private String state;
}
