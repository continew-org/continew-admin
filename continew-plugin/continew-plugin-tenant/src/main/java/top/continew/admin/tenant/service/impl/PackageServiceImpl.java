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

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.tenant.mapper.PackageMapper;
import top.continew.admin.tenant.model.entity.PackageDO;
import top.continew.admin.tenant.model.query.PackageQuery;
import top.continew.admin.tenant.model.req.PackageReq;
import top.continew.admin.tenant.model.resp.PackageDetailResp;
import top.continew.admin.tenant.model.resp.PackageResp;
import top.continew.admin.tenant.service.PackageMenuService;
import top.continew.admin.tenant.service.PackageService;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.util.List;

/**
 * 套餐业务实现
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/26 11:25
 */
@Service
@RequiredArgsConstructor
public class PackageServiceImpl extends
    BaseServiceImpl<PackageMapper, PackageDO, PackageResp, PackageDetailResp, PackageQuery, PackageReq>
    implements PackageService {

    private final PackageMenuService packageMenuService;
    @Lazy
    @Resource
    private TenantService tenantService;

    @Override
    public Long create(PackageReq req) {
        this.checkNameRepeat(req.getName(), null);
        // 新增信息
        Long id = super.create(req);
        // 保存套餐和菜单关联
        packageMenuService.add(req.getMenuIds(), id);
        return id;
    }

    @Override
    public void update(PackageReq req, Long id) {
        this.checkNameRepeat(req.getName(), id);
        // 更新信息
        super.update(req, id);
        // 保存套餐和菜单关联
        boolean isSaveMenuSuccess = packageMenuService.add(req.getMenuIds(), id);
        if (!isSaveMenuSuccess) {
            return;
        }
        // 更新租户菜单
        tenantService.updateTenantMenu(req.getMenuIds(), id);
    }

    @Override
    public void beforeDelete(List<Long> ids) {
        CheckUtils.throwIf(tenantService.countByPackageIds(ids) > 0, "所选套餐存在关联租户，不允许删除");
    }

    @Override
    public void checkStatus(Long id) {
        PackageDO entity = this.getById(id);
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, entity.getStatus(), "租户套餐已被禁用");
    }

    /**
     * 名称是否存在
     *
     * @param name 名称
     * @param id   ID
     */
    private void checkNameRepeat(String name, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(PackageDO::getName, name)
            .ne(id != null, PackageDO::getId, id)
            .exists(), "名称为 [{}] 的套餐已存在", name);
    }
}
