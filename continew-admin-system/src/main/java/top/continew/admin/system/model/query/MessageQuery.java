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
import top.continew.starter.data.core.annotation.QueryIgnore;
import top.continew.starter.data.core.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息查询条件
 *
 * @author Bull-BCLS
 * @since 2023/10/15 19:05
 */
@Data
@Schema(description = "消息查询条件")
public class MessageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "欢迎注册 xxx")
    @Query(type = QueryType.LIKE)
    private String title;

    /**
     * 类型
     */
    @Schema(description = "类型（1：系统消息）", example = "1")
    private Integer type;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读", example = "true")
    @QueryIgnore
    private Boolean isRead;

    /**
     * 用户 ID
     */
    @Schema(hidden = true)
    @QueryIgnore
    private Long userId;
}