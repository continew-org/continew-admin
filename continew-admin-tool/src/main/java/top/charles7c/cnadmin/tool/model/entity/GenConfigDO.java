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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 生成配置实体
 *
 * @author Charles7c
 * @since 2023/4/12 20:21
 */
@Data
@TableName("gen_config")
public class GenConfigDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 包名称
     */
    private String packageName;

    /**
     * 业务名称
     */
    private String businessName;

    /**
     * 作者
     */
    private String author;

    /**
     * 前端路径
     */
    private String frontendPath;

    /**
     * 表前缀
     */
    private String tablePrefix;

    /**
     * 是否覆盖
     */
    private Boolean isOverride;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
