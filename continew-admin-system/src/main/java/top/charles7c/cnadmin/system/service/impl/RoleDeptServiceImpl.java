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

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;

import top.charles7c.cnadmin.system.mapper.RoleDeptMapper;
import top.charles7c.cnadmin.system.model.entity.RoleDeptDO;
import top.charles7c.cnadmin.system.service.RoleDeptService;

/**
 * 角色和部门业务实现类
 *
 * @author Charles7c
 * @since 2023/2/19 10:47
 */
@Service
@RequiredArgsConstructor
public class RoleDeptServiceImpl implements RoleDeptService {

    private final RoleDeptMapper roleDeptMapper;

    @Override
    public void save(List<Long> deptIds, Long roleId) {
        if (CollUtil.isEmpty(deptIds)) {
            return;
        }
        // 删除原有关联
        roleDeptMapper.lambdaUpdate().eq(RoleDeptDO::getRoleId, roleId).remove();
        // 保存最新关联
        List<RoleDeptDO> roleDeptList =
            deptIds.stream().map(deptId -> new RoleDeptDO(roleId, deptId)).collect(Collectors.toList());
        roleDeptMapper.insertBatch(roleDeptList);
    }

    @Override
    public List<Long> listDeptIdByRoleId(Long roleId) {
        return roleDeptMapper.selectDeptIdByRoleId(roleId);
    }
}
