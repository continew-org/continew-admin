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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import top.continew.admin.schedule.model.req.JobReq;
import top.continew.admin.schedule.model.req.JobStatusReq;
import top.continew.admin.schedule.model.req.JobTriggerReq;
import top.continew.admin.schedule.service.JobService;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 任务 API 权限契约与写操作失败路径测试。
 *
 * <p>以注解反射锁定各端点的 @SaCheckPermission 权限码；写操作业务结果为假时
 * 校验工具抛出对应失败消息。校验工具的校验器实例取自 Spring 上下文，单测环境
 * 以恒通过的 mock 供给。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class JobControllerTest {

    private JobService jobService;
    private JobController controller;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        Validator validator = mock(Validator.class);
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class)).thenReturn(validator);
        jobService = mock(JobService.class);
        controller = new JobController(jobService);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldDeclareExpectedPermissionCodes() {
        List<String> codes = permissionCodes(JobController.class.getDeclaredMethods());

        // getDeclaredMethods 顺序不保证，两端点的同名权限码按字典序去重后断言
        assertEquals(List.of("schedule:job:create", "schedule:job:delete", "schedule:job:list",
            "schedule:job:trigger", "schedule:job:update"),
            codes.stream().distinct().sorted()
                .toList());
        assertEquals(7, codes.size());
    }

    @Test
    void shouldThrowWhenCreateFailed() {
        when(jobService.create(any(JobReq.class))).thenReturn(false);

        BusinessException exception = assertThrowsBusinessException(() -> controller
            .create(new JobReq()));

        assertEquals("任务创建失败", exception.getMessage());
        verify(jobService).create(any(JobReq.class));
    }

    @Test
    void shouldNotThrowWhenCreateSucceeded() {
        when(jobService.create(any(JobReq.class))).thenReturn(true);

        controller.create(new JobReq());

        verify(jobService).create(any(JobReq.class));
    }

    @Test
    void shouldThrowWhenUpdateFailed() {
        when(jobService.update(any(JobReq.class), eq(7L))).thenReturn(false);

        BusinessException exception = assertThrowsBusinessException(() -> controller
            .update(new JobReq(), 7L));

        assertEquals("任务修改失败", exception.getMessage());
        verify(jobService).update(any(JobReq.class), eq(7L));
    }

    @Test
    void shouldThrowWhenUpdateStatusFailed() {
        when(jobService.updateStatus(any(JobStatusReq.class), eq(7L))).thenReturn(false);

        BusinessException exception = assertThrowsBusinessException(() -> controller
            .updateStatus(new JobStatusReq(), 7L));

        assertEquals("任务状态修改失败", exception.getMessage());
        verify(jobService).updateStatus(any(JobStatusReq.class), eq(7L));
    }

    @Test
    void shouldThrowWhenDeleteFailed() {
        when(jobService.delete(7L)).thenReturn(false);

        BusinessException exception = assertThrowsBusinessException(() -> controller.delete(7L));

        assertEquals("任务删除失败", exception.getMessage());
        verify(jobService).delete(7L);
    }

    @Test
    void shouldThrowWhenTriggerFailed() {
        when(jobService.trigger(any(JobTriggerReq.class))).thenReturn(false);

        BusinessException exception = assertThrowsBusinessException(() -> controller.trigger(7L));

        assertEquals("任务执行失败", exception.getMessage());
        ArgumentCaptor<JobTriggerReq> reqCaptor = ArgumentCaptor.forClass(JobTriggerReq.class);
        verify(jobService).trigger(reqCaptor.capture());
        assertEquals(7L, reqCaptor.getValue().getJobId());
    }

    private BusinessException assertThrowsBusinessException(Runnable action) {
        try {
            action.run();
        } catch (BusinessException e) {
            return e;
        }
        throw new AssertionError("预期抛出 BusinessException，但未抛出");
    }

    private List<String> permissionCodes(Method[] methods) {
        return Arrays.stream(methods)
            .filter(method -> method.isAnnotationPresent(SaCheckPermission.class))
            .map(method -> method.getAnnotation(SaCheckPermission.class).value()[0])
            .toList();
    }
}
