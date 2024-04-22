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

package top.continew.admin.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.service.PermissionService;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.system.service.MenuService;
import top.continew.admin.system.service.RoleService;

import java.util.Set;

/**
 * 权限业务实现
 *
 * @author Charles7c
 * @since 2023/3/2 20:40
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final MenuService menuService;
    private final RoleService roleService;

    @Override
    public Set<String> listPermissionByUserId(Long userId) {
        Set<String> roleCodeSet = this.listRoleCodeByUserId(userId);
        // 超级管理员赋予全部权限
        if (roleCodeSet.contains(SysConstants.ADMIN_ROLE_CODE)) {
            return CollUtil.newHashSet(SysConstants.ALL_PERMISSION);
        }
        return menuService.listPermissionByUserId(userId);
    }

    @Override
    public Set<String> listRoleCodeByUserId(Long userId) {
        return roleService.listCodeByUserId(userId);
    }
}
