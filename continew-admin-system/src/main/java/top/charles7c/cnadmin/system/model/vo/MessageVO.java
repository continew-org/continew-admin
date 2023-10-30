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

package top.charles7c.cnadmin.system.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.cnadmin.common.base.BaseVO;

/**
 * 消息信息
 *
 * @author BULL_BCLS
 * @since 2023/10/15 19:05
 */
@Data
@Schema(description = "消息信息")
public class MessageVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID", example = "1")
    private Long id;

    /**
     * 主题
     */
    @Schema(description = "主题", example = "欢迎 xxx")
    private String title;

    /**
     * 内容
     */
    @Schema(description = "内容", example = "欢迎 xxx")
    private String content;

    /**
     * 类型（取值于字典 message_type）
     */
    @Schema(description = "类型（取值于字典 message_type）", example = "1")
    private String type;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读", example = "true")
    private Boolean readStatus;

    /**
     * 读取时间
     */
    @Schema(description = "读取时间", example = "2023-08-08 23:59:59", type = "string")
    private LocalDateTime readTime;
}