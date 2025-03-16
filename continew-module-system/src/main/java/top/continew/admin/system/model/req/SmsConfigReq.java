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

import jakarta.validation.constraints.*;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建或修改短信服务配置参数
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Data
@Schema(description = "创建或修改短信服务配置参数")
public class SmsConfigReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    @Length(max = 255, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 厂商名称标识
     */
    @Schema(description = "厂商名称标识")
    @NotBlank(message = "厂商名称标识不能为空")
    @Length(max = 50, message = "厂商名称标识长度不能超过 {max} 个字符")
    private String supplier;

    /**
     * Access Key 或 API Key
     */
    @Schema(description = "Access Key 或 API Key")
    @NotBlank(message = "Access Key 或 API Key不能为空")
    @Length(max = 255, message = "Access Key 或 API Key长度不能超过 {max} 个字符")
    private String accessKeyId;

    /**
     * Access Secret 或 API Secret
     */
    @Schema(description = "Access Secret 或 API Secret")
    @NotBlank(message = "Access Secret 或 API Secret不能为空")
    @Length(max = 255, message = "Access Secret 或 API Secret长度不能超过 {max} 个字符")
    private String accessKeySecret;

    /**
     * 短信签名
     */
    @Schema(description = "短信签名")
    @NotBlank(message = "短信签名不能为空")
    @Length(max = 100, message = "短信签名长度不能超过 {max} 个字符")
    private String signature;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID")
    @NotBlank(message = "模板 ID不能为空")
    @Length(max = 50, message = "模板 ID长度不能超过 {max} 个字符")
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
    @Length(max = 10000, message = "各个厂商独立配置长度不能超过 {max} 个字符")
    private String supplierConfig;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    @NotNull(message = "是否启用不能为空")
    private Boolean isEnable;
}