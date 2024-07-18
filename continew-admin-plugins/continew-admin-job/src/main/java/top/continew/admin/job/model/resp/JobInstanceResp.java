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

package top.continew.admin.job.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务实例信息
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/28 16:58
 */
@Data
@Schema(description = "任务实例信息")
public class JobInstanceResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 任务组
     */
    @Schema(description = "任务组", example = "continew-admin")
    private String groupName;

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
     * 执行状态
     */
    @Schema(description = "执行状态", example = "1")
    private Integer taskStatus;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数", example = "1")
    private Integer retryCount;

    /**
     * 执行结果
     */
    @Schema(description = "执行结果", example = "")
    private String resultMessage;

    /**
     * 客户端信息
     */
    @Schema(description = "客户端信息", example = "1812406095098114048@192.168.138.48:1789")
    private String clientInfo;
}
