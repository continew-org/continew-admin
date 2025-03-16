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

package top.continew.admin.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.starter.data.core.annotation.Query;
import top.continew.starter.data.core.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信服务配置查询条件
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Data
@Schema(description = "短信服务配置查询条件")
public class SmsConfigQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称")
    @Query(type = QueryType.LIKE)
    private String name;

    /**
     * 厂商名称标识
     */
    @Schema(description = "厂商名称标识")
    @Query(type = QueryType.EQ)
    private String supplier;

    /**
     * Access Key 或 API Key
     */
    @Schema(description = "Access Key 或 API Key")
    @Query(type = QueryType.EQ)
    private String accessKeyId;

    /**
     * 短信签名
     */
    @Schema(description = "短信签名")
    @Query(type = QueryType.EQ)
    private String signature;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID")
    @Query(type = QueryType.EQ)
    private String templateId;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    @Query(type = QueryType.EQ)
    private Boolean isEnable;
}