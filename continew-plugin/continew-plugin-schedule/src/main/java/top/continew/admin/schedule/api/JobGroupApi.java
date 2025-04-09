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
import org.springframework.web.bind.annotation.GetMapping;
import top.continew.admin.schedule.config.FeignRequestInterceptor;

import java.util.List;

/**
 * 任务组 REST API
 *
 * @author Charles7c
 * @since 2025/3/28 22:25
 */
@FeignClient(value = "job-group", url = "${snail-job.server.api.url}", path = "/group", configuration = FeignRequestInterceptor.class)
public interface JobGroupApi {

    /**
     * 查询分组列表
     *
     * @return 响应信息
     */
    @GetMapping("/all/group-name/list")
    Result<List<String>> listGroup();
}
