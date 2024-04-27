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

package top.continew.admin.system.model.resp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.starter.extension.crud.model.resp.BaseDetailResp;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 公告详情信息
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "公告详情信息")
public class NoticeDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "这是公告标题")
    @ExcelProperty(value = "标题")
    private String title;

    /**
     * 内容
     */
    @Schema(description = "内容", example = "这是公告内容")
    @ExcelProperty(value = "内容")
    private String content;

    /**
     * 类型（取值于字典 notice_type）
     */
    @Schema(description = "类型（取值于字典 notice_type）", example = "1")
    @ExcelProperty(value = "类型")
    private String type;

    /**
     * 生效时间
     */
    @Schema(description = "生效时间", example = "2023-08-08 00:00:00", type = "string")
    @ExcelProperty(value = "生效时间")
    private LocalDateTime effectiveTime;

    /**
     * 终止时间
     */
    @Schema(description = "终止时间", example = "2023-08-08 23:59:59", type = "string")
    @ExcelProperty(value = "终止时间")
    private LocalDateTime terminateTime;
}