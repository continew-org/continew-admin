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

package top.continew.admin.system.service;

import top.continew.admin.system.model.entity.MenuDO;
import top.continew.admin.system.model.query.MenuQuery;
import top.continew.admin.system.model.req.MenuReq;
import top.continew.admin.system.model.resp.MenuResp;
import top.continew.starter.extension.crud.service.BaseService;
import top.continew.starter.data.mybatis.plus.service.IService;

import java.util.List;
import java.util.Set;

/**
 * 菜单业务接口
 *
 * @author Charles7c
 * @since 2023/2/15 20:30
 */
public interface MenuService extends BaseService<MenuResp, MenuResp, MenuQuery, MenuReq>, IService<MenuDO> {

    /**
     * 查询全部菜单
     *
     * @return 菜单列表
     */
    List<MenuResp> listAll();

    /**
     * 根据用户 ID 查询
     *
     * @param userId 用户 ID
     * @return 权限码集合
     */
    Set<String> listPermissionByUserId(Long userId);

    /**
     * 根据角色编码查询
     *
     * @param roleCode 角色编码
     * @return 菜单列表
     */
    List<MenuResp> listByRoleCode(String roleCode);
}
