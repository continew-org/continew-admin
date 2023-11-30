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

package top.charles7c.continew.admin.webapi.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckPermission;

import top.charles7c.continew.admin.common.constant.SysConstants;
import top.charles7c.continew.admin.system.model.query.UserQuery;
import top.charles7c.continew.admin.system.model.req.UserReq;
import top.charles7c.continew.admin.system.model.req.UserRoleUpdateReq;
import top.charles7c.continew.admin.system.model.resp.UserDetailResp;
import top.charles7c.continew.admin.system.model.resp.UserResp;
import top.charles7c.continew.admin.system.service.UserService;
import top.charles7c.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.charles7c.continew.starter.extension.crud.base.BaseController;
import top.charles7c.continew.starter.extension.crud.base.ValidateGroup;
import top.charles7c.continew.starter.extension.crud.model.resp.R;

/**
 * 用户管理 API
 *
 * @author Charles7c
 * @since 2023/2/20 21:00
 */
@Tag(name = "用户管理 API")
@Validated
@RestController
@CrudRequestMapping("/system/user")
public class UserController extends BaseController<UserService, UserResp, UserDetailResp, UserQuery, UserReq> {

    @Override
    @SaCheckPermission("system:user:add")
    public R<Long> add(@Validated(ValidateGroup.Crud.Add.class) @RequestBody UserReq req) {
        Long id = baseService.add(req);
        return R.ok(String.format("新增成功，请牢记默认密码：%s", SysConstants.DEFAULT_PASSWORD), id);
    }

    @Operation(summary = "重置密码", description = "重置用户登录密码为默认密码")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:user:password:reset")
    @PatchMapping("/{id}/password")
    public R resetPassword(@PathVariable Long id) {
        baseService.resetPassword(id);
        return R.ok(String.format("重置密码成功，请牢记默认密码：%s", SysConstants.DEFAULT_PASSWORD));
    }

    @Operation(summary = "分配角色", description = "为用户新增或移除角色")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:user:role:update")
    @PatchMapping("/{id}/role")
    public R updateRole(@Validated @RequestBody UserRoleUpdateReq updateReq, @PathVariable Long id) {
        baseService.updateRole(updateReq, id);
        return R.ok("分配成功");
    }
}
