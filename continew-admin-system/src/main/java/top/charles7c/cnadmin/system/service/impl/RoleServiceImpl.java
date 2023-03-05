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
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.constant.SysConsts;
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
    private final UserRoleService userRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(RoleRequest request) {
        String name = request.getName();
        CheckUtils.throwIf(() -> this.checkNameExists(name, request.getId()), String.format("新增失败，'%s'已存在", name));
        String code = request.getCode();
        CheckUtils.throwIf(() -> this.checkCodeExists(code, request.getId()), String.format("新增失败，'%s'已存在", code));

        // 新增信息
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
        String name = request.getName();
        CheckUtils.throwIf(() -> this.checkNameExists(name, request.getId()), String.format("修改失败，'%s'已存在", name));
        String code = request.getCode();
        CheckUtils.throwIf(() -> this.checkCodeExists(code, request.getId()), String.format("修改失败，'%s'已存在", code));

        // 更新信息
        super.update(request);
        Long roleId = request.getId();
        // 保存角色和菜单关联
        roleMenuService.save(request.getMenuIds(), roleId);
        // 保存角色和部门关联
        roleDeptService.save(request.getDeptIds(), roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        CheckUtils.throwIf(() -> userRoleService.countByRoleIds(ids) > 0, "所选角色存在用户关联，请解除关联后重试");
        super.delete(ids);
    }

    @Override
    public void fillDetail(Object detailObj) {
        super.fillDetail(detailObj);
        if (detailObj instanceof RoleDetailVO) {
            RoleDetailVO detailVO = (RoleDetailVO)detailObj;
            Long roleId = detailVO.getId();
            if (SysConsts.SUPER_ADMIN.equals(detailVO.getCode())) {
                List<MenuVO> list = menuService.list(null, null);
                List<Long> menuIds = list.stream().map(MenuVO::getId).collect(Collectors.toList());
                detailVO.setMenuIds(menuIds);
            } else {
                detailVO.setMenuIds(roleMenuService.listMenuIdByRoleIds(CollUtil.newArrayList(roleId)));
            }
            detailVO.setDeptIds(roleDeptService.listDeptIdByRoleId(roleId));
        }
    }

    @Override
    public List<LabelValueVO<Long>> buildDict(List<RoleVO> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>(0);
        }
        return list.stream().map(r -> new LabelValueVO<>(r.getName(), r.getId())).collect(Collectors.toList());
    }

    @Override
    public List<String> listNameByIds(List<Long> ids) {
        List<RoleDO> roleList = baseMapper.lambdaQuery().select(RoleDO::getName).in(RoleDO::getId, ids).list();
        return roleList.stream().map(RoleDO::getName).collect(Collectors.toList());
    }

    @Override
    public Set<String> listCodeByUserId(Long userId) {
        List<Long> roleIds = userRoleService.listRoleIdByUserId(userId);
        List<RoleDO> roleList = baseMapper.lambdaQuery().select(RoleDO::getCode).in(RoleDO::getId, roleIds).list();
        return roleList.stream().map(RoleDO::getCode).collect(Collectors.toSet());
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
        return baseMapper.lambdaQuery().eq(RoleDO::getName, name).ne(id != null, RoleDO::getId, id).exists();
    }

    /**
     * 检查编码是否存在
     *
     * @param code
     *            编码
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean checkCodeExists(String code, Long id) {
        return baseMapper.lambdaQuery().eq(RoleDO::getCode, code).ne(id != null, RoleDO::getId, id).exists();
    }
}
