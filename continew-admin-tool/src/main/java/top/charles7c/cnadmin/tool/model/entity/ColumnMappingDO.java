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

package top.charles7c.cnadmin.tool.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.dialect.PropsUtil;

import top.charles7c.cnadmin.common.enums.QueryTypeEnum;
import top.charles7c.cnadmin.tool.enums.FormTypeEnum;

/**
 * 列映射实体
 *
 * @author Charles7c
 * @since 2023/4/12 20:21
 */
@Data
@TableName("gen_column_mapping")
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "列映射信息")
public class ColumnMappingDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表名称
     */
    @Schema(description = "表名称")
    @NotBlank(message = "表名称不能为空")
    private String tableName;

    /**
     * 列名称
     */
    @Schema(description = "列名称")
    @NotBlank(message = "列名称不能为空")
    private String columnName;

    /**
     * 列类型
     */
    @Schema(description = "列类型")
    @NotBlank(message = "列类型不能为空")
    private String columnType;

    /**
     * 字段名称
     */
    @Schema(description = "字段名称")
    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    /**
     * 字段类型
     */
    @Schema(description = "字段类型")
    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    /**
     * 注释
     */
    @Schema(description = "注释")
    private String comment;

    /**
     * 是否必填
     */
    @Schema(description = "是否必填")
    private Boolean isRequired;

    /**
     * 是否在列表中显示
     */
    @Schema(description = "是否在列表中显示")
    private Boolean showInList;

    /**
     * 是否在表单中显示
     */
    @Schema(description = "是否在表单中显示")
    private Boolean showInForm;

    /**
     * 是否在查询中显示
     */
    @Schema(description = "是否在查询中显示")
    private Boolean showInQuery;

    /**
     * 表单类型
     */
    @Schema(description = "表单类型")
    private FormTypeEnum formType;

    /**
     * 查询方式
     */
    @Schema(description = "查询方式")
    private QueryTypeEnum queryType;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public ColumnMappingDO(String tableName) {
        this.tableName = tableName;
    }

    public ColumnMappingDO setColumnName(String columnName) {
        this.columnName = columnName;
        this.fieldName = StrUtil.toCamelCase(this.columnName);
        return this;
    }

    public ColumnMappingDO setColumnType(String columnType) {
        this.columnType = columnType;
        Props generatorProp = PropsUtil.get("generator");
        this.fieldType = generatorProp.getStr(columnType);
        return this;
    }
}
