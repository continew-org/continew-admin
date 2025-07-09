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

package top.continew.admin.tenant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.admin.tenant.mapper.TenantPackageMapper;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.entity.TenantPackageDO;
import top.continew.admin.tenant.model.query.TenantPackageQuery;
import top.continew.admin.tenant.model.req.TenantPackageReq;
import top.continew.admin.tenant.model.resp.TenantPackageDetailResp;
import top.continew.admin.tenant.model.resp.TenantPackageResp;
import top.continew.admin.tenant.service.TenantPackageService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.util.List;

/**
 * 租户套餐业务实现
 *
 * @author 小熊
 * @since 2024/11/26 11:25
 */
@Service
@RequiredArgsConstructor
public class TenantPackageServiceImpl extends BaseServiceImpl<TenantPackageMapper, TenantPackageDO, TenantPackageResp, TenantPackageDetailResp, TenantPackageQuery, TenantPackageReq> implements TenantPackageService {

    private final TenantMapper tenantMapper;

    @Override
    public TenantPackageDetailResp get(Long id) {
        TenantPackageDO tenantPackageDO = getById(id);
        TenantPackageDetailResp packageDetailResp = BeanUtil
            .copyProperties(tenantPackageDO, TenantPackageDetailResp.class);
        packageDetailResp.setMenuIds(new JSONArray(tenantPackageDO.getMenuIds()).toList(Long.class));
        fill(packageDetailResp);
        return packageDetailResp;
    }

    @Override
    protected void beforeCreate(TenantPackageReq req) {
        CheckUtils.throwIf(baseMapper.selectCount(Wrappers.lambdaQuery(TenantPackageDO.class)
            .eq(TenantPackageDO::getName, req.getName())) > 0, "租户套餐名称不能重复");
    }

    @Override
    protected void beforeDelete(List<Long> ids) {
        CheckUtils.throwIf(tenantMapper.selectCount(Wrappers.lambdaQuery(TenantDO.class)
            .in(TenantDO::getPackageId, ids)) > 0, "存在关联租户无法删除");
    }
}