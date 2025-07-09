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

package top.continew.admin.tenant.service;

import top.continew.admin.common.base.service.BaseService;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.query.TenantQuery;
import top.continew.admin.tenant.model.req.TenantReq;
import top.continew.admin.tenant.model.resp.TenantAvailableResp;
import top.continew.admin.tenant.model.resp.TenantDetailResp;
import top.continew.admin.tenant.model.resp.TenantResp;
import top.continew.starter.data.service.IService;

import java.util.List;

/**
 * 租户业务接口
 *
 * @author 小熊
 * @since 2024/11/26 17:20
 */
public interface TenantService extends BaseService<TenantResp, TenantDetailResp, TenantQuery, TenantReq>, IService<TenantDO> {

    /**
     * 获取所有可用的租户列表
     */
    List<TenantAvailableResp> getAvailableList();

    /**
     * 租户绑定用户
     */
    void bindUser(Long tenantId, Long userId);

    /**
     * 检查租户状态
     */
    void checkStatus();

    /**
     * 根据id获取租户DO
     */
    TenantDO getTenantById(Long id);

    /**
     * 根据用户id获取租户信息
     */
    TenantDO getTenantByUserId(Long userId);

}