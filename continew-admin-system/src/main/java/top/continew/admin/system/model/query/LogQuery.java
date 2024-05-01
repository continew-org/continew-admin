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

package top.continew.admin.system.model.query;

import cn.hutool.core.date.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 日志查询条件
 *
 * @author Charles7c
 * @since 2023/1/15 11:43
 */
@Data
@Schema(description = "日志查询条件")
public class LogQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志描述
     */
    @Schema(description = "日志描述", example = "新增数据")
    private String description;

    /**
     * 所属模块
     */
    @Schema(description = "所属模块", example = "所属模块")
    private String module;

    /**
     * IP
     */
    @Schema(description = "IP", example = "")
    private String ip;

    /**
     * 操作人
     */
    @Schema(description = "操作人", example = "admin")
    private String createUserString;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间", example = "2023-08-08 00:00:00,2023-08-08 23:59:59")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @Size(max = 2, message = "操作时间必须是一个范围")
    private List<Date> createTime;

    /**
     * 状态
     */
    @Schema(description = "状态（1：成功；2：失败）", example = "1")
    private Integer status;
}
