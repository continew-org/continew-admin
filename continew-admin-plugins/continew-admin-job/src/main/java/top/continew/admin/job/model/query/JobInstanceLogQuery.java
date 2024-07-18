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

package top.continew.admin.job.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务实例日志查询条件
 *
 * @author KAI
 * @since 2024/6/28 16:58
 */
@Data
@Schema(description = "任务实例日志查询条件")
public class JobInstanceLogQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    @Schema(description = "任务ID", example = "1")
    private Long jobId;

    /**
     * 任务批次 ID
     */
    @Schema(description = "任务批次ID", example = "1")
    private Long taskBatchId;

    /**
     * 任务实例 ID
     */
    @Schema(description = "任务实例ID", example = "1")
    private Long taskId;

    /**
     * 开始 ID
     */
    @Schema(description = "开始ID", example = "2850")
    private Integer startId;

    /**
     * 起始索引
     */
    @Schema(description = "起始索引", example = "0")
    @Min(value = 0, message = "起始索引最小值为 {value}")
    private Integer fromIndex = 0;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "50")
    @Range(min = 1, max = 1000, message = "每页条数（取值范围 {min}-{max}）")
    private Integer size = 50;
}
