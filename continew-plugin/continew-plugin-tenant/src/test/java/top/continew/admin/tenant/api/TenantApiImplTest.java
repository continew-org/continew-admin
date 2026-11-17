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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.tenant.constant.TenantCacheConstants;
import top.continew.admin.tenant.mapper.PackageMapper;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.admin.tenant.model.entity.PackageDO;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.support.ValidatorStubSupport;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.autoconfigure.TenantProperties;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户业务 API 实现单元测试。
 *
 * <p>覆盖会话租户状态校验（默认租户直通、禁用/过期/套餐联动拒绝）与
 * 管理员绑定后的租户详情缓存刷新。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class TenantApiImplTest {

    private static final long DEFAULT_TENANT_ID = 0L;
    private static final long TENANT_ID = 2L;
    private static final long PACKAGE_ID = 5L;

    private TenantMapper tenantMapper;
    private PackageMapper packageMapper;
    private TenantApiImpl tenantApi;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<RedisUtils> redisUtilsMock;

    @BeforeEach
    void setUp() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(),
            TenantDO.class.getName());
        TableInfoHelper.initTableInfo(assistant, TenantDO.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),
            PackageDO.class.getName()), PackageDO.class);
        // isTenantEnabled 与 CheckUtils 的校验器均经 SpringUtil 取 bean，先桩再触达业务类
        springUtilMock = mockStatic(SpringUtil.class);
        ValidatorStubSupport.stubJakartaValidator(springUtilMock);
        tenantMapper = mock(TenantMapper.class);
        packageMapper = mock(PackageMapper.class);
        TenantExtensionProperties properties = new TenantExtensionProperties();
        properties.setDefaultTenantId(DEFAULT_TENANT_ID);
        tenantApi = new TenantApiImpl(tenantMapper, packageMapper, properties);
        redisUtilsMock = Mockito.mockStatic(RedisUtils.class);
    }

    @AfterEach
    void tearDown() {
        redisUtilsMock.close();
        springUtilMock.close();
        TenantContextHolder.clear();
    }

    @Test
    void shouldRejectNullTenantIdWhenTenantEnabled() {
        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setEnabled(true);
        // TenantContextHolder.isTenantEnabled 内部经 SpringUtils 转 SpringUtil.getBean(Class)
        springUtilMock.when(() -> SpringUtil.getBean(TenantProperties.class))
            .thenReturn(tenantProperties);
        assertThrows(BusinessException.class, () -> tenantApi.checkStatus(null));
    }

    @Test
    void shouldPassNullTenantIdWhenTenantDisabled() {
        assertDoesNotThrow(() -> tenantApi.checkStatus(null));
    }

    @Test
    void shouldReturnEarlyForDefaultTenant() {
        assertDoesNotThrow(() -> tenantApi.checkStatus(DEFAULT_TENANT_ID));
        verify(tenantMapper, never()).selectById(any());
    }

    @Test
    void shouldRejectUnknownTenant() {
        when(tenantMapper.selectById(TENANT_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantApi
            .checkStatus(TENANT_ID));

        assertEquals("租户不存在", exception.getMessage());
    }

    @Test
    void shouldTreatNullStatusAsDisabled() {
        when(tenantMapper.selectById(TENANT_ID)).thenReturn(tenant(null, null));

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantApi
            .checkStatus(TENANT_ID));

        assertEquals("租户已被禁用", exception.getMessage());
    }

    @Test
    void shouldRejectDisabledTenant() {
        when(tenantMapper.selectById(TENANT_ID))
            .thenReturn(tenant(DisEnableStatusEnum.DISABLE, null));

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantApi
            .checkStatus(TENANT_ID));

        assertEquals("租户已被禁用", exception.getMessage());
    }

    @Test
    void shouldRejectExpiredTenant() {
        TenantDO tenant = tenant(DisEnableStatusEnum.ENABLE, LocalDateTime
            .now(GlobalConstants.DEFAULT_ZONE_ID)
            .minusDays(1));
        when(tenantMapper.selectById(TENANT_ID)).thenReturn(tenant);

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantApi
            .checkStatus(TENANT_ID));

        assertEquals("租户已过期", exception.getMessage());
    }

    @Test
    void shouldRejectMissingPackage() {
        when(tenantMapper.selectById(TENANT_ID))
            .thenReturn(tenant(DisEnableStatusEnum.ENABLE, null));
        when(packageMapper.selectById(PACKAGE_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantApi
            .checkStatus(TENANT_ID));

        assertEquals("租户套餐不存在", exception.getMessage());
    }

    @Test
    void shouldRejectDisabledPackage() {
        when(tenantMapper.selectById(TENANT_ID))
            .thenReturn(tenant(DisEnableStatusEnum.ENABLE, null));
        PackageDO tenantPackage = new PackageDO();
        tenantPackage.setStatus(DisEnableStatusEnum.DISABLE);
        when(packageMapper.selectById(PACKAGE_ID)).thenReturn(tenantPackage);

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantApi
            .checkStatus(TENANT_ID));

        assertEquals("租户套餐已被禁用", exception.getMessage());
    }

    @Test
    void shouldPassEnabledTenantWithEnabledPackage() {
        when(tenantMapper.selectById(TENANT_ID))
            .thenReturn(tenant(DisEnableStatusEnum.ENABLE, null));
        PackageDO tenantPackage = new PackageDO();
        tenantPackage.setStatus(DisEnableStatusEnum.ENABLE);
        when(packageMapper.selectById(PACKAGE_ID)).thenReturn(tenantPackage);

        assertDoesNotThrow(() -> tenantApi.checkStatus(TENANT_ID));
    }

    @Test
    void shouldBindAdminUserAndRefreshTenantCache() {
        LambdaUpdateChainWrapper<TenantDO> updateChain = mock(LambdaUpdateChainWrapper.class);
        doReturn(updateChain).when(tenantMapper).lambdaUpdate();
        doReturn(updateChain).when(updateChain).set(any(SFunction.class), any());
        doReturn(updateChain).when(updateChain).eq(any(SFunction.class), any());
        when(updateChain.update()).thenReturn(true);
        TenantDO tenant = new TenantDO();
        tenant.setId(TENANT_ID);
        when(tenantMapper.selectById(TENANT_ID)).thenReturn(tenant);

        tenantApi.bindAdminUser(TENANT_ID, 9L);

        verify(updateChain).update();
        redisUtilsMock.verify(() -> RedisUtils.set(TenantCacheConstants.TENANT_KEY_PREFIX
            + TENANT_ID, tenant));
    }

    @Test
    void shouldListTenantIdsByPackageId() {
        LambdaQueryChainWrapper<TenantDO> queryChain = mockChain();
        doReturn(queryChain).when(tenantMapper).lambdaQuery();
        TenantDO tenantA = new TenantDO();
        tenantA.setId(11L);
        TenantDO tenantB = new TenantDO();
        tenantB.setId(12L);
        when(queryChain.list()).thenReturn(List.of(tenantA, tenantB));

        List<Long> ids = tenantApi.listIdByPackageId(PACKAGE_ID);

        assertEquals(List.of(11L, 12L), ids);
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<TenantDO> mockChain() {
        LambdaQueryChainWrapper<TenantDO> queryChain = mock(LambdaQueryChainWrapper.class);
        doReturn(queryChain).when(queryChain).select(any(SFunction.class));
        doReturn(queryChain).when(queryChain).eq(any(SFunction.class), any());
        return queryChain;
    }

    private TenantDO tenant(DisEnableStatusEnum status, LocalDateTime expireTime) {
        TenantDO tenant = new TenantDO();
        tenant.setId(TENANT_ID);
        tenant.setStatus(status);
        tenant.setExpireTime(expireTime);
        tenant.setPackageId(PACKAGE_ID);
        return tenant;
    }
}
