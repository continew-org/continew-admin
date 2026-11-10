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

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.system.constant.SystemConstants;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.UserRoleDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.query.RoleUserQuery;
import top.continew.admin.system.model.resp.role.RoleUserResp;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户和角色业务实现
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleMapper baseMapper;
    @Lazy
    @Resource
    private RoleService roleService;
    @Lazy
    @Resource
    private UserService userService;

    @Override
    @AutoOperate(type = RoleUserResp.class, on = "list")
    public PageResp<RoleUserResp> pageUser(RoleUserQuery query, PageQuery pageQuery) {
        String description = query.getDescription();
        QueryWrapper<UserRoleDO> queryWrapper =
            new QueryWrapper<UserRoleDO>().eq("t1.role_id", query.getRoleId())
                .and(StrUtil.isNotBlank(description), q -> q.like("t2.username", description)
                    .or()
                    .like("t2.nickname", description)
                    .or()
                    .like("t2.description", description));
        QueryWrapperHelper.sort(queryWrapper, pageQuery.getSort());
        IPage<RoleUserResp> page =
            baseMapper.selectUserPage(new Page<>(pageQuery.getPage(), pageQuery
                .getSize()), queryWrapper);
        return PageResp.build(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(List<Long> roleIds, Long userId) {
        List<Long> oldRoleIdList = this.listRoleIdByUserId(userId);
        boolean isRoleChanged = CollUtil
            .isNotEmpty(CollUtil.disjunction(roleIds, oldRoleIdList));
        // 角色无变更的幂等重放不触发系统内置用户守卫
        this.checkSystemUserRoleChange(isRoleChanged ? Set.of(userId) : Set.of(), Set.of());
        // 超级管理员和租户管理员角色不允许分配
        CheckUtils.throwIf(roleIds.contains(SystemConstants.SUPER_ADMIN_ROLE_ID), "不允许分配超级管理员角色");
        Set<String> roleCodeSet =
            CollUtils.mapToSet(roleService.listByUserId(userId), RoleContext::getCode);
        CheckUtils.throwIf(roleCodeSet.contains(RoleCodeEnum.TENANT_ADMIN.getCode()),
            "不允许分配系统管理员角色");
        // 检查是否有变更
        if (!isRoleChanged) {
            return false;
        }
        // 删除原有关联
        baseMapper.lambdaUpdate().eq(UserRoleDO::getUserId, userId).remove();
        // 保存最新关联
        List<UserRoleDO> userRoleList =
            CollUtils.mapToList(roleIds, roleId -> new UserRoleDO(userId, roleId));
        return baseMapper.insertBatch(userRoleList);
    }

    @Override
    public boolean assignRoleToUsers(Long roleId, List<Long> userIds) {
        List<UserRoleDO> userRoleList =
            CollUtils.mapToList(userIds, userId -> new UserRoleDO(userId, roleId));
        return baseMapper.insertBatch(userRoleList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        List<UserRoleDO> userRoleList = baseMapper.selectByIds(ids);
        Set<Long> userIds = CollUtils.mapToSet(userRoleList, UserRoleDO::getUserId);
        Set<Long> roleIds = CollUtils.mapToSet(userRoleList, UserRoleDO::getRoleId);
        this.checkSystemUserRoleChange(userIds, roleIds);
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        baseMapper.lambdaUpdate().in(UserRoleDO::getUserId, userIds).remove();
    }

    @Override
    public void saveBatch(List<UserRoleDO> list) {
        baseMapper.insert(list);
    }

    @Override
    public List<Long> listRoleIdByUserId(Long userId) {
        return baseMapper.lambdaQuery()
            .select(UserRoleDO::getRoleId)
            .eq(UserRoleDO::getUserId, userId)
            .list()
            .stream()
            .map(UserRoleDO::getRoleId)
            .toList();
    }

    @Override
    public List<Long> listUserIdByRoleId(Long roleId) {
        return baseMapper.lambdaQuery()
            .select(UserRoleDO::getUserId)
            .eq(UserRoleDO::getRoleId, roleId)
            .list()
            .stream()
            .map(UserRoleDO::getUserId)
            .toList();
    }

    @Override
    public boolean isRoleIdExists(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        return baseMapper.lambdaQuery().in(UserRoleDO::getRoleId, roleIds).exists();
    }

    /**
     * 校验并拒绝系统内置数据变更。
     * <p>
     * 系统内置用户的角色关联与系统内置角色的用户关联均不允许增删，任一集合为空时跳过对应侧检查，
     * 检查失败整批拒绝。
     * </p>
     *
     * @param userIds 本次涉及的用户 ID 集合
     * @param roleIds 本次涉及的角色 ID 集合
     */
    private void checkSystemUserRoleChange(Set<Long> userIds, Set<Long> roleIds) {
        if (CollUtil.isNotEmpty(userIds)) {
            List<UserDO> userList = userService.listByIds(userIds);
            Optional<UserDO> systemUser = userList.stream().filter(UserDO::getIsSystem).findFirst();
            CheckUtils.throwIf(systemUser::isPresent, "[{}] 是系统内置用户，不允许变更角色",
                systemUser.orElseGet(UserDO::new).getNickname());
        }
        if (CollUtil.isEmpty(roleIds)) {
            return;
        }
        List<RoleDO> roleList = roleService.listByIds(roleIds);
        Optional<RoleDO> systemRole = roleList.stream().filter(RoleDO::getIsSystem).findFirst();
        CheckUtils.throwIf(systemRole::isPresent, "[{}] 是系统内置角色，不允许变更用户关联",
            systemRole.orElseGet(RoleDO::new).getName());
    }
}
