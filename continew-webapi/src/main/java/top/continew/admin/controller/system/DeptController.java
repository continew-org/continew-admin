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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.system.model.query.DeptQuery;
import top.continew.admin.system.model.req.dept.DeptImportReq;
import top.continew.admin.system.model.req.dept.DeptReq;
import top.continew.admin.system.model.resp.dept.DeptResp;
import top.continew.admin.system.model.resp.dept.DeptImportParseResp;
import top.continew.admin.system.model.resp.dept.DeptImportResp;
import top.continew.admin.system.service.DeptService;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

import java.io.IOException;

/**
 * 部门管理 API
 *
 * @author Charles7c
 * @since 2023/1/22 17:50
 */
@Tag(name = "部门管理 API")
@RestController
@CrudRequestMapping(value = "/system/dept", api = {Api.TREE, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE, Api.EXPORT})
public class DeptController extends BaseController<DeptService, DeptResp, DeptResp, DeptQuery, DeptReq> {

    @Operation(summary = "下载导入模板", description = "下载导入模板")
    @SaCheckPermission("system:dept:import")
    @GetMapping(value = "/import/template", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        baseService.downloadImportTemplate(response);
    }

    @Operation(summary = "解析导入数据", description = "解析导入数据")
    @SaCheckPermission("system:user:import")
    @PostMapping("/import/parse")
    public DeptImportParseResp parseImport(@NotNull(message = "文件不能为空") MultipartFile file) {
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        return baseService.parseImport(file);
    }

    @Operation(summary = "导入数据", description = "导入数据")
    @SaCheckPermission("system:user:import")
    @PostMapping(value = "/import")
    public DeptImportResp importDept(@Validated @RequestBody DeptImportReq req) {
        return baseService.importDept(req);
    }
}
