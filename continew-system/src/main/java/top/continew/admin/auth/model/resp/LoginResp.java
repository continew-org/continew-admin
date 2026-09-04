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

package top.continew.admin.auth.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应参数
 *
 * @author Charles7c
 * @since 2022/12/21 20:42
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "登录响应参数")
public class LoginResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 短期访问令牌。前端只需要持久化该令牌，过期后调用 /auth/refresh 获取新令牌。
     */
    @Schema(description = "Access Token",
        example = "eyJhbGciOiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb2dpbiIsImxvZ2luSWQiOjF9.KUPOYm-2wfuLUSfEEAbpGE527fzmkAJG7sMNcQ0pUZ8")
    private String accessToken;

    /** 访问令牌类型，当前固定为 Bearer。 */
    @Schema(description = "Access Token 类型", example = "Bearer")
    private String tokenType;

    /** Access Token 有效期（秒）；-1 表示由 Sa-Token 配置为永不过期。 */
    @Schema(description = "Access Token 有效期（秒）", example = "900")
    private Long expiresIn;

    /** Refresh Token 的绝对有效期（秒），用于客户端展示或提前续期提示。 */
    @Schema(description = "Refresh Token 有效期（秒）", example = "2592000")
    private Long refreshExpiresIn;

    /**
     * 当前登录客户端是否启用 Refresh Token。
     *
     * <p>前端不能读取 HttpOnly Cookie，因此必须通过该字段判断 Access Token 过期后
     * 是否允许调用刷新接口；客户端关闭该功能时应直接重新登录。</p>
     */
    @Schema(description = "当前客户端是否启用 Refresh Token", example = "true")
    private Boolean refreshTokenEnabled;

    /**
     * BODY 模式的 Refresh Token，仅供未来 App / 微信小程序使用；浏览器 Cookie 模式为空。
     */
    @Schema(description = "Refresh Token（浏览器 Cookie 模式不返回）", example = "rft_xxx")
    private String refreshToken;

    /**
     * 租户 ID
     */
    @Schema(description = "租户 ID", example = "0")
    private Long tenantId;
}
