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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.continew.admin.schedule.service.JobLogService;
import top.continew.starter.core.exception.BusinessException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 任务日志 API 权限契约与写操作路径测试。
 *
 * <p>以注解反射锁定各端点的 @SaCheckPermission 权限码；停止与重试直接委托
 * 日志服务，业务失败由服务层抛出异常，控制器原样传播不额外包装。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class JobLogControllerTest {

    private JobLogService jobLogService;
    private JobLogController controller;

    @BeforeEach
    void setUp() {
        jobLogService = mock(JobLogService.class);
        controller = new JobLogController(jobLogService);
    }

    @Test
    void shouldDeclareExpectedPermissionCodes() {
        List<String> codes = permissionCodes(JobLogController.class.getDeclaredMethods());

        // getDeclaredMethods 顺序不保证，排序后断言
        assertEquals(List.of("schedule:log:list", "schedule:log:retry", "schedule:log:stop"), codes
            .stream()
            .sorted()
            .toList());
    }

    @Test
    void shouldThrowWhenStopFailed() {
        when(jobLogService.stop(7L)).thenThrow(new BusinessException("任务停止失败"));

        BusinessException exception = assertThrowsBusinessException(() -> controller.stop(7L));

        assertEquals("任务停止失败", exception.getMessage());
        verify(jobLogService).stop(7L);
    }

    @Test
    void shouldThrowWhenRetryFailed() {
        when(jobLogService.retry(7L)).thenThrow(new BusinessException("任务重试失败"));

        BusinessException exception = assertThrowsBusinessException(() -> controller.retry(7L));

        assertEquals("任务重试失败", exception.getMessage());
        verify(jobLogService).retry(7L);
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
