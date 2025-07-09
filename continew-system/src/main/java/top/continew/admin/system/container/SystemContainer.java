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

package top.continew.admin.system.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.admin.common.constant.ContainerConstants;
import top.continew.admin.system.mapper.RoleMapper;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.UserRoleDO;

import java.util.Collections;
import java.util.List;

/**
 * 系统管理容器（Crane4j 数据填充）
 * <p>不建议复用，Crane4j 对列表填充聚合查询，优化性能</p>
 *
 * @author Charles7c
 * @since 2025/6/29 11:51
 */
@Component
@RequiredArgsConstructor
public class SystemContainer {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    /**
     * 根据用户 ID 列表获取角色 ID 列表
     *
     * @param userIds 用户 ID 列表
     * @return 角色 ID 列表
     */
    @ContainerMethod(namespace = ContainerConstants.USER_ROLE_ID_LIST, resultKey = "userId", resultType = UserRoleDO.class, type = MappingType.ONE_TO_MANY)
    public List<UserRoleDO> listRoleIdByUserId(List<Long> userIds) {
        return userRoleMapper.lambdaQuery()
            .select(UserRoleDO::getRoleId, UserRoleDO::getUserId)
            .in(UserRoleDO::getUserId, userIds)
            .list();
    }

    /**
     * 根据角色 ID 列表获取角色名称列表
     *
     * @param ids 角色 ID 列表
     * @return 角色名称列表
     */
    @ContainerMethod(namespace = ContainerConstants.USER_ROLE_NAME_LIST, resultType = RoleDO.class)
    public List<RoleDO> listRoleNameByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return roleMapper.lambdaQuery().select(RoleDO::getName, RoleDO::getId).in(RoleDO::getId, ids).list();
    }
}
