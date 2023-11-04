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

package top.charles7c.cnadmin.webapi.controller.monitor;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.resp.PageDataResp;
import top.charles7c.cnadmin.monitor.annotation.Log;
import top.charles7c.cnadmin.monitor.model.query.LoginLogQuery;
import top.charles7c.cnadmin.monitor.model.query.OperationLogQuery;
import top.charles7c.cnadmin.monitor.model.query.SystemLogQuery;
import top.charles7c.cnadmin.monitor.model.resp.LoginLogResp;
import top.charles7c.cnadmin.monitor.model.resp.OperationLogResp;
import top.charles7c.cnadmin.monitor.model.resp.SystemLogDetailResp;
import top.charles7c.cnadmin.monitor.model.resp.SystemLogResp;
import top.charles7c.cnadmin.monitor.service.LogService;

/**
 * 日志管理 API
 *
 * @author Charles7c
 * @since 2023/1/18 23:55
 */
@Tag(name = "日志管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/log")
public class LogController {

    private final LogService logService;

    @Log(module = "登录日志")
    @Operation(summary = "分页查询登录日志列表", description = "分页查询登录日志列表")
    @GetMapping("/login")
    public PageDataResp<LoginLogResp> page(LoginLogQuery query, @Validated PageQuery pageQuery) {
        return logService.page(query, pageQuery);
    }

    @Log(module = "操作日志")
    @Operation(summary = "分页查询操作日志列表", description = "分页查询操作日志列表")
    @GetMapping("/operation")
    public PageDataResp<OperationLogResp> page(OperationLogQuery query, @Validated PageQuery pageQuery) {
        return logService.page(query, pageQuery);
    }

    @Log(module = "系统日志")
    @Operation(summary = "分页查询系统日志列表", description = "分页查询系统日志列表")
    @GetMapping("/system")
    public PageDataResp<SystemLogResp> page(SystemLogQuery query, @Validated PageQuery pageQuery) {
        return logService.page(query, pageQuery);
    }

    @Log(module = "系统日志")
    @Operation(summary = "查看系统日志详情", description = "查看系统日志详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/system/{id}")
    public SystemLogDetailResp get(@PathVariable Long id) {
        return logService.get(id);
    }
}
