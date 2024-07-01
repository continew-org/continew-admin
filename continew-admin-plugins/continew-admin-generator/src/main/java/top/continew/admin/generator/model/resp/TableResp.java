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

package top.continew.admin.generator.model.resp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 表信息
 *
 * @author Charles7c
 * @since 2023/4/12 20:21
 */
@Data
@Schema(description = "表信息")
public class TableResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表名称
     */
    @Schema(description = "表名称", example = "sys_user")
    private String tableName;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "用户表")
    private String comment;

    /**
     * 存储引擎
     */
    @Schema(description = "存储引擎", example = "InnoDB")
    private String engine;

    /**
     * 字符集
     */
    @Schema(description = "字符集", example = "utf8mb4_general_ci")
    private String charset;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;

    /**
     * 是否已配置
     */
    @Schema(description = "是否已配置", example = "true")
    private Boolean isConfiged;

    /**
     * 是否禁用
     */
    @Schema(description = "是否禁用", example = "true")
    private Boolean disabled;

    public Boolean getDisabled() {
        return !isConfiged;
    }
}
