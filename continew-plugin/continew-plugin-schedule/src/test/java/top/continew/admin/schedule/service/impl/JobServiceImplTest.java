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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aizuda.snailjob.common.core.model.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import top.continew.admin.schedule.api.JobApi;
import top.continew.admin.schedule.api.JobClient;
import top.continew.admin.schedule.api.JobGroupApi;
import top.continew.admin.schedule.model.JobPageResult;
import top.continew.admin.schedule.model.query.JobQuery;
import top.continew.admin.schedule.model.req.JobReq;
import top.continew.admin.schedule.model.resp.JobResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 任务业务委托测试。
 *
 * <p>覆盖任务的分页/创建/更新/触发与分组列表对调度客户端的委托传递。
 * 调度客户端用 Mockito 模拟，不发起真实网络请求。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class JobServiceImplTest {

    private JobClient jobClient;
    private JobApi jobApi;
    private JobGroupApi jobGroupApi;
    private JobServiceImpl jobService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        jobClient = mock(JobClient.class);
        jobApi = mock(JobApi.class);
        jobGroupApi = mock(JobGroupApi.class);
        jobService = new JobServiceImpl(jobClient, jobApi, jobGroupApi);
    }

    @Test
    void shouldDelegatePageToJobApi() {
        JobPageResult<List<JobResp>> pageResult = new JobPageResult<>();
        pageResult.setStatus(JobClient.STATUS_SUCCESS);
        pageResult.setData(List.of());
        pageResult.setTotal(0L);
        when(jobClient.requestPage(any()))
            .thenReturn(top.continew.starter.extension.crud.model.resp.PageResp
                .build(1, 10, List.of()));
        when(jobApi.page(any())).thenReturn(pageResult);

        PageResp<JobResp> page = jobService.page(new JobQuery());

        assertEquals(0, page.getList().size());
    }

    @Test
    void shouldDelegateCreateAndUpdateToJobApi() {
        JobReq req = new JobReq();
        when(
            jobClient.request(ArgumentMatchers.<java.util.function.Supplier<Result<Boolean>>>any()))
            .thenReturn(Boolean.TRUE);
        when(jobApi.create(any())).thenReturn(new Result<>());
        when(jobApi.update(any())).thenReturn(new Result<>());

        assertEquals(Boolean.TRUE, jobService.create(req));
        assertEquals(Boolean.TRUE, jobService.update(req, 1L));
        assertEquals(1L, req.getId());
    }

    @Test
    void shouldDelegateListGroupToJobGroupApi() {
        when(jobClient
            .request(ArgumentMatchers.<java.util.function.Supplier<Result<List<String>>>>any()))
            .thenReturn(List.of("default"));
        when(jobGroupApi.listGroup()).thenReturn(new Result<>());

        List<String> groups = jobService.listGroup();

        assertEquals(List.of("default"), groups);
    }
}
