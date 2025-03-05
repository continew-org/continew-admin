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

package top.continew.admin.system.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.model.resp.BaseDetailResp;
import top.continew.admin.common.enums.DisEnableStatusEnum;

import java.io.Serial;
import java.util.List;

/**
 * 终端信息
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/03 16:04
 */
@Data
@Schema(description = "终端信息")
public class ClientResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 终端 ID
     */
    @Schema(description = "终端 ID", example = "ef51c9a3e9046c4f2ea45142c8a8344a")
    private String clientId;

    /**
     * 终端 Key
     */
    @Schema(description = "终端 Key", example = "PC")
    private String clientKey;

    /**
     * 终端秘钥
     */
    @Schema(description = "终端秘钥", example = "dd77ab1e353a027e0d60ce3b151e8642")
    private String clientSecret;

    /**
     * 认证类型
     */
    @Schema(description = "认证类型", example = "ACCOUNT")
    private List<String> authType;

    /**
     * 终端类型
     */
    @Schema(description = "终端类型", example = "PC")
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
}