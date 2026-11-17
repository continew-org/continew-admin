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

import org.junit.jupiter.api.Test;
import top.continew.starter.web.model.R;

/**
 * 任务模块禁用兜底接口测试。
 *
 * <p>{@code snail-job.enabled: false} 时兜底控制器接管全部 REST 方法，
 * 返回统一的「模块已禁用」提示。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class DefaultControllerTest {

    private final DefaultController controller = new DefaultController();

    @Test
    void shouldFailWithDisabledMessage() {
        R result = controller.error();

        assertEquals("500", result.getCode());
        assertEquals("任务模块已禁用，请于对应环境配置文件中配置 snail-job.enabled 为 true 进行启用", result
            .getMsg());
    }
}
