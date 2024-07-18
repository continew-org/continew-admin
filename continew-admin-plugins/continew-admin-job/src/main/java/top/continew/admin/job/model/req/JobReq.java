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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.job.enums.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建或修改任务信息
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/25 16:40
 */
@Data
@Schema(description = "创建或修改任务信息")
public class JobReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务组
     */
    @Schema(description = "任务组", example = "continew-admin")
    @NotBlank(message = "任务组不能为空")
    private String groupName;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称", example = "定时任务1")
    @NotBlank(message = "任务名称不能为空")
    @Length(max = 64, message = "任务名称不能超过 {max} 个字符")
    private String jobName;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "定时任务1的描述")
    private String description;

    /**
     * 触发类型
     */
    @Schema(description = "触发类型", example = "2")
    @NotNull(message = "触发类型非法")
    private JobTriggerTypeEnum triggerType;

    /**
     * 间隔时长
     */
    @Schema(description = "间隔时长", example = "60")
    @NotBlank(message = "间隔时长不能为空")
    private String triggerInterval;

    /**
     * 执行器类型
     */
    @Schema(description = "执行器类型", example = "1", defaultValue = "1")
    private Integer executorType = 1;

    /**
     * 任务类型
     */
    @Schema(description = "任务类型", example = "1")
    @NotNull(message = "任务类型非法")
    private JobTaskTypeEnum taskType;

    /**
     * 执行器名称
     */
    @Schema(description = "执行器名称", example = "test")
    @NotBlank(message = "执行器名称不能为空")
    private String executorInfo;

    /**
     * 任务参数
     */
    @Schema(description = "任务参数", example = "")
    private String argsStr;

    /**
     * 参数类型
     */
    @Schema(description = "参数类型", example = "1")
    private Integer argsType;

    /**
     * 路由策略
     */
    @Schema(description = "路由策略", example = "4")
    @NotNull(message = "路由策略非法")
    private JobRouteStrategyEnum routeKey;

    /**
     * 阻塞策略
     */
    @Schema(description = "阻塞策略", example = "1")
    @NotNull(message = "阻塞策略非法")
    private JobBlockStrategyEnum blockStrategy;

    /**
     * 超时时间（单位：秒）
     */
    @Schema(description = "超时时间（单位：秒）", example = "60")
    @NotNull(message = "超时时间不能为空")
    private Integer executorTimeout;

    /**
     * 最大重试次数
     */
    @Schema(description = "最大重试次数", example = "3")
    @NotNull(message = "最大重试次数不能为空")
    private Integer maxRetryTimes;

    /**
     * 重试间隔（单位：秒）
     */
    @Schema(description = "重试间隔（单位：秒）", example = "1")
    @NotNull(message = "重试间隔不能为空")
    private Integer retryInterval;

    /**
     * 并行数
     */
    @Schema(description = "并行数", example = "1")
    @NotNull(message = "并行数不能为空")
    private Integer parallelNum;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "0", defaultValue = "0")
    private JobStatusEnum jobStatus = JobStatusEnum.DISABLED;

    /**
     * ID
     */
    @Schema(hidden = true)
    private Long id;
}
