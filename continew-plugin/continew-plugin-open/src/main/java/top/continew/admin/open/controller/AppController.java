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

package top.continew.admin.open.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.open.model.query.AppQuery;
import top.continew.admin.open.model.req.AppReq;
import top.continew.admin.open.model.resp.AppDetailResp;
import top.continew.admin.open.model.resp.AppResp;
import top.continew.admin.open.model.resp.AppSecretResp;
import top.continew.admin.open.service.AppService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

/**
 * 应用管理 API
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
@Tag(name = "应用管理 API")
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/open/app", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE, Api.EXPORT})
public class AppController extends BaseController<AppService, AppResp, AppDetailResp, AppQuery, AppReq> {

    @Operation(summary = "获取密钥", description = "获取应用密钥")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("open:app:secret")
    @GetMapping("/{id}/secret")
    public AppSecretResp getSecret(@PathVariable Long id) {
        return baseService.getSecret(id);
    }

    @Operation(summary = "重置密钥", description = "重置应用密钥")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("open:app:resetSecret")
    @PatchMapping("/{id}/secret")
    public void resetSecret(@PathVariable Long id) {
        baseService.resetSecret(id);
    }
}