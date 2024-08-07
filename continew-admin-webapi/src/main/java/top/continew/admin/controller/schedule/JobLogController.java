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

package top.continew.admin.controller.schedule;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.continew.admin.job.model.JobInstanceLogPageResult;
import top.continew.admin.job.model.query.JobInstanceLogQuery;
import top.continew.admin.job.model.query.JobLogQuery;
import top.continew.admin.job.model.query.JobInstanceQuery;
import top.continew.admin.job.model.resp.JobLogResp;
import top.continew.admin.job.model.resp.JobInstanceResp;
import top.continew.admin.job.service.JobLogService;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 任务日志 API
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/27 22:24
 */
@Tag(name = " 任务日志 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule/log")
public class JobLogController {

    private final JobLogService baseService;

    @Operation(summary = "分页查询任务日志列表", description = "分页查询任务日志列表")
    @SaCheckPermission("schedule:log:list")
    @GetMapping
    public PageResp<JobLogResp> page(JobLogQuery query) {
        return baseService.page(query);
    }

    @Operation(summary = "停止任务", description = "停止任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:log:stop")
    @PostMapping("/stop/{id}")
    public void stop(@PathVariable Long id) {
        baseService.stop(id);
    }

    @Operation(summary = "重试任务", description = "重试任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:log:retry")
    @PostMapping("/retry/{id}")
    public void retry(@PathVariable Long id) {
        baseService.retry(id);
    }

    @Operation(summary = "查询任务实例列表", description = "查询任务实例列表")
    @SaCheckPermission("schedule:log:list")
    @GetMapping("/instance")
    public List<JobInstanceResp> listInstance(JobInstanceQuery query) {
        return baseService.listInstance(query);
    }

    @Operation(summary = "分页查询任务实例日志列表", description = "分页查询任务实例日志列表")
    @SaCheckPermission("schedule:log:list")
    @GetMapping("/instance/log")
    public JobInstanceLogPageResult pageInstanceLog(JobInstanceLogQuery query) {
        return baseService.pageInstanceLog(query);
    }
}
