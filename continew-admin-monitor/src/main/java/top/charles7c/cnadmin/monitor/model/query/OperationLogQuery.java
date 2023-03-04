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

package top.charles7c.cnadmin.monitor.model.query;

import static top.charles7c.cnadmin.common.annotation.Query.Type;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

import top.charles7c.cnadmin.common.annotation.Query;

/**
 * 操作日志查询条件
 *
 * @author Charles7c
 * @since 2023/1/15 11:43
 */
@Data
@ParameterObject
@Schema(description = "操作日志查询条件")
public class OperationLogQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    @Query(property = "createUser")
    private Long uid;

    /**
     * 操作内容
     */
    @Schema(description = "操作内容")
    @Query(type = Type.INNER_LIKE)
    private String description;

    /**
     * 操作状态（1成功 2失败）
     */
    @Schema(description = "操作状态（1成功 2失败）")
    @Query
    private Integer status;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间")
    @Query(type = Type.BETWEEN)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<Date> createTime;
}
