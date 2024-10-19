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

package top.continew.admin.system.model.resp.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘-通用总览信息
 *
 * @author Charles7c
 * @since 2024/10/19 12:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表盘-通用总览信息")
public class DashboardOverviewCommonResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总数
     */
    @Schema(description = "总数", example = "888888")
    private Long total;

    /**
     * 今日数量
     */
    @Schema(description = "今日数量", example = "888")
    private Long today;

    /**
     * 较昨日新增（百分比）
     */
    @Schema(description = "较昨日新增（百分比）", example = "23.4")
    private BigDecimal growth;

    /**
     * 图表数据
     */
    @Schema(description = "图表数据")
    private List<DashboardChartCommonResp> dataList;

    /**
     * 昨日数量
     */
    @JsonIgnore
    private Long yesterday;
}
