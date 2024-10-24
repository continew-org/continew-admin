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

package top.continew.admin.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import top.continew.admin.system.model.query.DeptQuery;
import top.continew.admin.system.model.query.RoleQuery;
import top.continew.admin.system.model.req.RoleReq;
import top.continew.admin.system.model.resp.RoleDetailResp;
import top.continew.admin.system.model.resp.RoleResp;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.controller.BaseController;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.crud.model.query.SortQuery;

import java.util.List;

/**
 * 角色管理 API
 *
 * @author Charles7c
 * @since 2023/2/8 23:11
 */
@Tag(name = "角色管理 API")
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/system/role", api = {Api.PAGE, Api.GET, Api.ADD, Api.UPDATE, Api.DELETE})
public class RoleController extends BaseController<RoleService, RoleResp, RoleDetailResp, RoleQuery, RoleReq> {

    private final UserRoleService userRoleService;

    @Operation(summary = "查询角色关联用户", description = "查询角色组绑定的关联用户")
    @GetMapping("/listRoleUsers/{id}")
    public List<Long> listUsers(@PathVariable("id") Long roleId) {
        return userRoleService.listUserIdByRoleId(roleId);
    }

    @Operation(summary = "关联用户", description = "批量关联用户")
    @SaCheckPermission("system:role:bindUsers")
    @PostMapping("/bindUsers/{id}")
    public void bindUsers(@PathVariable("id") Long roleId,@RequestBody List<Long> userIds) {
        userRoleService.bindUserIds(roleId, userIds);
    }
}
