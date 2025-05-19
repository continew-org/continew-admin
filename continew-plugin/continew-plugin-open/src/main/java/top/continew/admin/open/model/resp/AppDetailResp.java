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

package top.continew.admin.open.model.resp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.model.resp.BaseDetailResp;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.starter.file.excel.converter.ExcelBaseEnumConverter;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 应用详情响应参数
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "应用详情响应参数")
public class AppDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "应用1")
    @ExcelProperty(value = "名称", order = 2)
    private String name;

    /**
     * Access Key（访问密钥）
     */
    @Schema(description = "Access Key（访问密钥）", example = "YjUyMGJjYjIxNTE0NDAxMWE1NmRiY2")
    @ExcelProperty(value = "Access Key", order = 3)
    private String accessKey;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "失效时间", order = 4)
    private LocalDateTime expireTime;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 5)
    private DisEnableStatusEnum status;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "应用1描述信息")
    @ExcelProperty(value = "描述", order = 6)
    private String description;
}