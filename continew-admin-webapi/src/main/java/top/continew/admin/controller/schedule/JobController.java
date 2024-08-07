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
import top.continew.admin.job.model.query.JobQuery;
import top.continew.admin.job.model.req.JobReq;
import top.continew.admin.job.model.req.JobStatusReq;
import top.continew.admin.job.model.resp.JobResp;
import top.continew.admin.job.service.JobService;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.crud.util.ValidateGroup;
import top.continew.starter.log.core.annotation.Log;

import java.util.List;

/**
 * 任务 API
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/25 22:24
 */
@Tag(name = " 任务 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule/job")
public class JobController {

    private final JobService baseService;

    @Operation(summary = "分页查询任务列表", description = "分页查询任务列表")
    @SaCheckPermission("schedule:job:list")
    @GetMapping
    public PageResp<JobResp> page(JobQuery query) {
        return baseService.page(query);
    }

    @Operation(summary = "新增任务", description = "新增任务")
    @SaCheckPermission("schedule:job:add")
    @PostMapping
    public void add(@Validated(ValidateGroup.Crud.Add.class) @RequestBody JobReq req) {
        baseService.add(req);
    }

    @Operation(summary = "修改任务", description = "修改任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:update")
    @PutMapping("/{id}")
    public void update(@Validated(ValidateGroup.Crud.Update.class) @RequestBody JobReq req, @PathVariable Long id) {
        baseService.update(req, id);
    }

    @Operation(summary = "修改任务状态", description = "修改任务状态")
    @SaCheckPermission("schedule:job:update")
    @PatchMapping("/{id}/status")
    public void updateStatus(@Validated @RequestBody JobStatusReq req, @PathVariable Long id) {
        baseService.updateStatus(req, id);
    }

    @Operation(summary = "删除任务", description = "删除任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        baseService.delete(id);
    }

    @Operation(summary = "执行任务", description = "执行任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:trigger")
    @PostMapping("/trigger/{id}")
    public void trigger(@PathVariable Long id) {
        baseService.trigger(id);
    }

    @Log(ignore = true)
    @Operation(summary = "查询任务分组列表", description = "查询任务分组列表")
    @SaCheckPermission("schedule:job:list")
    @GetMapping("/group")
    public List<String> listGroup() {
        return baseService.listGroup();
    }
}
