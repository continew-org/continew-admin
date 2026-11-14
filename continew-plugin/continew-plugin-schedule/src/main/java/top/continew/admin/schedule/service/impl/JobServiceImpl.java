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

package top.continew.admin.schedule.service.impl;

import com.aizuda.snailjob.client.job.core.openapi.SnailJobOpenApi;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.schedule.api.JobApi;
import top.continew.admin.schedule.api.JobClient;
import top.continew.admin.schedule.api.JobGroupApi;
import top.continew.admin.schedule.enums.JobStatusEnum;
import top.continew.admin.schedule.model.query.JobQuery;
import top.continew.admin.schedule.model.req.JobReq;
import top.continew.admin.schedule.model.req.JobStatusReq;
import top.continew.admin.schedule.model.req.JobTriggerReq;
import top.continew.admin.schedule.model.resp.JobResp;
import top.continew.admin.schedule.service.JobService;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;
import java.util.Set;

/**
 * 任务业务实现
 *
 * @author KAI
 * @author Charles7c
 * @since 3.2.0
 */
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobClient jobClient;
    private final JobApi jobApi;
    private final JobGroupApi jobGroupApi;

    @Override
    public PageResp<JobResp> page(JobQuery query) {
        return jobClient.requestPage(() -> jobApi.page(query));
    }

    @Override
    public boolean create(JobReq request) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobApi.create(request)));
    }

    @Override
    public boolean update(JobReq request, Long id) {
        request.setId(id);
        return Boolean.TRUE.equals(jobClient.request(() -> jobApi.update(request)));
    }

    @Override
    public boolean updateStatus(JobStatusReq request, Long id) {
        return SnailJobOpenApi.updateJobStatus(id)
            .setStatus(
                JobStatusEnum.DISABLED.equals(request.getJobStatus()) ? StatusEnum.NO
                    : StatusEnum.YES)
            .execute();
    }

    @Override
    public boolean delete(Long id) {
        return SnailJobOpenApi.deleteJob(Set.of(id)).execute();
    }

    @Override
    public boolean trigger(JobTriggerReq request) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobApi.trigger(request)));
    }

    @Override
    public List<String> listGroup() {
        return jobClient.request(jobGroupApi::listGroup);
    }
}
