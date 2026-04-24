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

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.base.model.resp.BaseDetailResp;
import top.continew.admin.common.config.excel.DictExcelProperty;
import top.continew.admin.common.config.excel.ExcelDictConverter;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.enums.LogoutModeEnum;
import top.continew.admin.system.enums.ReplacedRangeEnum;
import top.continew.starter.excel.converter.ExcelBaseEnumConverter;
import top.continew.starter.excel.converter.ExcelListConverter;

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
     * 是否启用 Refresh Token
     */
    @Schema(description = "是否启用 Refresh Token", example = "false")
    @ExcelProperty(value = "是否启用 Refresh Token", order = 7)
    private Boolean isEnableRefreshToken;

    /**
     * Refresh Token 有效期（单位：秒; 小于0,则和 Token 有效期相同）
     */
    @Schema(description = "Refresh Token 有效期（单位：秒; 小于0,则和 Token 有效期相同）", example = "2592000")
    @ExcelProperty(value = "Refresh Token 有效期", order = 7)
    private Long refreshTokenTimeout;

    /**
     * 是否允许同一账号多地同时登录（true：允许；false：新登录挤掉旧登录）
     */
    @Schema(description = "是否允许同一账号多地同时登录", example = "true")
    @ExcelProperty(value = "是否允许同一账号多地同时登录", order = 8)
    private Boolean isConcurrent;

    /**
     * 顶人下线的范围
     */
    @Schema(description = "顶人下线的范围", example = "ALL_DEVICE_TYPE")
    @ExcelProperty(value = "顶人下线的范围", converter = ExcelBaseEnumConverter.class, order = 9)
    private ReplacedRangeEnum replacedRange;

    /**
     * 同一账号最大登录数量（-1：不限制，只有在 isConcurrent=true，isShare=false 时才有效）
     */
    @Schema(description = "同一账号最大登录数量", example = "-1")
    @ExcelProperty(value = "同一账号最大登录数量", order = 10)
    private Integer maxLoginCount;

    /**
     * 溢出人数的下线方式
     */
    @Schema(description = "溢出人数的下线方式", example = "KICKOUT")
    @ExcelProperty(value = "溢出人数的下线方式", converter = ExcelBaseEnumConverter.class, order = 11)
    private LogoutModeEnum overflowLogoutMode;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 12)
    private DisEnableStatusEnum status;
}