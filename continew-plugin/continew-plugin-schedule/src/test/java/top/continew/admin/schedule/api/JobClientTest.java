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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.aizuda.snailjob.common.core.model.Result;
import org.junit.jupiter.api.Test;
import top.continew.admin.schedule.exception.ScheduleClientException;
import top.continew.admin.schedule.model.JobPageResult;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 任务调度客户端测试。
 *
 * <p>覆盖构造参数校验、响应状态判定（失败抛客户端异常）与分页结果组装。
 * 请求供给经 Supplier 直接构造，不发起真实网络请求。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class JobClientTest {

    private static JobClient client() {
        return new JobClient("http://localhost:8800", "admin", "admin");
    }

    @Test
    void shouldThrowWhenUrlBlank() {
        assertThrows(IllegalArgumentException.class, () -> new JobClient("", "admin", "admin"));
    }

    @Test
    void shouldThrowWhenUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
            () -> new JobClient("http://localhost:8800", " ", "admin"));
    }

    @Test
    void shouldThrowWhenPasswordBlank() {
        assertThrows(IllegalArgumentException.class,
            () -> new JobClient("http://localhost:8800", "admin", null));
    }

    @Test
    void shouldReturnDataWhenStatusSuccess() {
        Result<String> result = new Result<>();
        result.setStatus(JobClient.STATUS_SUCCESS);
        result.setData("ok");

        assertEquals("ok", client().request(() -> result));
    }

    @Test
    void shouldThrowWhenStatusFailed() {
        Result<String> result = new Result<>();
        result.setStatus(0);
        result.setMessage("任务不存在");

        JobClient client = client();

        ScheduleClientException exception =
            assertThrows(ScheduleClientException.class, () -> client.request(() -> result));

        assertEquals("任务不存在", exception.getMessage());
    }

    @Test
    void shouldAssemblePageWhenStatusSuccess() {
        JobPageResult<List<String>> result = new JobPageResult<>();
        result.setStatus(JobClient.STATUS_SUCCESS);
        result.setData(List.of("job-1"));
        result.setTotal(1L);

        PageResp<String> page = client().requestPage(() -> result);

        assertEquals(1, page.getList().size());
        assertEquals("job-1", page.getList().get(0));
        assertEquals(1L, page.getTotal());
    }

    @Test
    void shouldThrowWhenPageStatusFailed() {
        JobPageResult<List<String>> result = new JobPageResult<>();
        result.setStatus(0);
        result.setMessage("查询失败");

        JobClient client = client();

        ScheduleClientException exception =
            assertThrows(ScheduleClientException.class, () -> client.requestPage(() -> result));

        assertEquals("查询失败", exception.getMessage());
    }
}
