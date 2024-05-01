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
 * 用户查询条件
 *
 * @author Charles7c
 * @since 2023/2/20 21:01
 */
@Data
@Schema(description = "用户查询条件")
public class UserQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "zhangsan")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 00:00:00,2023-08-08 23:59:59")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @Size(max = 2, message = "创建时间必须是一个范围")
    private List<Date> createTime;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID", example = "1")
    private Long deptId;
}
