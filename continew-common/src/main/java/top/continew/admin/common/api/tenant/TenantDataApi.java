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

import top.continew.admin.common.model.dto.TenantDTO;

/**
 * 租户数据 API
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/12/2 20:08
 */
public interface TenantDataApi {

    /**
     * 初始化数据
     *
     * @param tenant 租户信息
     */
    void init(TenantDTO tenant);

    /**
     * 清除数据
     */
    void clear();
}
