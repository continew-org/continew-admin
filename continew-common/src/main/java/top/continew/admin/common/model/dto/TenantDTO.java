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

package top.continew.admin.common.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 租户信息
 *
 * @author Charles7c
 * @since 4.0.0
 */
@Data
public class TenantDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 管理员用户名
     */
    private String adminUsername;

    /**
     * 管理员密码
     */
    private String adminPassword;

    /**
     * 套餐 ID
     */
    private Long packageId;
}
