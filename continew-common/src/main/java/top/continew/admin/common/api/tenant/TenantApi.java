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

package top.continew.admin.common.api.tenant;

import java.util.List;

/**
 * 租户业务 API
 *
 * @author Charles7c
 * @since 2025/7/23 21:13
 */
public interface TenantApi {

    /**
     * 校验租户是否可以继续提供服务。
     *
     * @param tenantId 租户 ID
     */
    void checkStatus(Long tenantId);

    /**
     * 绑定租户管理员用户
     *
     * @param tenantId 租户 ID
     * @param userId   用户 ID
     */
    void bindAdminUser(Long tenantId, Long userId);

    /**
     * 查询指定套餐当前关联的租户 ID。
     *
     * @param packageId 套餐 ID
     * @return 租户 ID 列表
     */
    List<Long> listIdByPackageId(Long packageId);
}
