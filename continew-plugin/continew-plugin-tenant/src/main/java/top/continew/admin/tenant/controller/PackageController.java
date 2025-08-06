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
import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.api.system.MenuApi;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.tenant.model.query.PackageQuery;
import top.continew.admin.tenant.model.req.PackageReq;
import top.continew.admin.tenant.model.resp.PackageDetailResp;
import top.continew.admin.tenant.model.resp.PackageResp;
import top.continew.admin.tenant.service.PackageService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

import java.util.List;

/**
 * 套餐管理 API
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/26 11:25
 */
@Tag(name = "套餐管理 API")
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/tenant/package", api = {Api.LIST, Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE,
    Api.DICT})
public class PackageController extends BaseController<PackageService, PackageResp, PackageDetailResp, PackageQuery, PackageReq> {

    private final TenantExtensionProperties tenantExtensionProperties;
    private final MenuApi menuApi;

    @Operation(summary = "查询租户套餐菜单", description = "查询租户套餐菜单树列表")
    @SaCheckPermission("tenant:package:list")
    @GetMapping("/menu/tree")
    public List<Tree<Long>> listMenuTree(@RequestParam(required = false, defaultValue = "true") Boolean isSimple) {
        return menuApi.listTree(tenantExtensionProperties.getIgnoreMenus(), isSimple);
    }
}