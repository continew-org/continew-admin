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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建或修改短信记录参数
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Data
@Schema(description = "创建或修改短信记录参数")
public class SmsRecordReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置id
     */
    @Schema(description = "配置id")
    @NotNull(message = "配置id不能为空")
    private Long configId;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @NotBlank(message = "手机号不能为空")
    @Length(max = 25, message = "手机号长度不能超过 {max} 个字符")
    private String phone;

    /**
     * 参数配置
     */
    @Schema(description = "参数配置")
    private String params;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    @NotNull(message = "发送状态不能为空")
    private Boolean status;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据")
    @NotBlank(message = "返回数据不能为空")
    @Length(max = 2048, message = "返回数据长度不能超过 {max} 个字符")
    private String resMsg;
}