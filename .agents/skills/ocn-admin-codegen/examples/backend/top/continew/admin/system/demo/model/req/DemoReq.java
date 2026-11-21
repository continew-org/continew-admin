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

package top.continew.admin.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 示例创建或修改请求参数
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@Schema(description = "示例创建或修改请求参数")
public class DemoReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "这是示例名称")
    @NotBlank(message = "名称不能为空")
    @Length(max = 64, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "10")
    @NotNull(message = "数量不能为空")
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
