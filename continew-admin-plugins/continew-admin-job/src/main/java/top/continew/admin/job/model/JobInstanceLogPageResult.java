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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务实例日志分页信息
 *
 * @author Charles7c
 * @since 2024/7/14 21:51
 */
@Data
@Schema(description = "任务实例日志分页信息")
public class JobInstanceLogPageResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 日志详情
     */
    @Schema(description = "日志详情")
    private List message;

    /**
     * 异常信息
     */
    @Schema(description = "异常信息")
    private String throwable;

    /**
     * 是否结束
     */
    @Schema(description = "是否结束", example = "true")
    private boolean isFinished;

    /**
     * 起始索引
     */
    @Schema(description = "起始索引", example = "0")
    private Integer fromIndex;

    /**
     * 下一个开始 ID
     */
    @Schema(description = "下一个开始ID", example = "9")
    private Long nextStartId;
}
