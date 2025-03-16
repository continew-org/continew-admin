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
 * 短信记录查询条件
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Data
@Schema(description = "短信记录查询条件")
public class SmsRecordQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置id
     */
    @Schema(description = "配置id")
    @Query(type = QueryType.EQ)
    private Long configId;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @Query(type = QueryType.EQ)
    private String phone;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    @Query(type = QueryType.EQ)
    private Boolean status;
}