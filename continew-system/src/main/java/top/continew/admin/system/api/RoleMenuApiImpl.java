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

package top.continew.admin.system.api;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.api.system.RoleMenuApi;
import top.continew.admin.system.model.entity.RoleMenuDO;
import top.continew.admin.system.service.RoleMenuService;
import top.continew.starter.core.util.CollUtils;

import java.util.List;
import java.util.Set;

/**
 * 角色和菜单关联业务 API 实现
 *
 * @author Charles7c
 * @since 4.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleMenuApiImpl implements RoleMenuApi {

    private final RoleMenuService baseService;

    @Override
    public Set<Long> listRoleIdByNotInMenuIds(List<Long> menuIds) {
        // MP 对空集合 NOT IN 生成非法 SQL（NOT IN ()），空列表语义为不排除任何菜单
        List<RoleMenuDO> roleMenuList;
        if (CollUtil.isEmpty(menuIds)) {
            roleMenuList = baseService.lambdaQuery().select(RoleMenuDO::getRoleId).list();
        } else {
            roleMenuList = baseService.lambdaQuery()
                .select(RoleMenuDO::getRoleId)
                .notIn(RoleMenuDO::getMenuId, menuIds)
                .list();
        }
        return CollUtils.mapToSet(roleMenuList, RoleMenuDO::getRoleId);
    }

    @Override
    public List<Long> listMenuIdByRoleIds(List<Long> roleIds) {
        return baseService.listMenuIdByRoleIds(roleIds);
    }

    @Override
    public void deleteByNotInMenuIds(List<Long> menuIds) {
        // MP 对空集合 NOT IN 生成非法 SQL（NOT IN ()），空列表语义为删除全部关联，
        // 调用方须处于租户上下文（行级隔离限定本租户，如套餐菜单同步）
        if (CollUtil.isEmpty(menuIds)) {
            baseService.lambdaUpdate().remove();
            return;
        }
        baseService.lambdaUpdate().notIn(RoleMenuDO::getMenuId, menuIds).remove();
    }

    @Override
    public boolean add(List<Long> menuIds, Long roleId) {
        return baseService.add(menuIds, roleId);
    }
}
