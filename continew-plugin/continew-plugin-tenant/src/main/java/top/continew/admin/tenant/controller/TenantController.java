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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.api.system.UserApi;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.query.TenantQuery;
import top.continew.admin.tenant.model.req.TenantAdminUserPwdUpdateReq;
import top.continew.admin.tenant.model.req.TenantReq;
import top.continew.admin.tenant.model.resp.TenantDetailResp;
import top.continew.admin.tenant.model.resp.TenantResp;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.tenant.util.TenantUtils;

/**
 * 租户管理 API
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/26 17:20
 */
@Tag(name = "租户管理 API")
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/tenant/management",
    api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE})
public class TenantController
    extends BaseController<TenantService, TenantResp, TenantDetailResp, TenantQuery, TenantReq> {

    private final UserApi userApi;

    /**
     * 修改租户管理员密码
     *
     * @param req 新密码
     * @param id 租户 ID
     */
    @Operation(summary = "修改租户管理员密码", description = "修改租户管理员密码")
    @SaCheckPermission("tenant:management:updateAdminUserPwd")
    @PutMapping("/{id}/admin/pwd")
    public void updateAdminUserPwd(@RequestBody @Valid TenantAdminUserPwdUpdateReq req,
        @PathVariable Long id) {
        TenantDO tenant = baseService.getById(id);
        TenantUtils.execute(id, () -> {
            String password =
                SecureUtils.decryptPasswordByRsaPrivateKey(req.getPassword(), "新密码解密失败");
            userApi.resetPassword(password, tenant.getAdminUser());
        });
    }
}
