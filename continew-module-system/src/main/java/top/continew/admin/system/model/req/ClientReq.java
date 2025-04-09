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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.enums.DisEnableStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建或修改终端参数
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/03 16:04
 */
@Data
@Schema(description = "创建或修改终端参数")
public class ClientReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 终端 Key
     */
    @Schema(description = "终端 Key", example = "PC")
    @NotBlank(message = "终端Key不能为空")
    @Length(max = 32, message = "终端Key长度不能超过 {max} 个字符")
    private String clientKey;

    /**
     * 终端秘钥
     */
    @Schema(description = "终端秘钥", example = "dd77ab1e353a027e0d60ce3b151e8642")
    @NotBlank(message = "终端秘钥不能为空")
    @Length(max = 255, message = "终端秘钥长度不能超过 {max} 个字符")
    private String clientSecret;

    /**
     * 认证类型
     */
    @Schema(description = "认证类型", example = "ACCOUNT")
    @NotEmpty(message = "认证类型不能为空")
    private List<String> authType;

    /**
     * 终端类型
     */
    @Schema(description = "终端类型", example = "PC")
    @NotBlank(message = "终端类型不能为空")
    @Length(max = 32, message = "终端类型长度不能超过 {max} 个字符")
    private String clientType;

    /**
     * Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）
     */
    @Schema(description = "Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）", example = "1800")
    private Long activeTimeout;

    /**
     * Token 有效期（单位：秒，-1：永不过期）
     */
    @Schema(description = "Token 有效期（单位：秒，-1：永不过期）", example = "86400")
    private Long timeout;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 终端 ID
     */
    @Schema(hidden = true)
    private String clientId;
}