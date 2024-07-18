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

package top.continew.admin.job.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.continew.admin.job.enums.JobStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改任务状态信息
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/27 9:24
 */
@Data
@Schema(description = "修改任务状态信息")
public class JobStatusReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "1")
    @NotNull(message = "任务状态非法")
    private JobStatusEnum jobStatus;

    /**
     * ID
     */
    @Schema(hidden = true)
    private Long id;
}
