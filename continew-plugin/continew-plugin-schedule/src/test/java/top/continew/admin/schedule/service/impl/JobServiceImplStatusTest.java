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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aizuda.snailjob.client.job.core.handler.delete.DeleteJobHandler;
import com.aizuda.snailjob.client.job.core.handler.update.UpdateJobStatusHandler;
import com.aizuda.snailjob.client.job.core.openapi.SnailJobOpenApi;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import top.continew.admin.schedule.api.JobApi;
import top.continew.admin.schedule.api.JobClient;
import top.continew.admin.schedule.api.JobGroupApi;
import top.continew.admin.schedule.enums.JobStatusEnum;
import top.continew.admin.schedule.model.req.JobStatusReq;
import top.continew.admin.schedule.model.req.JobTriggerReq;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 任务状态变更、删除与执行委托测试。
 *
 * <p>覆盖状态修改的启用/禁用映射（DISABLED → StatusEnum.NO，其余 → StatusEnum.YES）、
 * 删除与执行对 SnailJobOpenApi 与调度客户端的委托传递。OpenApi 链式入口经静态模拟
 * 拦截，断言链上每个参数。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class JobServiceImplStatusTest {

    private JobClient jobClient;
    private JobApi jobApi;
    private JobGroupApi jobGroupApi;
    private JobServiceImpl jobService;
    private MockedStatic<SnailJobOpenApi> openApiMock;

    @BeforeEach
    void setUp() {
        jobClient = mock(JobClient.class);
        jobApi = mock(JobApi.class);
        jobGroupApi = mock(JobGroupApi.class);
        jobService = new JobServiceImpl(jobClient, jobApi, jobGroupApi);
        openApiMock = mockStatic(SnailJobOpenApi.class);
    }

    @AfterEach
    void tearDown() {
        openApiMock.close();
    }

    @Test
    void shouldMapDisabledToNoOnUpdateStatus() {
        stubUpdateStatusHandler(StatusEnum.NO);

        JobStatusReq req = new JobStatusReq();
        req.setJobStatus(JobStatusEnum.DISABLED);

        assertEquals(Boolean.TRUE, jobService.updateStatus(req, 1L));
    }

    @Test
    void shouldMapEnabledToYesOnUpdateStatus() {
        stubUpdateStatusHandler(StatusEnum.YES);

        JobStatusReq req = new JobStatusReq();
        req.setJobStatus(JobStatusEnum.ENABLED);

        assertEquals(Boolean.TRUE, jobService.updateStatus(req, 1L));
    }

    @Test
    void shouldPassIdAndStatusToUpdateStatusHandler() {
        UpdateJobStatusHandler handler = mock(UpdateJobStatusHandler.class);
        when(handler.setStatus(any(StatusEnum.class))).thenReturn(handler);
        when(handler.execute()).thenReturn(Boolean.TRUE);
        openApiMock.when(() -> SnailJobOpenApi.updateJobStatus(7L)).thenReturn(handler);

        JobStatusReq req = new JobStatusReq();
        req.setJobStatus(JobStatusEnum.ENABLED);

        jobService.updateStatus(req, 7L);

        verify(handler).setStatus(StatusEnum.YES);
        verify(handler).execute();
    }

    @Test
    void shouldDeleteJobByIdViaOpenApi() {
        DeleteJobHandler handler = mock(DeleteJobHandler.class);
        when(handler.execute()).thenReturn(Boolean.TRUE);
        openApiMock.when(() -> SnailJobOpenApi.deleteJob(Set.of(7L))).thenReturn(handler);

        assertEquals(Boolean.TRUE, jobService.delete(7L));

        openApiMock.verify(() -> SnailJobOpenApi.deleteJob(Set.of(7L)));
        verify(handler).execute();
    }

    @Test
    void shouldDelegateTriggerToJobApiWithRequest() {
        JobTriggerReq expected = new JobTriggerReq();
        expected.setJobId(7L);
        Result<Boolean> result = new Result<>();
        result.setStatus(JobClient.STATUS_SUCCESS);
        result.setData(Boolean.TRUE);
        when(jobClient.request(any())).thenReturn(Boolean.TRUE);
        ArgumentCaptor<Supplier<Result<Boolean>>> supplierCaptor = ArgumentCaptor
            .forClass(Supplier.class);
        when(jobApi.trigger(any(JobTriggerReq.class))).thenReturn(result);

        jobService.trigger(expected);

        verify(jobClient).request(supplierCaptor.capture());
        assertEquals(result, supplierCaptor.getValue().get());
        verify(jobApi).trigger(expected);
    }

    private void stubUpdateStatusHandler(StatusEnum expectedStatus) {
        UpdateJobStatusHandler handler = mock(UpdateJobStatusHandler.class);
        when(handler.setStatus(expectedStatus)).thenReturn(handler);
        when(handler.execute()).thenReturn(Boolean.TRUE);
        openApiMock.when(() -> SnailJobOpenApi.updateJobStatus(1L)).thenReturn(handler);
    }
}
