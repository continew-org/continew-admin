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

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;

import top.charles7c.cnadmin.auth.service.OnlineUserService;
import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.constant.SysConsts;
import top.charles7c.cnadmin.common.enums.DataScopeEnum;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.model.dto.RoleDTO;
import top.charles7c.cnadmin.common.model.resp.LabelValueResp;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.RoleMapper;
import top.charles7c.cnadmin.system.model.entity.RoleDO;
import top.charles7c.cnadmin.system.model.query.RoleQuery;
import top.charles7c.cnadmin.system.model.req.RoleReq;
import top.charles7c.cnadmin.system.model.resp.MenuResp;
import top.charles7c.cnadmin.system.model.resp.RoleDetailResp;
import top.charles7c.cnadmin.system.model.resp.RoleResp;
import top.charles7c.cnadmin.system.service.*;

/**
 * 角色业务实现
 *
 * @author Charles7c
 * @since 2023/2/8 23:17
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, RoleDO, RoleResp, RoleDetailResp, RoleQuery, RoleReq>
    implements RoleService {

    private final MenuService menuService;
    private final OnlineUserService onlineUserService;
    private final RoleMenuService roleMenuService;
    private final RoleDeptService roleDeptService;
    private final UserRoleService userRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(RoleReq req) {
        String name = req.getName();
        CheckUtils.throwIf(this.isNameExists(name, null), "新增失败，[{}] 已存在", name);
        String code = req.getCode();
        CheckUtils.throwIf(this.isCodeExists(code, null), "新增失败，[{}] 已存在", code);
        // 新增信息
        req.setStatus(DisEnableStatusEnum.ENABLE);
        Long roleId = super.add(req);
        // 保存角色和菜单关联
        roleMenuService.save(req.getMenuIds(), roleId);
        // 保存角色和部门关联
        roleDeptService.save(req.getDeptIds(), roleId);
        return roleId;
    }

    @Override
    @CacheEvict(cacheNames = CacheConsts.MENU_KEY_PREFIX, key = "#req.code == 'admin' ? 'ALL' : #req.code")
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleReq req, Long id) {
        String name = req.getName();
        CheckUtils.throwIf(this.isNameExists(name, id), "修改失败，[{}] 已存在", name);
        String code = req.getCode();
        CheckUtils.throwIf(this.isCodeExists(code, id), "修改失败，[{}] 已存在", code);
        RoleDO oldRole = super.getById(id);
        DataScopeEnum oldDataScope = oldRole.getDataScope();
        String oldCode = oldRole.getCode();
        if (Boolean.TRUE.equals(oldRole.getIsSystem())) {
            CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, req.getStatus(), "[{}] 是系统内置角色，不允许禁用",
                oldRole.getName());
            CheckUtils.throwIfNotEqual(req.getCode(), oldCode, "[{}] 是系统内置角色，不允许修改角色编码", oldRole.getName());
            CheckUtils.throwIfNotEqual(req.getDataScope(), oldDataScope, "[{}] 是系统内置角色，不允许修改角色数据权限", oldRole.getName());
        }
        // 更新信息
        super.update(req, id);
        // 更新关联信息
        if (!SysConsts.ADMIN_ROLE_CODE.equals(oldRole.getCode())) {
            // 保存角色和菜单关联
            boolean isSaveMenuSuccess = roleMenuService.save(req.getMenuIds(), id);
            // 保存角色和部门关联
            boolean isSaveDeptSuccess = roleDeptService.save(req.getDeptIds(), id);
            // 如果角色编码、功能权限或数据权限有变更，则清除关联的在线用户（重新登录以获取最新角色权限）
            if (ObjectUtil.notEqual(req.getCode(), oldCode) || ObjectUtil.notEqual(req.getDataScope(), oldDataScope)
                || isSaveMenuSuccess || isSaveDeptSuccess) {
                onlineUserService.cleanByRoleId(id);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        List<RoleDO> list =
            baseMapper.lambdaQuery().select(RoleDO::getName, RoleDO::getIsSystem).in(RoleDO::getId, ids).list();
        Optional<RoleDO> isSystemData = list.stream().filter(RoleDO::getIsSystem).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选角色 [{}] 是系统内置角色，不允许删除",
            isSystemData.orElseGet(RoleDO::new).getName());
        CheckUtils.throwIf(userRoleService.countByRoleIds(ids) > 0, "所选角色存在用户关联，请解除关联后重试");
        // 删除角色和菜单关联
        roleMenuService.deleteByRoleIds(ids);
        // 删除角色和部门关联
        roleDeptService.deleteByRoleIds(ids);
        // 删除角色
        super.delete(ids);
    }

    @Override
    public void fillDetail(Object detailObj) {
        super.fillDetail(detailObj);
        if (detailObj instanceof RoleDetailResp detail) {
            Long roleId = detail.getId();
            if (SysConsts.ADMIN_ROLE_CODE.equals(detail.getCode())) {
                List<MenuResp> list = menuService.list(null, null);
                List<Long> menuIds = list.stream().map(MenuResp::getId).collect(Collectors.toList());
                detail.setMenuIds(menuIds);
            } else {
                detail.setMenuIds(roleMenuService.listMenuIdByRoleIds(CollUtil.newArrayList(roleId)));
            }
            detail.setDeptIds(roleDeptService.listDeptIdByRoleId(roleId));
        }
    }

    @Override
    public List<LabelValueResp<Long>> buildDict(List<RoleResp> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>(0);
        }
        return list.stream().map(r -> new LabelValueResp<>(r.getName(), r.getId())).toList();
    }

    @Override
    public List<String> listNameByIds(List<Long> ids) {
        List<RoleDO> roleList = baseMapper.lambdaQuery().select(RoleDO::getName).in(RoleDO::getId, ids).list();
        return roleList.stream().map(RoleDO::getName).collect(Collectors.toList());
    }

    @Override
    public Set<String> listCodeByUserId(Long userId) {
        List<Long> roleIdList = userRoleService.listRoleIdByUserId(userId);
        List<RoleDO> roleList = baseMapper.lambdaQuery().select(RoleDO::getCode).in(RoleDO::getId, roleIdList).list();
        return roleList.stream().map(RoleDO::getCode).collect(Collectors.toSet());
    }

    @Override
    public Set<RoleDTO> listByUserId(Long userId) {
        List<Long> roleIdList = userRoleService.listRoleIdByUserId(userId);
        List<RoleDO> roleList = baseMapper.lambdaQuery().in(RoleDO::getId, roleIdList).list();
        return new HashSet<>(BeanUtil.copyToList(roleList, RoleDTO.class));
    }

    @Override
    public RoleDO getByCode(String code) {
        return baseMapper.lambdaQuery().eq(RoleDO::getCode, code).one();
    }

    /**
     * 名称是否存在
     *
     * @param name
     *            名称
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean isNameExists(String name, Long id) {
        return baseMapper.lambdaQuery().eq(RoleDO::getName, name).ne(null != id, RoleDO::getId, id).exists();
    }

    /**
     * 编码是否存在
     *
     * @param code
     *            编码
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean isCodeExists(String code, Long id) {
        return baseMapper.lambdaQuery().eq(RoleDO::getCode, code).ne(null != id, RoleDO::getId, id).exists();
    }
}
