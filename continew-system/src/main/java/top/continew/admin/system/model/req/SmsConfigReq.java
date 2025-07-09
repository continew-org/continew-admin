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
import top.continew.admin.common.enums.DisEnableStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信配置创建或修改请求参数
 *
 * @author luoqiz
 * @author Charles7c
 * @since 2025/03/15 18:41
 */
@Data
@Schema(description = "短信配置创建或修改请求参数")
public class SmsConfigReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "短信配置1")
    @NotBlank(message = "名称不能为空")
    @Length(max = 100, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 厂商
     */
    @Schema(description = "厂商", example = "cloopen")
    @NotNull(message = "厂商无效")
    private String supplier;

    /**
     * Access Key
     */
    @Schema(description = "Access Key", example = "7aaf0708674db3ee05676ecbc2f31b7b")
    @NotBlank(message = "Access Key 不能为空")
    @Length(max = 255, message = "Access Key 长度不能超过 {max} 个字符")
    private String accessKey;

    /**
     * Secret Key
     */
    @Schema(description = "Secret Key", example = "7fd47ade9ae54cddb222222sdsdss57be")
    @NotBlank(message = "Secret Key 不能为空")
    @Length(max = 255, message = "Secret Key 长度不能超过 {max} 个字符")
    private String secretKey;

    /**
     * 短信签名
     */
    @Schema(description = "短信签名", example = "")
    @Length(max = 100, message = "短信签名长度不能超过 {max} 个字符")
    private String signature;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID", example = "1")
    @NotBlank(message = "模板 ID 不能为空")
    @Length(max = 50, message = "模板 ID 长度不能超过 {max} 个字符")
    private String templateId;

    /**
     * 负载均衡权重
     */
    @Schema(description = "负载均衡权重", example = "1")
    private Integer weight;

    /**
     * 重试间隔（单位：秒）
     */
    @Schema(description = "重试间隔（单位：秒）", example = "5")
    private Integer retryInterval;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数", example = "0")
    private Integer maxRetries;

    /**
     * 发送上限
     */
    @Schema(description = "发送上限")
    private Integer maximum;

    /**
     * 各个厂商独立配置
     */
    @Schema(description = "各个厂商独立配置", example = "")
    @Length(max = 65535, message = "各个厂商独立配置长度不能超过 {max} 个字符")
    private String supplierConfig;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;
}