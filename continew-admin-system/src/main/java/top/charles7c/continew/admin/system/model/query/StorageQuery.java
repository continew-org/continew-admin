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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.charles7c.continew.starter.data.core.annotation.Query;
import top.charles7c.continew.starter.data.core.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 存储库查询条件
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Data
@Schema(description = "存储库查询条件")
public class StorageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "本地存储")
    @Query(columns = {"name", "description"}, type = QueryType.LIKE)
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", example = "1")
    private Integer status;
}