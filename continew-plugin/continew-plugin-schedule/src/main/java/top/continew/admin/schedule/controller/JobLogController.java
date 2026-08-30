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

package top.continew.admin.schedule.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.schedule.annotation.ConditionalOnEnabledScheduleJob;
import top.continew.admin.schedule.model.query.JobLogQuery;
import top.continew.admin.schedule.model.resp.JobLogResp;
import top.continew.admin.schedule.service.JobLogService;
import top.continew.starter.extension.crud.model.resp.PageResp;

/**
 * 任务日志 API
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/27 22:24
 */
@Tag(name = " 任务日志 API")
@RestController
@RequiredArgsConstructor
@ConditionalOnEnabledScheduleJob
@RequestMapping("/schedule/log")
public class JobLogController {

    private final JobLogService baseService;

    @Operation(summary = "分页查询任务日志列表", description = "分页查询任务日志列表")
    @SaCheckPermission("schedule:log:list")
    @GetMapping
    public PageResp<JobLogResp> page(@Valid JobLogQuery query) {
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
}
