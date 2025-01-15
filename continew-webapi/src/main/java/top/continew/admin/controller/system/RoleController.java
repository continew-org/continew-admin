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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.system.model.query.RoleQuery;
import top.continew.admin.system.model.req.RoleReq;
import top.continew.admin.system.model.resp.RoleDetailResp;
import top.continew.admin.system.model.resp.RoleResp;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

import java.util.List;

/**
 * 角色管理 API
 *
 * @author Charles7c
 * @since 2023/2/8 23:11
 */
@Tag(name = "角色管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/system/role", api = {Api.PAGE, Api.DETAIL, Api.ADD, Api.UPDATE, Api.DELETE})
public class RoleController extends BaseController<RoleService, RoleResp, RoleDetailResp, RoleQuery, RoleReq> {

    private final UserRoleService userRoleService;

    @Operation(summary = "分配角色给用户", description = "批量分配角色给用户")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/{id}/user")
    public void assignToUsers(@PathVariable("id") Long id,
                              @Validated @NotEmpty(message = "用户ID列表不能为空") @RequestBody List<Long> userIds) {
        baseService.assignToUsers(id, userIds);
    }

    @Operation(summary = "查询角色关联用户", description = "查询角色关联的用户ID列表")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/{id}/user")
    public List<Long> listUser(@PathVariable("id") Long id) {
        return userRoleService.listUserIdByRoleId(id);
    }
}
