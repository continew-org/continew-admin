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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘-访客地域分布信息
 *
 * @author Charles7c
 * @since 2023/9/9 12:07
 */
@Data
@Schema(description = "仪表盘-访客地域分布信息")
public class DashboardGeoDistributionResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地点列表
     */
    @Schema(description = "地点列表", example = "[\"中国北京北京市\",\"中国广东省深圳市\"]")
    private List<String> locations;

    /**
     * 地点 IP 统计信息
     */
    @Schema(description = "地点 IP 统计信息", example = "[{\"name\":\"中国北京北京市\",\"value\":1000},{\"name\":\"中国广东省深圳市\",\"value\": 500}]")
    private List<Map<String, Object>> locationIpStatistics;
}
