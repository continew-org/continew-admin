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

package top.charles7c.cnadmin.system.model.entity;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableName;

import top.charles7c.cnadmin.common.base.BaseDO;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 部门实体
 *
 * @author Charles7c
 * @since 2023/1/22 13:50
 */
@Data
@TableName("sys_dept")
public class DeptDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 上级部门 ID
     */
    private Long parentId;

    /**
     * 描述
     */
    private String description;

    /**
     * 部门排序
     */
    private Integer sort;

    /**
     * 状态（1：启用，2：禁用）
     */
    private DisEnableStatusEnum status;
}
