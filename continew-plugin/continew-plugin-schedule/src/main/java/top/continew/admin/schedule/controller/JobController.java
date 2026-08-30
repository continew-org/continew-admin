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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.schedule.annotation.ConditionalOnEnabledScheduleJob;
import top.continew.admin.schedule.model.query.JobQuery;
import top.continew.admin.schedule.model.req.JobReq;
import top.continew.admin.schedule.model.req.JobStatusReq;
import top.continew.admin.schedule.model.req.JobTriggerReq;
import top.continew.admin.schedule.model.resp.JobResp;
import top.continew.admin.schedule.service.JobService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.crud.validation.CrudValidationGroup;
import top.continew.starter.log.annotation.Log;

import java.util.List;

/**
 * 任务 API
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/25 22:24
 */
@Tag(name = " 任务 API")
@RestController
@RequiredArgsConstructor
@ConditionalOnEnabledScheduleJob
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
    @SaCheckPermission("schedule:job:create")
    @PostMapping
    @Validated(CrudValidationGroup.Create.class)
    public void create(@RequestBody @Valid JobReq req) {
        CheckUtils.throwIf(!baseService.create(req), "任务创建失败");
    }

    @Operation(summary = "修改任务", description = "修改任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:update")
    @PutMapping("/{id}")
    @Validated(CrudValidationGroup.Update.class)
    public void update(@RequestBody @Valid JobReq req, @PathVariable Long id) {
        CheckUtils.throwIf(!baseService.update(req, id), "任务修改失败");
    }

    @Operation(summary = "修改任务状态", description = "修改任务状态")
    @SaCheckPermission("schedule:job:update")
    @PatchMapping("/{id}/status")
    public void updateStatus(@RequestBody @Valid JobStatusReq req, @PathVariable Long id) {
        CheckUtils.throwIf(!baseService.updateStatus(req, id), "任务状态修改失败");
    }

    @Operation(summary = "删除任务", description = "删除任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        CheckUtils.throwIf(!baseService.delete(id), "任务删除失败");
    }

    /**
     * 执行任务
     *
     * @param id 任务 ID
     */
    @Operation(summary = "执行任务", description = "执行任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:trigger")
    @PostMapping("/trigger/{id}")
    public void trigger(@PathVariable Long id) {
        JobTriggerReq req = new JobTriggerReq();
        req.setJobId(id);
        CheckUtils.throwIf(!baseService.trigger(req), "任务执行失败");
    }

    @Log(ignore = true)
    @Operation(summary = "查询任务分组列表", description = "查询任务分组列表")
    @SaCheckPermission("schedule:job:list")
    @GetMapping("/group")
    public List<String> listGroup() {
        return baseService.listGroup();
    }
}
