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

package top.charles7c.continew.admin.system.model.query;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.continew.starter.data.mybatis.plus.query.Query;
import top.charles7c.continew.starter.data.mybatis.plus.query.QueryType;

/**
 * 公告查询条件
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Data
@Schema(description = "公告查询条件")
public class AnnouncementQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "这是公告标题")
    @Query(type = QueryType.LIKE)
    private String title;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "1")
    @Query
    private String type;
}