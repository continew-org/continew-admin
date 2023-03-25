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

package top.charles7c.cnadmin.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;

import top.charles7c.cnadmin.system.mapper.RoleMenuMapper;
import top.charles7c.cnadmin.system.model.entity.RoleMenuDO;
import top.charles7c.cnadmin.system.service.RoleMenuService;

/**
 * 角色和菜单业务实现类
 *
 * @author Charles7c
 * @since 2023/2/19 10:43
 */
@Service
@RequiredArgsConstructor
public class RoleMenuServiceImpl implements RoleMenuService {

    private final RoleMenuMapper roleMenuMapper;

    @Override
    public boolean save(List<Long> menuIds, Long roleId) {
        // 检查是否有变更
        List<Long> oldMenuIdList = roleMenuMapper.lambdaQuery().select(RoleMenuDO::getMenuId)
            .eq(RoleMenuDO::getRoleId, roleId).list().stream().map(RoleMenuDO::getMenuId).collect(Collectors.toList());
        if (CollUtil.isEmpty(CollUtil.disjunction(menuIds, oldMenuIdList))) {
            return false;
        }
        // 删除原有关联
        roleMenuMapper.lambdaUpdate().eq(RoleMenuDO::getRoleId, roleId).remove();
        // 保存最新关联
        List<RoleMenuDO> roleMenuList =
            menuIds.stream().map(menuId -> new RoleMenuDO(roleId, menuId)).collect(Collectors.toList());
        return roleMenuMapper.insertBatch(roleMenuList);
    }

    @Override
    public List<Long> listMenuIdByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return new ArrayList<>(0);
        }
        return roleMenuMapper.selectMenuIdByRoleIds(roleIds);
    }

    @Override
    public void deleteByRoleIds(List<Long> roleIds) {
        roleMenuMapper.lambdaUpdate().in(RoleMenuDO::getRoleId, roleIds).remove();
    }
}
