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
import top.continew.admin.common.model.resp.BaseResp;
import top.continew.starter.security.mask.annotation.JsonMask;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 短信服务配置信息
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Data
@Schema(description = "短信服务配置信息")
public class SmsConfigResp extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 厂商名称标识
     */
    @Schema(description = "厂商名称标识")
    private String supplier;

    /**
     * Access Key 或 API Key
     */
    @Schema(description = "Access Key 或 API Key")
    private String accessKeyId;

    /**
     * Access Secret 或 API Secret
     */
    @Schema(description = "Access Secret 或 API Secret")
    @JsonMask(left = 4, right = 4, character = '*')
    private String accessKeySecret;

    /**
     * 短信签名
     */
    @Schema(description = "短信签名")
    private String signature;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID")
    private String templateId;

    /**
     * 负载均衡权重
     */
    @Schema(description = "负载均衡权重")
    private Integer weight;

    /**
     * 短信自动重试间隔时间（秒）
     */
    @Schema(description = "短信自动重试间隔时间（秒）")
    private Integer retryInterval;

    /**
     * 短信重试次数
     */
    @Schema(description = "短信重试次数")
    private Integer maxRetries;

    /**
     * 当前厂商的发送数量上限
     */
    @Schema(description = "当前厂商的发送数量上限")
    private Integer maximum;

    /**
     * 各个厂商独立配置
     */
    @Schema(description = "各个厂商独立配置")
    private String supplierConfig;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean isEnable;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private Long updateUser;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}