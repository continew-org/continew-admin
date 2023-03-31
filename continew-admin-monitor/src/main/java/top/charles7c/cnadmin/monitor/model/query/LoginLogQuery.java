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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

import top.charles7c.cnadmin.common.annotation.Query;
import top.charles7c.cnadmin.common.constant.StringConsts;

/**
 * 登录日志查询条件
 *
 * @author Charles7c
 * @since 2023/1/16 23:25
 */
@Data
@ParameterObject
@Schema(description = "登录日志查询条件")
public class LoginLogQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 登录状态（1：成功，2：失败）
     */
    @Schema(description = "登录状态（1：成功，2：失败）")
    @Query
    private Integer status;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间")
    @Query(type = Query.Type.BETWEEN)
    @DateTimeFormat(pattern = StringConsts.NORM_DATE_TIME_PATTERN)
    private List<Date> createTime;
}
