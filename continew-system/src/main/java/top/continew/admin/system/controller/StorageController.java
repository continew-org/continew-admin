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

package top.continew.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.common.model.req.CommonStatusUpdateReq;
import top.continew.admin.system.model.query.StorageQuery;
import top.continew.admin.system.model.req.StorageReq;
import top.continew.admin.system.model.resp.StorageResp;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

/**
 * 存储管理 API
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Tag(name = "存储管理 API")
@Validated
@RestController
@CrudRequestMapping(value = "/system/storage", api = {Api.LIST, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE})
public class StorageController extends BaseController<StorageService, StorageResp, StorageResp, StorageQuery, StorageReq> {

    @Operation(summary = "修改状态", description = "修改状态")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:storage:updateStatus")
    @PutMapping({"/{id}/status"})
    public void updateStatus(@Validated @RequestBody CommonStatusUpdateReq req, @PathVariable("id") Long id) {
        baseService.updateStatus(req, id);
    }

    @Operation(summary = "设为默认存储", description = "设为默认存储")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:storage:setDefault")
    @PutMapping({"/{id}/default"})
    public void setDefault(@PathVariable("id") Long id) {
        baseService.setDefaultStorage(id);
    }
}