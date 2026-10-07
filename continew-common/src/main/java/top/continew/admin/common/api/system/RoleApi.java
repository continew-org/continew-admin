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

package top.continew.admin.common.api.system;

/**
 * 角色业务 API
 *
 * @author Charles7c
 * @since 4.0.0
 */
public interface RoleApi {

    /**
     * 根据编码查询 ID
     *
     * @param code 编码
     * @return 角色 ID
     */
    Long getIdByCode(String code);

    /**
     * 更新用户上下文
     *
     * @param roleId 角色 ID
     */
    void updateUserContext(Long roleId);
}
