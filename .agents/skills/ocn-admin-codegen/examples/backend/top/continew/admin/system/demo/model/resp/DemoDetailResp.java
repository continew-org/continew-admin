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
import top.continew.admin.common.base.model.resp.BaseDetailResp;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 示例详情响应参数
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@Schema(description = "示例详情响应参数")
public class DemoDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "这是示例名称")
    private String name;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "10")
    private Integer quantity;

    /**
     * 上线时间
     */
    @Schema(description = "上线时间", example = "2026-01-01 08:00:00", type = "string")
    private LocalDateTime onlineTime;

    /**
     * 扩展配置（JSON 对象）
     */
    @Schema(description = "扩展配置（JSON 对象）", type = "object")
    private Map<String, Object> extConfig;
}
