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

package top.continew.admin.webapi.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.system.model.query.LogQuery;
import top.continew.admin.system.model.resp.log.LogDetailResp;
import top.continew.admin.system.model.resp.log.LogResp;
import top.continew.admin.system.service.LogService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.web.model.R;

/**
 * 系统日志 API
 *
 * @author Charles7c
 * @since 2023/1/18 23:55
 */
@Tag(name = "系统日志 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/log")
public class LogController {

    private final LogService baseService;

    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("monitor:log:list")
    @GetMapping
    public R<PageResp<LogResp>> page(LogQuery query, @Validated PageQuery pageQuery) {
        return R.ok(baseService.page(query, pageQuery));
    }

    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("monitor:log:list")
    @GetMapping("/{id}")
    public R<LogDetailResp> get(@PathVariable Long id) {
        return R.ok(baseService.get(id));
    }

    @Operation(summary = "导出登录日志", description = "导出登录日志")
    @SaCheckPermission("monitor:log:export")
    @GetMapping("/export/login")
    public void exportLoginLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response) {
        baseService.exportLoginLog(query, sortQuery, response);
    }

    @Operation(summary = "导出操作日志", description = "导出操作日志")
    @SaCheckPermission("monitor:log:export")
    @GetMapping("/export/operation")
    public void exportOperationLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response) {
        baseService.exportOperationLog(query, sortQuery, response);
    }
}
