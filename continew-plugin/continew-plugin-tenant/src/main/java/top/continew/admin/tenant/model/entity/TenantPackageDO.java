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

package top.continew.admin.tenant.model.entity;

import java.io.Serial;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableName;
import top.continew.admin.common.base.model.entity.BaseDO;

/**
 * 租户套餐实体
 *
 * @author 小熊
 * @since 2024/11/26 11:25
 */
@Data
@TableName("sys_tenant_package")
public class TenantPackageDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 关联的菜单ids
     */
    private String menuIds;

    /**
     * 菜单选择是否父子节点关联
     */
    private Boolean menuCheckStrictly;

    /**
     * 状态（1：启用；2：禁用）
     */
    private Integer status;
}