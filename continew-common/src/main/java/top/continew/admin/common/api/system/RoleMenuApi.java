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

package top.continew.admin.common.api.system;

import java.util.List;
import java.util.Set;

/**
 * 角色和菜单关联业务 API
 *
 * @author Charles7c
 * @since 4.0.0
 */
public interface RoleMenuApi {

    /**
     * 根据菜单 ID 列表查询角色 ID 列表
     *
     * @param menuIds 菜单 ID 列表（Not In）
     * @return 角色 ID 列表
     */
    Set<Long> listRoleIdByNotInMenuIds(List<Long> menuIds);

    /**
     * 根据角色 ID 列表查询菜单 ID 列表
     *
     * @param roleIds 角色 ID 列表
     * @return 菜单 ID 列表
     */
    List<Long> listMenuIdByRoleIds(List<Long> roleIds);

    /**
     * 根据菜单 ID 列表删除
     *
     * @param menuIds 菜单 ID 列表（Not In）
     */
    void deleteByNotInMenuIds(List<Long> menuIds);

    /**
     * 新增
     *
     * @param menuIds 菜单 ID 列表
     * @param roleId  角色 ID
     * @return 是否新增成功（true：成功；false：无变更/失败）
     */
    boolean add(List<Long> menuIds, Long roleId);
}
