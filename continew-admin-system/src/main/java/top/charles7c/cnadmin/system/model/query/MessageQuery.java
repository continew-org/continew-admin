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

package top.charles7c.cnadmin.system.model.query;

import java.io.Serializable;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.cnadmin.common.annotation.Query;
import top.charles7c.cnadmin.common.enums.QueryTypeEnum;

/**
 * 消息查询条件
 *
 * @author BULL_BCLS
 * @since 2023/10/15 19:05
 */
@Data
@Schema(description = "消息查询条件")
public class MessageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @Query(type = QueryTypeEnum.EQUAL)
    private Long id;

    /**
     * 类型（取值于字典 message_type）
     */
    @Schema(description = "类型（取值于字典 message_type）", example = "1")
    @Query(type = QueryTypeEnum.EQUAL)
    private String type;

    /**
     * 主题
     */
    @Schema(description = "主题", example = "欢迎 xxx")
    @Query(type = QueryTypeEnum.INNER_LIKE)
    private String title;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long uid;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读", example = "true")
    private Boolean readStatus;
}