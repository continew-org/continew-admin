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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.api.system.RoleApi;
import top.continew.admin.common.api.system.RoleMenuApi;
import top.continew.admin.common.api.tenant.TenantDataApi;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.service.PackageService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.core.util.SpringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 套餐菜单变更同步租户角色菜单的测试。
 *
 * @author Charles7c
 * @since 5.0.0
 */
class TenantServiceImplUpdateTenantMenuTest {

    private static final long PACKAGE_ID = 5L;
    private static final long TENANT_ID = 10L;
    private static final long TENANT_ADMIN_ROLE_ID = 3L;
    private static final List<Long> NEW_MENU_IDS = List.of(100L, 200L);

    private RoleMenuApi roleMenuApi;
    private RoleApi roleApi;
    private TenantServiceImpl tenantService;
    private MockedStatic<SpringUtils> springUtilsMock;

    @BeforeEach
    void setUp() {
        // 租户启停配置取自 Spring 容器，未配置即视为未启用，TenantUtils.execute 直接执行业务体
        springUtilsMock = mockStatic(SpringUtils.class);
        roleMenuApi = mock(RoleMenuApi.class);
        roleApi = mock(RoleApi.class);
        tenantService = buildService();
        TenantMapper tenantMapper = mock(TenantMapper.class);
        LambdaQueryChainWrapper<TenantDO> chain = mockChain();
        when(tenantMapper.lambdaQuery()).thenReturn(chain);
        TenantDO tenant = new TenantDO();
        tenant.setId(TENANT_ID);
        when(chain.list()).thenReturn(List.of(tenant));
        ReflectionTestUtils.setField(tenantService, "baseMapper", tenantMapper);
    }

    @AfterEach
    void tearDown() {
        springUtilsMock.close();
    }

    private TenantServiceImpl buildService() {
        // 构造期读取 Spring 上下文中的 TenantDataApi，单测环境置空
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            springUtil.when(() -> SpringUtil.getBeansOfType(TenantDataApi.class))
                .thenReturn(Map.of());
            return new TenantServiceImpl(mock(TenantExtensionProperties.class),
                mock(PackageService.class),
                mock(IdGeneratorProvider.class),
                roleMenuApi,
                roleApi);
        }
    }

    @SuppressWarnings("unchecked")
    private static LambdaQueryChainWrapper<TenantDO> mockChain() {
        LambdaQueryChainWrapper<TenantDO> chain = mock(LambdaQueryChainWrapper.class);
        when(chain.select(any(SFunction.class))).thenReturn(chain);
        when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        return chain;
    }

    @Test
    void shouldQueryAffectedRolesBeforeDeletingMenus() {
        when(roleMenuApi.listRoleIdByNotInMenuIds(NEW_MENU_IDS)).thenReturn(Set.of(1L, 2L));
        try (MockedStatic<CacheUtils> cacheUtils = mockStatic(CacheUtils.class)) {
            tenantService.updateTenantMenu(NEW_MENU_IDS, PACKAGE_ID);
        }

        // 查询受影响角色与删除旧菜单的执行顺序
        InOrder inOrder = inOrder(roleMenuApi);
        inOrder.verify(roleMenuApi).listRoleIdByNotInMenuIds(NEW_MENU_IDS);
        inOrder.verify(roleMenuApi).deleteByNotInMenuIds(NEW_MENU_IDS);
        // 删除前查出的受影响角色全部完成在线刷新
        verify(roleApi).updateUserContext(1L);
        verify(roleApi).updateUserContext(2L);
    }

    @Test
    void shouldRefreshTenantAdminRoleWithNewMenus() {
        when(roleMenuApi.listRoleIdByNotInMenuIds(NEW_MENU_IDS)).thenReturn(Set.of());
        when(roleApi.getIdByCode(RoleCodeEnum.TENANT_ADMIN.getCode()))
            .thenReturn(TENANT_ADMIN_ROLE_ID);
        try (MockedStatic<CacheUtils> cacheUtils = mockStatic(CacheUtils.class)) {
            tenantService.updateTenantMenu(NEW_MENU_IDS, PACKAGE_ID);
        }

        verify(roleMenuApi).add(NEW_MENU_IDS, TENANT_ADMIN_ROLE_ID);
        // 租户管理员角色持有新套餐菜单，在线上下文同步刷新
        verify(roleApi).updateUserContext(TENANT_ADMIN_ROLE_ID);
    }

    @Test
    void shouldSkipWhenPackageHasNoTenant() {
        TenantMapper tenantMapper = mock(TenantMapper.class);
        LambdaQueryChainWrapper<TenantDO> chain = mockChain();
        when(tenantMapper.lambdaQuery()).thenReturn(chain);
        when(chain.list()).thenReturn(List.of());
        ReflectionTestUtils.setField(tenantService, "baseMapper", tenantMapper);

        try (MockedStatic<CacheUtils> cacheUtils = mockStatic(CacheUtils.class)) {
            tenantService.updateTenantMenu(NEW_MENU_IDS, PACKAGE_ID);
        }

        verify(roleMenuApi, never()).deleteByNotInMenuIds(NEW_MENU_IDS);
        verify(roleMenuApi, never()).listRoleIdByNotInMenuIds(NEW_MENU_IDS);
    }
}
