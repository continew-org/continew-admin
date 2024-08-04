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

package top.continew.admin.job.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.job.api.JobBatchApi;
import top.continew.admin.job.api.JobClient;
import top.continew.admin.job.model.JobInstanceLogPageResult;
import top.continew.admin.job.model.query.JobInstanceLogQuery;
import top.continew.admin.job.model.query.JobInstanceQuery;
import top.continew.admin.job.model.query.JobLogQuery;
import top.continew.admin.job.model.resp.JobInstanceResp;
import top.continew.admin.job.model.resp.JobLogResp;
import top.continew.admin.job.service.JobLogService;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 任务日志业务实现
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/27 22:54
 */
@Service
@RequiredArgsConstructor
public class JobLogServiceImpl implements JobLogService {

    private final JobClient jobClient;
    private final JobBatchApi jobBatchApi;

    @Override
    public PageResp<JobLogResp> page(JobLogQuery query) {
        LocalDateTime[] datetimeRange = query.getDatetimeRange();
        return jobClient.requestPage(() -> jobBatchApi.page(query.getJobId(), query.getJobName(), query
            .getGroupName(), query.getTaskBatchStatus() != null
                ? query.getTaskBatchStatus().getValue()
                : null, new String[] {DateUtil.format(datetimeRange[0], DatePattern.UTC_SIMPLE_PATTERN), DateUtil
                    .format(datetimeRange[1], DatePattern.UTC_SIMPLE_PATTERN)}, query.getPage(), query.getSize()));
    }

    @Override
    public boolean stop(Long id) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobBatchApi.stop(id)));
    }

    @Override
    public boolean retry(Long id) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobBatchApi.retry(id)));
    }

    @Override
    public List<JobInstanceResp> listInstance(JobInstanceQuery query) {
        return jobClient.requestPage(() -> jobBatchApi.pageTask(query.getJobId(), query.getTaskBatchId(), 1, 100))
            .getList();
    }

    @Override
    public JobInstanceLogPageResult pageInstanceLog(JobInstanceLogQuery query) {
        return Objects.requireNonNull(jobBatchApi.pageLog(query.getJobId(), query.getTaskBatchId(), query
            .getTaskId(), query.getStartId(), query.getFromIndex(), query.getSize()).getBody()).getData();
    }
}
