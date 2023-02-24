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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.consts.Constants;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.model.vo.LabelValueVO;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.RoleMapper;
import top.charles7c.cnadmin.system.model.entity.RoleDO;
import top.charles7c.cnadmin.system.model.query.RoleQuery;
import top.charles7c.cnadmin.system.model.request.RoleRequest;
import top.charles7c.cnadmin.system.model.vo.MenuVO;
import top.charles7c.cnadmin.system.model.vo.RoleDetailVO;
import top.charles7c.cnadmin.system.model.vo.RoleVO;
import top.charles7c.cnadmin.system.service.*;

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

    private final RoleMenuService roleMenuService;
    private final RoleDeptService roleDeptService;
    private final MenuService menuService;
    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(RoleRequest request) {
        String roleName = request.getRoleName();
        boolean isExist = this.checkNameExists(roleName, request.getRoleId());
        CheckUtils.throwIf(() -> isExist, String.format("新增失败，'%s'已存在", roleName));

        // 新增角色
        request.setStatus(DisEnableStatusEnum.ENABLE);
        Long roleId = super.add(request);
        // 保存角色和菜单关联
        roleMenuService.save(request.getMenuIds(), roleId);
        // 保存角色和部门关联
        roleDeptService.save(request.getDeptIds(), roleId);
        return roleId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleRequest request) {
        String roleName = request.getRoleName();
        boolean isExist = this.checkNameExists(roleName, request.getRoleId());
        CheckUtils.throwIf(() -> isExist, String.format("修改失败，'%s'已存在", roleName));

        // 更新角色
        super.update(request);
        Long roleId = request.getRoleId();
        // 保存角色和菜单关联
        roleMenuService.save(request.getMenuIds(), roleId);
        // 保存角色和部门关联
        roleDeptService.save(request.getDeptIds(), roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        CheckUtils.throwIf(() -> userService.countByRoleIds(ids) > 0, "所选角色存在用户关联，请解除关联后重试");
        super.delete(ids);
    }

    /**
     * 检查名称是否存在
     *
     * @param name
     *            名称
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean checkNameExists(String name, Long id) {
        return super.lambdaQuery().eq(RoleDO::getRoleName, name).ne(id != null, RoleDO::getRoleId, id).exists();
    }

    @Override
    public void fillDetail(Object detailObj) {
        super.fillDetail(detailObj);
        if (detailObj instanceof RoleDetailVO) {
            RoleDetailVO detailVO = (RoleDetailVO)detailObj;
            Long roleId = detailVO.getRoleId();
            if (Constants.ADMIN_ROLE_CODE.equals(detailVO.getRoleCode())) {
                List<MenuVO> list = menuService.list(null, null);
                List<Long> menuIds = list.stream().map(MenuVO::getMenuId).collect(Collectors.toList());
                detailVO.setMenuIds(menuIds);
            } else {
                detailVO.setMenuIds(roleMenuService.listMenuIdByRoleId(roleId));
            }
            detailVO.setDeptIds(roleDeptService.listDeptIdByRoleId(roleId));
        }
    }

    @Override
    public List<LabelValueVO<Long>> buildDict(List<RoleVO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(r -> new LabelValueVO<>(r.getRoleName(), r.getRoleId())).collect(Collectors.toList());
    }

    @Override
    public List<String> listRoleNamesByRoleIds(List<Long> roleIds) {
        List<RoleDO> roleList = super.lambdaQuery().select(RoleDO::getRoleName).in(RoleDO::getRoleId, roleIds).list();
        if (CollUtil.isEmpty(roleList)) {
            return Collections.emptyList();
        }
        return roleList.stream().map(RoleDO::getRoleName).collect(Collectors.toList());
    }
}
