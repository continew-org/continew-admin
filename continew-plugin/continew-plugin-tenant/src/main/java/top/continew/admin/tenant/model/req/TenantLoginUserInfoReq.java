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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 租户登录用户信息
 * @author: 小熊
 * @create: 2024-12-02 20:41
 */
@Data
public class TenantLoginUserInfoReq implements Serializable {

    /**
     * 租户id
     */
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    /**
     * 登录用户名
     */
    @NotEmpty(message = "登录用户名不能为空")
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * ID
     */
    @Schema(hidden = true)
    private Long id;
}
