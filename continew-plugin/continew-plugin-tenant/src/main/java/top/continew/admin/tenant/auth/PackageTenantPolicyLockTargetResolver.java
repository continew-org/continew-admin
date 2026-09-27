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

package top.continew.admin.tenant.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.api.AuthPolicyLockTarget;
import top.continew.admin.auth.api.AuthPolicyLockTargetResolver;
import top.continew.admin.common.api.tenant.TenantApi;

import java.util.Collection;

/** 将套餐变更影响的租户解析为认证会话策略锁目标。 */
@Component
@RequiredArgsConstructor
public class PackageTenantPolicyLockTargetResolver implements AuthPolicyLockTargetResolver {

    private final TenantApi tenantApi;

    @Override
    public Collection<AuthPolicyLockTarget> resolve(Object[] args) {
        if (args.length <= 1 || !(args[1] instanceof Long packageId)) {
            throw new IllegalArgumentException("认证套餐策略锁参数无效");
        }
        return tenantApi.listIdByPackageId(packageId).stream().map(AuthPolicyLockTarget::tenant)
            .toList();
    }
}
