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

package top.continew.admin.tenant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.common.config.properties.TenantProperties;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.system.model.entity.MenuDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.req.user.UserPasswordResetReq;
import top.continew.admin.system.service.*;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.query.TenantQuery;
import top.continew.admin.tenant.model.req.TenantLoginUserInfoReq;
import top.continew.admin.tenant.model.req.TenantReq;
import top.continew.admin.tenant.model.resp.*;
import top.continew.admin.tenant.service.TenantDbConnectService;
import top.continew.admin.tenant.service.TenantPackageService;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.crud.model.entity.BaseIdDO;
import top.continew.admin.common.base.model.resp.BaseResp;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.extension.tenant.TenantHandler;

import java.util.List;

/**
 * 租户管理 API
 *
 * @author 小熊
 * @since 2024/11/26 17:20
 */
@Tag(name = "租户管理 API")
@RestController
@AllArgsConstructor
@CrudRequestMapping(value = "/tenant/user", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE})
public class TenantController extends BaseController<TenantService, TenantResp, TenantDetailResp, TenantQuery, TenantReq> {

    private final TenantProperties tenantProperties;
    private final DeptService deptService;
    private final MenuService menuService;
    private final TenantPackageService packageService;
    private final RoleService roleService;
    private final UserService userService;
    private final TenantSysDataService tenantSysDataService;
    private final RoleMenuService roleMenuService;
    private final TenantDbConnectService dbConnectService;

    @GetMapping("/common")
    @SaIgnore
    @Operation(summary = "多租户通用信息查询", description = "多租户通用信息查询")
    public TenantCommonResp common() {
        TenantCommonResp commonResp = new TenantCommonResp();
        commonResp.setIsEnabled(tenantProperties.isEnabled());
        commonResp.setAvailableList(baseService.getAvailableList());
        return commonResp;
    }

    @Override
    @DSTransactional
    public IdResp<Long> create(TenantReq req) {
        //套餐菜单
        TenantPackageDetailResp detailResp = packageService.get(req.getPackageId());
        CheckUtils.throwIf(detailResp.getMenuIds().isEmpty(), "该套餐无可用菜单");
        List<MenuDO> menuRespList = menuService.listByIds(detailResp.getMenuIds());
        //租户添加
        IdResp<Long> baseIdResp = super.create(req);
        //在租户中执行数据插入
        SpringUtil.getBean(TenantHandler.class).execute(baseIdResp.getId(), () -> {
            //租户部门初始化
            Long deptId = deptService.initTenantDept(req.getName());
            //租户菜单初始化
            menuService.menuInit(menuRespList, 0L, 0L);
            //租户角色初始化
            Long roleId = roleService.initTenantRole();
            //角色绑定菜单
            roleMenuService.add(menuService.listAll(baseIdResp.getId()).stream().map(BaseResp::getId).toList(), roleId);
            //管理用户初始化
            Long userId = userService.initTenantUser(req.getUsername(), req.getPassword(), deptId);
            //用户绑定角色
            roleService.assignToUsers(roleId, ListUtil.of(userId));
            //租户绑定用户
            baseService.bindUser(baseIdResp.getId(), userId);
        });
        return baseIdResp;
    }

    @Override
    public void delete(Long id) {
        SpringUtil.getBean(TenantHandler.class).execute(id, () -> {
            //系统数据清除
            tenantSysDataService.clear();
        });
        super.delete(id);
    }

    @Override
    public void batchDelete(@Valid IdsReq ids) {
        for (Long id : ids.getIds()) {
            //在租户中执行数据清除
            SpringUtil.getBean(TenantHandler.class).execute(id, () -> {
                //系统数据清除
                tenantSysDataService.clear();
            });
        }
        super.batchDelete(ids);
    }

    /**
     * 获取租户管理账号用户名
     */
    @GetMapping("/loginUser/{tenantId}")
    @Operation(summary = "获取租户管理账号信息", description = "获取租户管理账号信息")
    @SaCheckPermission("tenant:user:editLoginUserInfo")
    public String loginUserInfo(@PathVariable Long tenantId) {
        TenantDO tenantDO = baseService.getTenantById(tenantId);
        CheckUtils.throwIfNull(tenantDO, "租户不存在");
        StringBuilder username = new StringBuilder();
        SpringUtil.getBean(TenantHandler.class).execute(tenantDO.getId(), () -> {
            UserDO userDO = userService.getById(tenantDO.getUserId());
            CheckUtils.throwIfNull(userDO, "租户管理用户不存在");
            username.append(userDO.getUsername());
        });
        return username.toString();
    }

    /**
     * 租户管理账号信息更新
     */
    @PutMapping("/loginUser")
    @Operation(summary = "租户管理账号信息更新", description = "租户管理账号信息更新")
    @SaCheckPermission("tenant:user:editLoginUserInfo")
    @DSTransactional
    public void editLoginUserInfo(@Validated @RequestBody TenantLoginUserInfoReq req) {
        TenantDO tenantDO = baseService.getTenantById(req.getTenantId());
        CheckUtils.throwIfNull(tenantDO, "租户不存在");
        SpringUtil.getBean(TenantHandler.class).execute(tenantDO.getId(), () -> {
            UserDO userDO = userService.getById(tenantDO.getUserId());
            CheckUtils.throwIfNull(userDO, "用户不存在");
            //修改用户名
            if (!req.getUsername().equals(userDO.getUsername())) {
                userService.update(Wrappers.lambdaUpdate(UserDO.class)
                    .set(UserDO::getUsername, req.getUsername())
                    .eq(BaseIdDO::getId, userDO.getId()));
            }
            //修改密码
            if (StrUtil.isNotEmpty(req.getPassword())) {
                String password = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(req.getPassword()));
                ValidationUtils.throwIfNull(password, "新密码解密失败");
                UserPasswordResetReq passwordResetReq = new UserPasswordResetReq();
                passwordResetReq.setNewPassword(password);
                userService.resetPassword(passwordResetReq, userDO.getId());
            }
        });
    }

    /**
     * 查询所有租户套餐
     */
    @GetMapping("/all/package")
    @Operation(summary = "查询所有租户套餐", description = "查询所有租户套餐")
    @SaCheckPermission(value = {"tenant:user:add", "tenant:user:update"}, mode = SaMode.OR)
    public List<TenantPackageResp> packageList() {
        return packageService.list(null, null);
    }

    /**
     * 查询所有数据库连接
     */
    @GetMapping("/all/dbConnect")
    @Operation(summary = "获取租户数据连接列表", description = "获取租户数据连接列表")
    @SaCheckPermission(value = {"tenant:user:add", "tenant:user:update"}, mode = SaMode.OR)
    public List<TenantDbConnectResp> dbConnectList() {
        return dbConnectService.list(null, null);
    }

}