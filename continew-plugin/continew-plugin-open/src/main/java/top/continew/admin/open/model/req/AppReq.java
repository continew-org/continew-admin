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

package top.continew.admin.open.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.enums.DisEnableStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用创建或修改请求参数
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
@Data
@Schema(description = "应用创建或修改请求参数")
public class AppReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "应用1")
    @NotBlank(message = "名称不能为空")
    @Length(max = 100, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2023-08-08 23:59:59", type = "string")
    @Future(message = "失效时间必须是未来时间")
    private LocalDateTime expireTime;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "应用1描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * Access Key（访问密钥）
     */
    @Schema(hidden = true)
    private String accessKey;

    /**
     * Secret Key（密钥）
     */
    @Schema(hidden = true)
    private String secretKey;
}