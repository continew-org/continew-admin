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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 仪表盘-总计信息
 *
 * @author Charles7c
 * @since 2023/9/8 21:32
 */
@Data
@Schema(description = "仪表盘-总计信息")
public class DashboardTotalResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 浏览量（PV）
     */
    @Schema(description = "浏览量（PV）", example = "88888")
    private Long pvCount;

    /**
     * IP 数
     */
    @Schema(description = "IP 数", example = "66666")
    private Long ipCount;

    /**
     * 今日浏览量（PV）
     */
    @Schema(description = "今日浏览量（PV）", example = "1234")
    private Long todayPvCount;

    /**
     * 较昨日新增 PV（百分比）
     */
    @Schema(description = "较昨日新增（百分比）", example = "23.4")
    private BigDecimal newPvFromYesterday;

    /**
     * 昨日浏览量（PV）
     */
    @JsonIgnore
    private Long yesterdayPvCount;
}
