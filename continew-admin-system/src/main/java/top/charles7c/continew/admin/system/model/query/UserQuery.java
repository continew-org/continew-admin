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

package top.charles7c.continew.admin.system.model.query;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.format.annotation.DateTimeFormat;

import cn.hutool.core.date.DatePattern;

import top.charles7c.continew.starter.data.mybatis.plus.annotation.Query;
import top.charles7c.continew.starter.data.mybatis.plus.enums.QueryTypeEnum;

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
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    @Query(blurry = {"username", "nickname", "email", "phone"})
    private String username;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", example = "1")
    @Query
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 00:00:00,2023-08-08 23:59:59")
    @Query(type = QueryTypeEnum.BETWEEN)
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private List<Date> createTime;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID", example = "1")
    @Query
    private Long deptId;
}
