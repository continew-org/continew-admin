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
import top.continew.admin.common.config.excel.DictExcelProperty;
import top.continew.admin.common.config.excel.ExcelDictConverter;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.resp.BaseDetailResp;
import top.continew.starter.file.excel.converter.ExcelBaseEnumConverter;
import top.continew.starter.file.excel.converter.ExcelListConverter;

import java.io.Serial;
import java.util.List;

/**
 * 客户端响应参数
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/03 16:04
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "客户端响应参数")
public class ClientResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端 ID
     */
    @Schema(description = "客户端 ID", example = "ef51c9a3e9046c4f2ea45142c8a8344a")
    @ExcelProperty(value = "客户端 ID", order = 2)
    private String clientId;

    /**
     * 客户端类型（取值于字典 client_type
     */
    @Schema(description = "客户端类型（取值于字典 client_type）", example = "PC")
    @ExcelProperty(value = "客户端类型", converter = ExcelDictConverter.class, order = 5)
    @DictExcelProperty("client_type")
    private String clientType;

    /**
     * 认证类型
     */
    @Schema(description = "认证类型", example = "ACCOUNT")
    @ExcelProperty(value = "认证类型", converter = ExcelListConverter.class, order = 4)
    private List<String> authType;

    /**
     * Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）
     */
    @Schema(description = "Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）", example = "1800")
    @ExcelProperty(value = "Token 最低活跃频率", order = 6)
    private Long activeTimeout;

    /**
     * Token 有效期（单位：秒，-1：永不过期）
     */
    @Schema(description = "Token 有效期（单位：秒，-1：永不过期）", example = "86400")
    @ExcelProperty(value = "Token 有效期", order = 7)
    private Long timeout;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 8)
    private DisEnableStatusEnum status;
}