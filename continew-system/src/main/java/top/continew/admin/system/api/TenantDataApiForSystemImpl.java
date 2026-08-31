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

package top.continew.admin.system.api;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.api.tenant.PackageMenuApi;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.common.api.tenant.TenantDataApi;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.common.enums.DataScopeEnum;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.GenderEnum;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.common.model.dto.TenantDTO;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.system.mapper.DeptMapper;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.mapper.MessageMapper;
import top.continew.admin.system.mapper.NoticeMapper;
import top.continew.admin.system.mapper.RoleDeptMapper;
import top.continew.admin.system.mapper.RoleMapper;
import top.continew.admin.system.mapper.RoleMenuMapper;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.mapper.user.UserPasswordHistoryMapper;
import top.continew.admin.system.mapper.user.UserSocialMapper;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.RoleMenuService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户数据 API 实现
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/12/2 20:12
 */
@Service
@RequiredArgsConstructor
public class TenantDataApiForSystemImpl implements TenantDataApi {

    private final PackageMenuApi packageMenuApi;
    private final TenantApi tenantApi;
    private final UserRoleService userRoleService;
    private final FileService fileService;
    private final RoleMenuService roleMenuService;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final LogMapper logMapper;
    private final MessageMapper messageMapper;
    private final MessageMapper messageUserMapper;
    private final NoticeMapper noticeMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final UserMapper userMapper;
    private final UserPasswordHistoryMapper userPasswordHistoryMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserSocialMapper userSocialMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void init(TenantDTO tenant) {
        Long tenantId = tenant.getId();
        TenantUtils.execute(tenantId, () -> {
            // 初始化部门
            Long deptId = this.initDeptData(tenant);
            // 初始化角色
            Long roleId = this.initRoleData();
            // 角色绑定菜单
            List<Long> menuIds = packageMenuApi.listMenuIdsByPackageId(tenant.getPackageId());
            roleMenuService.add(menuIds, roleId);
            // 初始化管理用户
            Long userId = this.initUserData(tenant, deptId);
            // 用户绑定角色
            userRoleService.assignRoleToUsers(roleId, ListUtil.of(userId));
            // 租户绑定用户
            tenantApi.bindAdminUser(tenantId, userId);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        // 退出所有用户
        List<UserDO> userList = userMapper.selectList(null);
        for (UserDO user : userList) {
            StpUtil.logout(user.getId());
        }
        Wrapper queryWrapper = Wrappers.query().eq("1", 1);
        // 部门清除
        deptMapper.delete(queryWrapper);
        // 文件清除
        List<Long> fileIds = CollUtils.mapToList(fileService.list(), FileDO::getId);
        if (!fileIds.isEmpty()) {
            fileService.delete(fileIds);
        }
        // 日志清除
        logMapper.delete(queryWrapper);
        // 消息清除
        messageMapper.delete(queryWrapper);
        messageUserMapper.delete(queryWrapper);
        // 通知清除
        noticeMapper.delete(queryWrapper);
        // 角色相关数据清除
        roleMapper.delete(queryWrapper);
        roleDeptMapper.delete(queryWrapper);
        roleMenuMapper.delete(queryWrapper);
        // 用户数据清除
        userMapper.delete(queryWrapper);
        userPasswordHistoryMapper.delete(queryWrapper);
        userRoleMapper.delete(queryWrapper);
        userSocialMapper.delete(queryWrapper);
    }

    /**
     * 初始化部门数据
     *
     * @param tenant 租户信息
     * @return 部门 ID
     */
    private Long initDeptData(TenantDTO tenant) {
        DeptDO dept = new DeptDO();
        dept.setName(tenant.getName());
        dept.setParentId(GlobalConstants.ROOT_PARENT_ID);
        dept.setAncestors(GlobalConstants.ROOT_PARENT_ID.toString());
        dept.setDescription("系统初始部门");
        dept.setSort(1);
        dept.setStatus(DisEnableStatusEnum.ENABLE);
        dept.setIsSystem(true);
        deptMapper.insert(dept);
        return dept.getId();
    }

    /**
     * 初始化角色数据
     *
     * @return 角色 ID
     */
    private Long initRoleData() {
        RoleDO role = new RoleDO();
        RoleCodeEnum tenantAdmin = RoleCodeEnum.TENANT_ADMIN;
        role.setName(tenantAdmin.getDescription());
        role.setCode(tenantAdmin.getCode());
        role.setDataScope(DataScopeEnum.ALL);
        role.setDescription("系统初始角色");
        role.setSort(1);
        role.setIsSystem(true);
        role.setMenuCheckStrictly(true);
        role.setDeptCheckStrictly(true);
        roleMapper.insert(role);
        return role.getId();
    }

    /**
     * 初始化用户数据
     *
     * @param tenant 租户信息
     * @param deptId 部门 ID
     * @return 用户 ID
     */
    private Long initUserData(TenantDTO tenant, Long deptId) {
        // 解密密码
        String password =
            SecureUtils.decryptPasswordByRsaPrivateKey(tenant.getAdminPassword(), "密码解密失败", true);
        // 初始化用户
        UserDO user = new UserDO();
        user.setUsername(tenant.getAdminUsername());
        user.setNickname(RoleCodeEnum.TENANT_ADMIN.getDescription());
        user.setPassword(password);
        user.setGender(GenderEnum.UNKNOWN);
        user.setDescription("系统初始用户");
        user.setStatus(DisEnableStatusEnum.ENABLE);
        user.setIsSystem(true);
        user.setPwdResetTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
        user.setDeptId(deptId);
        userMapper.insert(user);
        return user.getId();
    }
}
