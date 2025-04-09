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

package top.continew.admin.schedule.api;

import com.aizuda.snailjob.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import top.continew.admin.schedule.config.FeignRequestInterceptor;
import top.continew.admin.schedule.model.JobInstanceLogPageResult;
import top.continew.admin.schedule.model.JobPageResult;
import top.continew.admin.schedule.model.query.JobInstanceLogQuery;
import top.continew.admin.schedule.model.query.JobInstanceQuery;
import top.continew.admin.schedule.model.query.JobLogQuery;
import top.continew.admin.schedule.model.resp.JobInstanceResp;
import top.continew.admin.schedule.model.resp.JobLogResp;

import java.util.List;

/**
 * 任务批次 REST API
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/27 23:03
 */
@FeignClient(value = "job-batch", url = "${snail-job.server.api.url}", path = "/job", configuration = FeignRequestInterceptor.class)
public interface JobBatchApi {

    /**
     * 分页查询列表
     *
     * @param query 查询条件
     * @return 响应信息
     */
    @GetMapping("/batch/list")
    JobPageResult<List<JobLogResp>> page(@SpringQueryMap JobLogQuery query);

    /**
     * 停止
     *
     * @param id ID
     * @return 响应信息
     */
    @PostMapping("/batch/stop/{id}")
    Result<Boolean> stop(@PathVariable("id") Long id);

    /**
     * 重试
     *
     * @param id ID
     * @return 响应信息
     */
    @PostMapping("/batch/retry/{id}")
    Result<Boolean> retry(@PathVariable("id") Long id);

    /**
     * 分页查询任务实例列表
     *
     * @param query 查询条件
     * @return 响应信息
     */
    @GetMapping("/task/list")
    JobPageResult<List<JobInstanceResp>> pageTask(@SpringQueryMap JobInstanceQuery query);

    /**
     * 分页查询任务实例日志列表
     *
     * @param query 查询条件
     * @return 响应信息
     */
    @GetMapping("/log/list")
    Result<JobInstanceLogPageResult> pageLog(@SpringQueryMap JobInstanceLogQuery query);
}
