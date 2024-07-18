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

package top.continew.admin.job.model;

import com.aizuda.snailjob.common.core.model.Result;
import lombok.Data;

/**
 * 任务调度服务端分页返回对象
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/26 22:27
 */
@Data
public class JobPageResult<T> extends Result<T> {

    /**
     * 页码
     */
    private long page;

    /**
     * 每页条数
     */
    private long size;

    /**
     * 总条数
     */
    private long total;
}
