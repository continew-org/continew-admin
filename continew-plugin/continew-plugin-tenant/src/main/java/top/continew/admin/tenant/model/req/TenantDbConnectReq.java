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

package top.continew.admin.tenant.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建或修改租户数据连接参数
 *
 * @author 小熊
 * @since 2024/12/12 19:13
 */
@Data
@Schema(description = "创建或修改租户数据连接参数")
public class TenantDbConnectReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 连接名称
     */
    @Schema(description = "连接名称")
    @NotBlank(message = "连接名称不能为空")
    @Length(max = 128, message = "连接名称长度不能超过 {max} 个字符")
    private String connectName;

    /**
     * 连接类型
     */
    @Schema(description = "连接类型")
    @NotNull(message = "连接类型不能为空")
    private Integer type;

    /**
     * 连接主机地址
     */
    @Schema(description = "连接主机地址")
    @NotBlank(message = "连接主机地址不能为空")
    @Length(max = 128, message = "连接主机地址长度不能超过 {max} 个字符")
    private String host;

    /**
     * 连接端口
     */
    @Schema(description = "连接端口")
    @NotNull(message = "连接端口不能为空")
    private Integer port;

    /**
     * 连接用户名
     */
    @Schema(description = "连接用户名")
    @NotBlank(message = "连接用户名不能为空")
    @Length(max = 128, message = "连接用户名长度不能超过 {max} 个字符")
    private String username;

    /**
     * 连接密码
     */
    @Schema(description = "连接密码")
    @NotBlank(message = "连接密码不能为空")
    @Length(max = 128, message = "连接密码长度不能超过 {max} 个字符")
    private String password;

    /**
     * ID
     */
    @Schema(hidden = true)
    private Long id;
}