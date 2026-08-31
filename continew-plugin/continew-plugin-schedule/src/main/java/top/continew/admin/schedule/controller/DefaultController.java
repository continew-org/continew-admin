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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.schedule.annotation.ConditionalOnDisabledScheduleJob;
import top.continew.starter.web.model.R;

/**
 * 任务调度默认控制器
 *
 * @author Charles7c
 * @since 2025/10/25 12:28
 */
@RestController
@ConditionalOnDisabledScheduleJob
@RequestMapping({"/schedule/job", "/schedule/log"})
public class DefaultController {

    /**
     * 模块禁用时的兜底接口，显式声明接管标准 REST 方法并返回统一的"模块已禁用"提示
     */
    @RequestMapping(value = "/**",
        method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH})
    public R error() {
        return R.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR
            .value()), "任务模块已禁用，请于对应环境配置文件中配置 snail-job.enabled 为 true 进行启用");
    }
}
