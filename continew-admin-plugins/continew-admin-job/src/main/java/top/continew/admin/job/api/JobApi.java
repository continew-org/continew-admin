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

package top.continew.admin.job.api;

import com.aizuda.snailjob.common.core.model.Result;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;
import top.continew.admin.job.model.JobPageResult;
import top.continew.admin.job.model.req.JobReq;
import top.continew.admin.job.model.req.JobStatusReq;
import top.continew.admin.job.model.resp.JobResp;

import java.util.List;
import java.util.Set;

/**
 * 任务 REST API
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/25 18:20
 */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface JobApi {

    /**
     * 分页查询列表
     *
     * @param groupName 任务组
     * @param jobName   任务名称
     * @param jobStatus 任务状态
     * @param page      页码
     * @param size      每页条数
     * @return 响应信息
     */
    @GetExchange("/job/page/list")
    ResponseEntity<JobPageResult<List<JobResp>>> page(@RequestParam(value = "groupName", required = false) String groupName,
                                                      @RequestParam(value = "jobName", required = false) String jobName,
                                                      @RequestParam(value = "jobStatus", required = false) Integer jobStatus,
                                                      @RequestParam("page") int page,
                                                      @RequestParam("size") int size);

    /**
     * 新增
     *
     * @param req 新增信息
     * @return 响应信息
     */
    @PostExchange("/job")
    ResponseEntity<Result<Boolean>> add(@RequestBody JobReq req);

    /**
     * 修改
     *
     * @param req 修改信息
     * @return 响应信息
     */
    @PutExchange("/job")
    ResponseEntity<Result<Boolean>> update(@RequestBody JobReq req);

    /**
     * 修改状态
     *
     * @param req 修改信息
     * @return 响应信息
     */
    @PutExchange("/job/status")
    ResponseEntity<Result<Boolean>> updateStatus(@RequestBody JobStatusReq req);

    /**
     * 删除
     *
     * @param ids ID 列表
     * @return 响应信息
     */
    @DeleteExchange("/job/ids")
    ResponseEntity<Result<Boolean>> delete(@RequestBody Set<Long> ids);

    /**
     * 执行
     *
     * @param id ID
     * @return 响应信息
     */
    @PostExchange("/job/trigger/{id}")
    ResponseEntity<Result<Boolean>> trigger(@PathVariable("id") Long id);

    /**
     * 查询分组列表
     *
     * @return 响应信息
     */
    @GetExchange("/group/all/group-name/list")
    ResponseEntity<Result<List<String>>> listGroup();
}
