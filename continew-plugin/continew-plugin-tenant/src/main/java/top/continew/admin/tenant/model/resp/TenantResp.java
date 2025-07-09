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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.base.model.resp.BaseResp;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 租户信息
 *
 * @author 小熊
 * @since 2024/11/26 17:20
 */
@Data
@Schema(description = "租户信息")
public class TenantResp extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户名称
     */
    @Schema(description = "租户名称")
    private String name;

    /**
     * 绑定的域名
     */
    @Schema(description = "绑定的域名")
    private String domain;

    /**
     * 租户套餐编号
     */
    @Schema(description = "租户套餐编号")
    private Long packageId;

    /**
     * 状态（1：启用；2：禁用）
     */
    @Schema(description = "状态（1：启用；2：禁用）")
    private Integer status;

    /**
     * 租户过期时间
     */
    @Schema(description = "租户过期时间")
    private LocalDateTime expireTime;

    /**
     * 绑定的套餐名称
     */
    @Schema(description = "绑定的套餐名称")
    private String packageName;

    /**
     * 租户编号
     */
    private String tenantSn;

    /**
     * 隔离级别
     */
    @Schema(description = "隔离级别")
    private Integer isolationLevel;

    /**
     * 数据连接ID
     */
    @Schema(description = "数据连接ID")
    private Long dbConnectId;
}