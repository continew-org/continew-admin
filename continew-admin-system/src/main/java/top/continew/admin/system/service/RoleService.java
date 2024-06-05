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

import top.continew.admin.common.model.dto.RoleDTO;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.query.RoleQuery;
import top.continew.admin.system.model.req.RoleReq;
import top.continew.admin.system.model.resp.RoleDetailResp;
import top.continew.admin.system.model.resp.RoleResp;
import top.continew.starter.data.mybatis.plus.service.IService;
import top.continew.starter.extension.crud.service.BaseService;

import java.util.List;
import java.util.Set;

/**
 * 角色业务接口
 *
 * @author Charles7c
 * @since 2023/2/8 23:15
 */
public interface RoleService extends BaseService<RoleResp, RoleDetailResp, RoleQuery, RoleReq>, IService<RoleDO> {

    /**
     * 根据 ID 列表查询
     *
     * @param ids ID 列表
     * @return 名称列表
     */
    List<String> listNameByIds(List<Long> ids);

    /**
     * 根据用户 ID 查询角色编码
     *
     * @param userId 用户 ID
     * @return 角色编码集合
     */
    Set<String> listCodeByUserId(Long userId);

    /**
     * 根据用户 ID 查询角色
     *
     * @param userId 用户 ID
     * @return 角色集合
     */
    Set<RoleDTO> listByUserId(Long userId);

    /**
     * 根据角色编码查询
     *
     * @param code 角色编码
     * @return 角色信息
     */
    RoleDO getByCode(String code);
}
