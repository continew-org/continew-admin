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

import java.util.List;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

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
@TableName(value = "sys_role", autoResultMap = true)
public class RoleDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    @TableId
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 数据权限（1全部数据权限 2本部门及以下数据权限 3本部门数据权限 4仅本人数据权限 5自定义数据权限）
     */
    private DataScopeEnum dataScope;

    /**
     * 数据权限范围（部门 ID 数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> dataScopeDeptIds;

    /**
     * 描述
     */
    private String description;

    /**
     * 角色排序
     */
    private Integer roleSort;

    /**
     * 状态（1启用 2禁用）
     */
    private DisEnableStatusEnum status;
}
