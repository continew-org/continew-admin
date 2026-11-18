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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.starter.auth.refresh.token.api.AuthPolicyLockTarget;

import java.util.Collection;
import java.util.List;

/**
 * 套餐策略锁目标解析单元测试。
 *
 * <p>覆盖套餐 ID 参数提取、无效参数拒绝与解析结果到租户锁目标的映射。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class PackageTenantPolicyLockTargetResolverTest {

    private static final long PACKAGE_ID = 8L;

    private TenantApi tenantApi;
    private PackageTenantPolicyLockTargetResolver resolver;

    @BeforeEach
    void setUp() {
        tenantApi = mock(TenantApi.class);
        resolver = new PackageTenantPolicyLockTargetResolver(tenantApi);
    }

    @Test
    void shouldResolvePackageIdArgToTenantTargets() {
        when(tenantApi.listIdByPackageId(PACKAGE_ID)).thenReturn(List.of(11L, 12L));

        Collection<AuthPolicyLockTarget> targets = resolver.resolve(new Object[] {"req",
            PACKAGE_ID});

        assertEquals(2, targets.size());
        AuthPolicyLockTarget target = targets.iterator().next();
        assertEquals(AuthPolicyLockTarget.Type.TENANT, target.type());
    }

    @Test
    void shouldRejectWhenPackageIdArgMissing() {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(new Object[] {"req"}));
    }

    @Test
    void shouldRejectWhenPackageIdArgNotLong() {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(new Object[] {"req",
            "not-long"}));
    }

    @Test
    void shouldMapToTenantKeyFromPackageTenants() {
        when(tenantApi.listIdByPackageId(PACKAGE_ID)).thenReturn(List.of(30L));

        Collection<AuthPolicyLockTarget> targets = resolver.resolve(new Object[] {"req",
            PACKAGE_ID});

        assertEquals("30", targets.iterator().next().key());
    }
}
