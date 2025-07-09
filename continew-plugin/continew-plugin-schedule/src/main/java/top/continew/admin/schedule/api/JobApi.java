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
import org.springframework.web.bind.annotation.*;
import top.continew.admin.schedule.config.FeignRequestInterceptor;
import top.continew.admin.schedule.model.JobPageResult;
import top.continew.admin.schedule.model.query.JobQuery;
import top.continew.admin.schedule.model.req.JobReq;
import top.continew.admin.schedule.model.req.JobTriggerReq;
import top.continew.admin.schedule.model.resp.JobResp;

import java.util.List;
import java.util.Set;

/**
 * 任务 REST API
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/25 18:20
 */
@FeignClient(value = "job", url = "${snail-job.server.api.url}", path = "/job", configuration = FeignRequestInterceptor.class)
public interface JobApi {

    /**
     * 分页查询列表
     *
     * @param query 查询条件
     * @return 响应信息
     */
    @GetMapping("/page/list")
    JobPageResult<List<JobResp>> page(@SpringQueryMap JobQuery query);

    /**
     * 新增
     *
     * @param req 请求参数
     * @return 响应信息
     */
    @PostMapping
    Result<Boolean> create(@RequestBody JobReq req);

    /**
     * 修改
     *
     * @param req 请求参数
     * @return 响应信息
     */
    @PutMapping
    Result<Boolean> update(@RequestBody JobReq req);

    /**
     * 删除
     *
     * @param ids ID 列表
     * @return 响应信息
     */
    @DeleteMapping("/ids")
    Result<Boolean> delete(@RequestBody Set<Long> ids);

    /**
     * 执行
     *
     * @param req 请求参数
     * @return 响应信息
     */
    @PostMapping("/trigger")
    Result<Boolean> trigger(@RequestBody JobTriggerReq req);
}
