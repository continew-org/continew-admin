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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.tenant.constant.TenantCacheConstants;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.extension.crud.model.entity.BaseIdDO;

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

    @Override
    public void bindAdminUser(Long tenantId, Long userId) {
        baseMapper.lambdaUpdate().set(TenantDO::getAdminUser, userId).eq(BaseIdDO::getId, tenantId)
            .update();
        // 更新租户缓存
        TenantDO entity = baseMapper.selectById(tenantId);
        RedisUtils.set(TenantCacheConstants.TENANT_KEY_PREFIX + tenantId, entity);
    }
}
