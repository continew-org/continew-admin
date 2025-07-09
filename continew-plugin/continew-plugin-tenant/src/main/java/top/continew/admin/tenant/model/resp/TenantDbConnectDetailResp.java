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

package top.continew.admin.tenant.model.resp;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.base.model.resp.BaseDetailResp;

import java.io.Serial;

/**
 * 租户数据连接详情信息
 *
 * @author 小熊
 * @since 2024/12/12 19:13
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "租户数据连接详情信息")
public class TenantDbConnectDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 连接名称
     */
    @Schema(description = "连接名称")
    @ExcelProperty(value = "连接名称")
    private String connectName;

    /**
     * 连接类型
     */
    @Schema(description = "连接类型")
    @ExcelProperty(value = "连接类型")
    private Integer type;

    /**
     * 连接主机地址
     */
    @Schema(description = "连接主机地址")
    @ExcelProperty(value = "连接主机地址")
    private String host;

    /**
     * 连接端口
     */
    @Schema(description = "连接端口")
    @ExcelProperty(value = "连接端口")
    private Integer port;

    /**
     * 连接用户名
     */
    @Schema(description = "连接用户名")
    @ExcelProperty(value = "连接用户名")
    private String username;

    /**
     * 连接密码
     */
    @Schema(description = "连接密码")
    @ExcelProperty(value = "连接密码")
    private String password;
}