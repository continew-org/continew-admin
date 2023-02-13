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

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.RoleMapper;
import top.charles7c.cnadmin.system.model.entity.RoleDO;
import top.charles7c.cnadmin.system.model.query.RoleQuery;
import top.charles7c.cnadmin.system.model.request.RoleRequest;
import top.charles7c.cnadmin.system.model.vo.RoleDetailVO;
import top.charles7c.cnadmin.system.model.vo.RoleVO;
import top.charles7c.cnadmin.system.service.RoleService;

/**
 * 角色业务实现类
 *
 * @author Charles7c
 * @since 2023/2/8 23:17
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, RoleDO, RoleVO, RoleDetailVO, RoleQuery, RoleRequest>
    implements RoleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleRequest request) {
        String roleName = request.getRoleName();
        boolean isExist = this.checkNameExist(roleName, request.getRoleId());
        CheckUtils.throwIf(() -> isExist, String.format("新增失败，'%s'已存在", roleName));

        // 保存信息
        request.setStatus(DisEnableStatusEnum.ENABLE);
        return super.create(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleRequest request) {
        String roleName = request.getRoleName();
        boolean isExist = this.checkNameExist(roleName, request.getRoleId());
        CheckUtils.throwIf(() -> isExist, String.format("修改失败，'%s'已存在", roleName));

        super.update(request);
    }

    /**
     * 检查名称是否存在
     *
     * @param roleName
     *            角色名称
     * @param roleId
     *            角色 ID
     * @return 是否存在
     */
    private boolean checkNameExist(String roleName, Long roleId) {
        return baseMapper.exists(Wrappers.<RoleDO>lambdaQuery().eq(RoleDO::getRoleName, roleName).ne(roleId != null,
            RoleDO::getRoleId, roleId));
    }
}
