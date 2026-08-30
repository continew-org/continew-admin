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

package top.continew.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.DataScopeEnum;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.system.constant.SystemConstants;
import top.continew.admin.system.mapper.RoleMapper;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.query.RoleQuery;
import top.continew.admin.system.model.req.RolePermissionUpdateReq;
import top.continew.admin.system.model.req.RoleReq;
import top.continew.admin.system.model.resp.MenuResp;
import top.continew.admin.system.model.resp.role.RoleDetailResp;
import top.continew.admin.system.model.resp.role.RoleResp;
import top.continew.admin.system.service.MenuService;
import top.continew.admin.system.service.RoleDeptService;
import top.continew.admin.system.service.RoleMenuService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色业务实现
 *
 * @author Charles7c
 * @since 2023/2/8 23:17
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl
    extends BaseServiceImpl<RoleMapper, RoleDO, RoleResp, RoleDetailResp, RoleQuery, RoleReq>
    implements RoleService {

    @Resource
    private MenuService menuService;
    @Resource
    private UserRoleService userRoleService;
    private final RoleMenuService roleMenuService;
    private final RoleDeptService roleDeptService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleReq req) {
        this.checkNameRepeat(req.getName(), null);
        String code = req.getCode();
        this.checkCodeRepeat(code, null);
        // 防止租户添加超级管理员
        CheckUtils.throwIfEqual(RoleCodeEnum.SUPER_ADMIN.getCode(), code, "编码 [{}] 禁止使用", code);
        // 新增信息
        Long roleId = super.create(req);
        // 保存角色和部门关联
        roleDeptService.add(req.getDeptIds(), roleId);
        return roleId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleReq req, Long id) {
        this.checkNameRepeat(req.getName(), id);
        RoleDO oldRole = super.getById(id);
        CheckUtils.throwIfNotEqual(req.getCode(), oldRole.getCode(), "角色编码不允许修改",
            oldRole.getName());
        DataScopeEnum oldDataScope = oldRole.getDataScope();
        if (Boolean.TRUE.equals(oldRole.getIsSystem())) {
            CheckUtils.throwIfNotEqual(req.getDataScope(), oldDataScope, "[{}] 是系统内置角色，不允许修改角色数据权限",
                oldRole.getName());
        }
        // 更新信息
        super.update(req, id);
        if (RoleCodeEnum.isSuperRoleCode(req.getCode())) {
            return;
        }
        // 保存角色和部门关联
        boolean isSaveDeptSuccess = roleDeptService.add(req.getDeptIds(), id);
        // 如果数据权限有变更，则更新在线用户权限信息
        if (isSaveDeptSuccess || ObjectUtil.notEqual(req.getDataScope(), oldDataScope)) {
            this.updateUserContext(id);
        }
    }

    @Override
    public void beforeDelete(List<Long> ids) {
        List<RoleDO> list = baseMapper.lambdaQuery()
            .select(RoleDO::getName, RoleDO::getIsSystem)
            .in(RoleDO::getId, ids)
            .list();
        Optional<RoleDO> isSystemData = list.stream().filter(RoleDO::getIsSystem).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选角色 [{}] 是系统内置角色，不允许删除",
            isSystemData.orElseGet(RoleDO::new)
                .getName());
        CheckUtils.throwIf(userRoleService.isRoleIdExists(ids), "所选角色存在用户关联，请解除关联后重试");
        // 删除角色和菜单关联
        roleMenuService.deleteByRoleIds(ids);
        // 删除角色和部门关联
        roleDeptService.deleteByRoleIds(ids);
    }

    @Override
    public void fill(Object obj) {
        super.fill(obj);
        if (obj instanceof RoleDetailResp detail) {
            Long roleId = detail.getId();
            List<MenuResp> list = menuService.listByRoleId(roleId);
            List<Long> menuIds = CollUtils.mapToList(list, MenuResp::getId);
            detail.setMenuIds(menuIds);
        }
    }

    @Override
    public List<LabelValueResp> dict(RoleQuery query, SortQuery sortQuery) {
        boolean isTenantAdmin = UserContextHolder.isTenantAdmin();
        query.setExcludeRoleCodes(isTenantAdmin
            ? List.of(RoleCodeEnum.SUPER_ADMIN.getCode())
            : RoleCodeEnum.getSuperRoleCodes());
        return super.dict(query, sortQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(key = "#id", name = CacheConstants.ROLE_MENU_KEY_PREFIX)
    public void updatePermission(Long id, RolePermissionUpdateReq req) {
        RoleDO role = super.getById(id);
        CheckUtils.throwIf(Boolean.TRUE.equals(role.getIsSystem()), "[{}] 是系统内置角色，不允许修改角色功能权限",
            role.getName());
        // 保存角色和菜单关联
        boolean isSaveMenuSuccess = roleMenuService.add(req.getMenuIds(), id);
        // 如果功能权限有变更，则更新在线用户权限信息
        if (isSaveMenuSuccess) {
            this.updateUserContext(id);
        }
        baseMapper.lambdaUpdate()
            .set(RoleDO::getMenuCheckStrictly, req.getMenuCheckStrictly())
            .eq(RoleDO::getId, id)
            .update();
    }

    @Override
    public void assignToUsers(Long id, List<Long> userIds) {
        RoleDO role = super.getById(id);
        CheckUtils.throwIf(Boolean.TRUE.equals(role.getIsSystem()), "[{}] 是系统内置角色，不允许分配角色给其他用户",
            role.getName());
        // 保存用户和角色关联
        userRoleService.assignRoleToUsers(id, userIds);
        // 更新用户上下文
        this.updateUserContext(id);
    }

    @Override
    public void updateUserContext(Long roleId) {
        List<Long> userIdList = userRoleService.listUserIdByRoleId(roleId);
        userIdList.forEach(userId -> {
            UserContext userContext = UserContextHolder.getContext(userId);
            if (userContext != null) {
                userContext.setRoles(this.listByUserId(userId));
                userContext.setPermissions(this.listPermissionByUserId(userId));
                UserContextHolder.setContext(userContext);
            }
        });
    }

    @Override
    public Set<String> listPermissionByUserId(Long userId) {
        Set<String> roleCodeSet = this.listCodeByUserId(userId);
        // 超级管理员赋予全部权限
        if (roleCodeSet.contains(RoleCodeEnum.SUPER_ADMIN.getCode())) {
            return CollUtil.newHashSet(SystemConstants.ALL_PERMISSION);
        }
        return menuService.listPermissionByUserId(userId);
    }

    @Override
    public Set<String> listCodeByUserId(Long userId) {
        List<Long> roleIdList = userRoleService.listRoleIdByUserId(userId);
        if (CollUtil.isEmpty(roleIdList)) {
            return Collections.emptySet();
        }
        List<RoleDO> roleList =
            baseMapper.lambdaQuery().select(RoleDO::getCode).in(RoleDO::getId, roleIdList).list();
        return CollUtils.mapToSet(roleList, RoleDO::getCode);
    }

    @Override
    public Set<RoleContext> listByUserId(Long userId) {
        List<Long> roleIdList = userRoleService.listRoleIdByUserId(userId);
        if (CollUtil.isEmpty(roleIdList)) {
            return Collections.emptySet();
        }
        List<RoleDO> roleList = baseMapper.lambdaQuery()
            .select(RoleDO::getId, RoleDO::getCode, RoleDO::getDataScope)
            .in(RoleDO::getId, roleIdList)
            .list();
        return CollUtils.mapToSet(roleList,
            r -> new RoleContext(r.getId(), r.getCode(), r.getDataScope()));
    }

    @Override
    public Long getIdByCode(String code) {
        return baseMapper.lambdaQuery().eq(RoleDO::getCode, code).oneOpt().map(RoleDO::getId)
            .orElse(null);
    }

    @Override
    public List<RoleDO> listByNames(List<String> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.<RoleDO>lambdaQuery().in(RoleDO::getName, list));
    }

    @Override
    public int countByNames(List<String> roleNames) {
        if (CollUtil.isEmpty(roleNames)) {
            return 0;
        }
        return (int) this.count(Wrappers.<RoleDO>lambdaQuery().in(RoleDO::getName, roleNames));
    }

    /**
     * 检查名称是否重复
     *
     * @param name 名称
     * @param id   ID
     */
    private void checkNameRepeat(String name, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(RoleDO::getName, name)
            .ne(id != null, RoleDO::getId, id)
            .exists(), "名称为 [{}] 的角色已存在", name);
    }

    /**
     * 检查编码是否重复
     *
     * @param code 编码
     * @param id   ID
     */
    private void checkCodeRepeat(String code, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(RoleDO::getCode, code)
            .ne(id != null, RoleDO::getId, id)
            .exists(), "编码为 [{}] 的角色已存在", code);
    }
}
