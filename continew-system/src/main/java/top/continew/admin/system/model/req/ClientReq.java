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

import cn.sticki.spel.validator.constrain.SpelNotNull;
import cn.sticki.spel.validator.jakarta.SpelValid;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;
import top.continew.admin.system.enums.LogoutModeEnum;
import top.continew.admin.system.enums.ReplacedRangeEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 客户端创建或修改请求参数
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/03 16:04
 */
@Data
@SpelValid
@Schema(description = "客户端创建或修改请求参数")
public class ClientReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端类型
     */
    @Schema(description = "客户端类型", example = "PC")
    @NotBlank(message = "客户端类型不能为空")
    @Length(max = 32, message = "客户端类型长度不能超过 {max} 个字符")
    private String clientType;

    /**
     * 认证类型
     */
    @Schema(description = "认证类型", example = "ACCOUNT")
    @NotEmpty(message = "认证类型不能为空")
    private List<String> authType;

    /**
     * Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）
     */
    @Schema(description = "Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）", example = "1800")
    @NotNull(message = "Token 最低活跃频率不能为空")
    private Long activeTimeout;

    /**
     * Token 有效期（单位：秒，-1：永不过期）
     */
    @Schema(description = "Token 有效期（单位：秒，-1：永不过期）", example = "86400")
    @NotNull(message = "Token 有效期不能为空")
    private Long timeout;

    /** 是否启用 Refresh Token；创建时未填写则默认开启，修改时为空表示不更新。 */
    @Schema(description = "是否启用 Refresh Token", example = "true")
    private Boolean isEnableRefreshToken;

    /** Refresh Token 绝对有效期（单位：秒）；创建时未填写则使用系统默认值。 */
    @Schema(description = "Refresh Token 有效期（单位：秒）", example = "2592000")
    private Long refreshTokenTimeout;

    /** Refresh Token 传输模式；创建时未填写默认 COOKIE，修改时为空表示不更新。 */
    @Schema(description = "Refresh Token 传输模式", example = "COOKIE")
    private RefreshTokenModeEnum refreshTokenMode;

    /**
     * 是否允许同一账号多地同时登录（true：允许；false：新登录挤掉旧登录）
     */
    @Schema(description = "是否允许同一账号多地同时登录", example = "true")
    @NotNull(message = "是否允许同一账号多地同时登录不能为空")
    private Boolean isConcurrent;

    /**
     * 顶人下线的范围
     */
    @Schema(description = "顶人下线的范围", example = "ALL_DEVICE_TYPE")
    @SpelNotNull(condition = "#this.isConcurrent == false", message = "顶人下线的范围无效")
    private ReplacedRangeEnum replacedRange;

    /**
     * 同一账号最大登录数量（-1：不限制，只有在 isConcurrent=true，isShare=false 时才有效）
     */
    @Schema(description = "同一账号最大登录数量", example = "-1")
    @NotNull(message = "同一账号最大登录数量不能为空")
    private Integer maxLoginCount;

    /**
     * 溢出人数的下线方式
     */
    @Schema(description = "溢出人数的下线方式", example = "KICKOUT")
    @SpelNotNull(condition = "#this.maxLoginCount != -1", message = "溢出人数的下线方式无效")
    private LogoutModeEnum overflowLogoutMode;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 客户端 ID
     */
    @Schema(hidden = true)
    private String clientId;
}
