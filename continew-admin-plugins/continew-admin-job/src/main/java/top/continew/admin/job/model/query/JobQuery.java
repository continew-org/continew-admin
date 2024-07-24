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
import top.continew.admin.job.enums.JobStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务查询条件
 *
 * @author KAI
 * @since 2024/6/25 16:43
 */
@Data
@Schema(description = "任务查询条件")
public class JobQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务组
     */
    @Schema(description = "任务组", example = "continew-admin")
    private String groupName;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称", example = "定时任务1")
    private String jobName;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "1")
    private JobStatusEnum jobStatus;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小值为 {value}")
    private Integer page = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "10")
    @Range(min = 1, max = 1000, message = "每页条数（取值范围 {min}-{max}）")
    private Integer size = 10;
}
