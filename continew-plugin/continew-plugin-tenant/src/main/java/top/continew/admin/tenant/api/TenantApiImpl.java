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

package top.continew.admin.tenant.api;

import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.tenant.constant.TenantCacheConstants;
import top.continew.admin.tenant.mapper.PackageMapper;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.admin.tenant.model.entity.PackageDO;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.crud.model.entity.BaseIdDO;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户业务 API 实现
 *
 * @author Charles7c
 * @since 2025/7/23 21:13
 */
@Service
@RequiredArgsConstructor
public class TenantApiImpl implements TenantApi {

    private final TenantMapper baseMapper;
    private final PackageMapper packageMapper;
    private final TenantExtensionProperties tenantExtensionProperties;

    @Override
    public void checkStatus(Long tenantId) {
        // 默认租户承载超级管理员，不依赖租户套餐数据，保持与租户插件原有规则一致。
        // 租户功能开启时，null 表示会话没有可靠的租户归属，不能按默认租户放行。
        // 这也能阻断“租户功能关闭期间签发的旧 Refresh Session”在重新开启租户功能后
        // 绕过租户状态校验继续换取 Access Token。
        if (tenantId == null) {
            CheckUtils.throwIf(TenantContextHolder.isTenantEnabled(), "租户信息不存在");
            return;
        }
        if (tenantExtensionProperties.getDefaultTenantId() != null
            && tenantExtensionProperties.getDefaultTenantId().equals(tenantId)) {
            return;
        }
        TenantDO tenant = baseMapper.selectById(tenantId);
        CheckUtils.throwIfNull(tenant, "租户不存在");
        // 状态为空（异常数据）视为禁用，避免 throwIfEqual(DISABLE, null) 放行未知状态租户
        CheckUtils.throwIf(tenant.getStatus() == null
            || DisEnableStatusEnum.DISABLE.equals(tenant.getStatus()), "租户已被禁用");
        CheckUtils.throwIf(tenant.getExpireTime() != null && tenant.getExpireTime()
            .isBefore(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID)), "租户已过期");

        PackageDO tenantPackage = packageMapper.selectById(tenant.getPackageId());
        CheckUtils.throwIfNull(tenantPackage, "租户套餐不存在");
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, tenantPackage.getStatus(),
            "租户套餐已被禁用");
    }

    @Override
    public void bindAdminUser(Long tenantId, Long userId) {
        baseMapper.lambdaUpdate().set(TenantDO::getAdminUser, userId).eq(BaseIdDO::getId, tenantId)
            .update();
        // 更新租户缓存
        TenantDO entity = baseMapper.selectById(tenantId);
        RedisUtils.set(TenantCacheConstants.TENANT_KEY_PREFIX + tenantId, entity);
    }

    @Override
    public List<Long> listIdByPackageId(Long packageId) {
        return baseMapper.lambdaQuery()
            .select(TenantDO::getId)
            .eq(TenantDO::getPackageId, packageId)
            .list()
            .stream()
            .map(TenantDO::getId)
            .toList();
    }
}
