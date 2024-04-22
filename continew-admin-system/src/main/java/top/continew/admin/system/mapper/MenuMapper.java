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

package top.continew.admin.system.mapper;

import org.apache.ibatis.annotations.Param;
import top.continew.admin.system.model.entity.MenuDO;
import top.continew.starter.data.mybatis.plus.base.BaseMapper;

import java.util.List;
import java.util.Set;

/**
 * 菜单 Mapper
 *
 * @author Charles7c
 * @since 2023/2/15 20:30
 */
public interface MenuMapper extends BaseMapper<MenuDO> {

    /**
     * 根据用户 ID 查询权限码
     *
     * @param userId 用户 ID
     * @return 权限码集合
     */
    Set<String> selectPermissionByUserId(@Param("userId") Long userId);

    /**
     * 根据角色编码查询
     *
     * @param roleCode 角色编码
     * @return 菜单列表
     */
    List<MenuDO> selectListByRoleCode(@Param("roleCode") String roleCode);
}
