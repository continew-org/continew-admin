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
import top.continew.admin.tenant.model.entity.PackageDO;
import top.continew.admin.tenant.model.query.PackageQuery;
import top.continew.admin.tenant.model.req.PackageReq;
import top.continew.admin.tenant.model.resp.PackageDetailResp;
import top.continew.admin.tenant.model.resp.PackageResp;
import top.continew.starter.data.service.IService;

/**
 * 套餐业务接口
 *
 * @author 小熊
 * @since 2024/11/26 11:25
 */
public interface PackageService extends
    BaseService<PackageResp, PackageDetailResp, PackageQuery, PackageReq>, IService<PackageDO> {

    /**
     * 检查套餐状态
     *
     * @param id ID
     */
    void checkStatus(Long id);
}
