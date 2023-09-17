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
import top.charles7c.cnadmin.common.enums.DataScopeEnum;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 角色实体
 *
 * @author Charles7c
 * @since 2023/2/8 22:54
 */
@Data
@TableName("sys_role")
public class RoleDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 数据权限（1：全部数据权限，2：本部门及以下数据权限，3：本部门数据权限，4：仅本人数据权限，5：自定义数据权限）
     */
    private DataScopeEnum dataScope;

    /**
     * 描述
     */
    private String description;

    /**
     * 角色排序
     */
    private Integer sort;

    /**
     * 状态（1：启用，2：禁用）
     */
    private DisEnableStatusEnum status;

    /**
     * 是否为系统内置数据
     */
    private Boolean isSystem;
}
