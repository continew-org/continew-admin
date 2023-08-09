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

package top.charles7c.cnadmin.tool.model.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.charles7c.cnadmin.tool.model.entity.ColumnMappingDO;
import top.charles7c.cnadmin.tool.model.entity.GenConfigDO;

/**
 * 代码生成配置信息
 *
 * @author Charles7c
 * @since 2023/8/8 20:40
 */
@Data
@Schema(description = "代码生成配置信息")
public class GenConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 列映射信息列表
     */
    @Schema(description = "列映射信息列表")
    @NotEmpty(message = "列映射信息不能为空")
    private List<ColumnMappingDO> columnMappings = new ArrayList<>();

    /**
     * 生成配置信息
     */
    @Schema(description = "生成配置信息")
    @NotNull(message = "生成配置信息不能为空")
    private GenConfigDO genConfig;
}
