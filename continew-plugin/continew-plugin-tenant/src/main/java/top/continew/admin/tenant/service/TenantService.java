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
import top.continew.admin.tenant.model.resp.TenantDetailResp;
import top.continew.admin.tenant.model.resp.TenantResp;
import top.continew.starter.data.service.IService;

import java.util.List;

/**
 * 租户业务接口
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/26 17:20
 */
public interface TenantService
    extends BaseService<TenantResp, TenantDetailResp, TenantQuery, TenantReq>, IService<TenantDO> {

    /**
     * 根据域名查询
     *
     * @param domain 域名
     * @return ID
     */
    Long getIdByDomain(String domain);

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return ID
     */
    Long getIdByCode(String code);

    /**
     * 检查租户状态
     *
     * @param id ID
     */
    void checkStatus(Long id);

    /**
     * 更新租户菜单
     *
     * @param newMenuIds 新菜单 ID 列表
     * @param packageId  套餐 ID
     */
    void updateTenantMenu(List<Long> newMenuIds, Long packageId);

    /**
     * 根据套餐 ID 查询数量
     *
     * @param packageIds 套餐 ID 列表
     * @return 租户数量
     */
    Long countByPackageIds(List<Long> packageIds);
}
