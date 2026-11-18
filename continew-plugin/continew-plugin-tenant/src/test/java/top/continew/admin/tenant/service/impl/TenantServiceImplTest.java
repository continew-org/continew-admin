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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.api.system.RoleApi;
import top.continew.admin.common.api.system.RoleMenuApi;
import top.continew.admin.common.api.tenant.TenantDataApi;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.tenant.constant.TenantCacheConstants;
import top.continew.admin.tenant.mapper.TenantMapper;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.req.TenantReq;
import top.continew.admin.tenant.service.PackageService;
import top.continew.admin.tenant.support.ValidatorStubSupport;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 租户业务实现单元测试。
 *
 * <p>覆盖新增（编码生成 + 租户数据初始化）、删除保护（默认租户）、更新前后钩子
 * （重复校验、套餐变更检查、缓存与在线会话失效）、状态检查与带缓存的域名/编码查询。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class TenantServiceImplTest {

    private static final long TENANT_ID = 2L;
    private static final long PACKAGE_ID = 5L;
    private static final long DEFAULT_TENANT_ID = 0L;

    private TenantMapper tenantMapper;
    private PackageService packageService;
    private IdGeneratorProvider idGeneratorProvider;
    private TenantDataApi tenantDataApi;
    private TenantServiceImpl tenantService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<RedisUtils> redisUtilsMock;
    private MockedStatic<CacheUtils> cacheUtilsMock;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),
            TenantDO.class.getName()), TenantDO.class);
        TenantExtensionProperties properties = new TenantExtensionProperties();
        properties.setDefaultTenantId(DEFAULT_TENANT_ID);
        // 构造期读取 Spring 上下文中的 TenantDataApi，编码重名检查经 CheckUtils 校验器取 bean，
        // 均需先桩 SpringUtil 再构造业务实例
        springUtilMock = mockStatic(SpringUtil.class);
        tenantDataApi = mock(TenantDataApi.class);
        // tenantDataApiMap 在构造期固化，TenantDataApi 必须在构造前放入上下文
        springUtilMock.when(() -> SpringUtil.getBeansOfType(TenantDataApi.class))
            .thenReturn(Map.of("userApi", tenantDataApi));
        ValidatorStubSupport.stubJakartaValidator(springUtilMock);
        redisUtilsMock = mockStatic(RedisUtils.class);
        cacheUtilsMock = mockStatic(CacheUtils.class);
        tenantMapper = mock(TenantMapper.class);
        packageService = mock(PackageService.class);
        idGeneratorProvider = mock(IdGeneratorProvider.class);
        tenantService = spy(new TenantServiceImpl(properties, packageService,
            idGeneratorProvider, mock(RoleMenuApi.class), mock(RoleApi.class)));
        ReflectionTestUtils.setField(tenantService, "baseMapper", tenantMapper);
    }

    @AfterEach
    void tearDown() {
        cacheUtilsMock.close();
        redisUtilsMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldRejectDuplicateNameOnCreate() {
        stubExistsQuery(true);
        TenantReq req = req();

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantService
            .create(req));

        assertEquals("名称为 [测试租户] 的租户已存在", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateDomainOnCreate() {
        stubExistsQuery(false, true);
        TenantReq req = req();

        assertThrows(BusinessException.class, () -> tenantService.create(req));
    }

    @Test
    void shouldRejectDeleteOfDefaultTenant() {
        List<Long> ids = List.of(DEFAULT_TENANT_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantService
            .delete(ids));

        assertEquals("默认租户不允许删除", exception.getMessage());
    }

    @Test
    void shouldClearTenantDataBeforeDelete() {
        stubExistsQuery(false);

        tenantService.delete(List.of(TENANT_ID));

        verify(tenantDataApi).clear();
    }

    @Test
    void shouldCheckPackageOnUpdateWhenPackageChanged() {
        stubExistsQuery(false);
        TenantDO tenant = new TenantDO();
        tenant.setId(TENANT_ID);
        tenant.setPackageId(PACKAGE_ID);
        doReturn(tenant).when(tenantService).getById(TENANT_ID);
        TenantReq req = req();
        req.setPackageId(PACKAGE_ID + 1);

        tenantService.beforeUpdate(req, TENANT_ID);

        verify(packageService).checkStatus(PACKAGE_ID + 1);
    }

    @Test
    void shouldSkipPackageCheckOnUpdateWhenPackageUnchanged() {
        stubExistsQuery(false);
        TenantDO tenant = new TenantDO();
        tenant.setId(TENANT_ID);
        tenant.setPackageId(PACKAGE_ID);
        doReturn(tenant).when(tenantService).getById(TENANT_ID);
        TenantReq req = req();
        req.setPackageId(PACKAGE_ID);

        tenantService.beforeUpdate(req, TENANT_ID);

        verify(packageService, never()).checkStatus(PACKAGE_ID);
    }

    @Test
    void shouldInvalidateCacheAndSessionsAfterUpdate() {
        TenantDO entity = new TenantDO();
        entity.setId(TENANT_ID);

        tenantService.afterUpdate(req(), entity);

        redisUtilsMock.verify(() -> RedisUtils
            .deleteByPattern(TenantCacheConstants.TENANT_KEY_PREFIX + "*"));
        cacheUtilsMock.verify(() -> CacheUtils.evictAll(TenantCacheConstants.TENANT_KEY_PREFIX));
        verify(tenantDataApi).invalidateSessions();
    }

    @Test
    void shouldInvalidateSessionsByPackageId() {
        stubListIdByPackageId(List.of(TENANT_ID));

        tenantService.invalidateSessionsByPackageId(PACKAGE_ID);

        verify(tenantDataApi).invalidateSessions();
    }

    @Test
    void shouldPassCheckStatusForDefaultTenant() {
        tenantService.checkStatus(DEFAULT_TENANT_ID);

        verify(tenantMapper, never()).selectById(any());
    }

    @Test
    void shouldRejectCheckStatusForMissingTenant() {
        doReturn(null).when(tenantService).getById(TENANT_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantService
            .checkStatus(TENANT_ID));

        assertEquals("租户不存在", exception.getMessage());
    }

    @Test
    void shouldRejectDisabledTenantOnCheckStatus() {
        TenantDO tenant = new TenantDO();
        tenant.setId(TENANT_ID);
        tenant.setStatus(DisEnableStatusEnum.DISABLE);
        doReturn(tenant).when(tenantService).getById(TENANT_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantService
            .checkStatus(TENANT_ID));

        assertEquals("租户已被禁用", exception.getMessage());
    }

    @Test
    void shouldRejectExpiredTenantOnCheckStatus() {
        TenantDO tenant = new TenantDO();
        tenant.setId(TENANT_ID);
        tenant.setStatus(DisEnableStatusEnum.ENABLE);
        tenant.setExpireTime(LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).minusDays(1));
        doReturn(tenant).when(tenantService).getById(TENANT_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantService
            .checkStatus(TENANT_ID));

        assertEquals("租户已过期", exception.getMessage());
    }

    @Test
    void shouldCheckPackageOnCheckStatusForValidTenant() {
        TenantDO tenant = new TenantDO();
        tenant.setId(TENANT_ID);
        tenant.setStatus(DisEnableStatusEnum.ENABLE);
        tenant.setPackageId(PACKAGE_ID);
        doReturn(tenant).when(tenantService).getById(TENANT_ID);

        tenantService.checkStatus(TENANT_ID);

        verify(packageService).checkStatus(PACKAGE_ID);
    }

    @Test
    void shouldCacheTenantIdResolvedByDomain() {
        stubCachedIdQuery(100L);

        Long id = tenantService.getIdByDomain("a.example.com");

        assertEquals(100L, id);
        cacheUtilsMock.verify(() -> CacheUtils.put(TenantCacheConstants.TENANT_KEY_PREFIX,
            "a.example.com", 100L));
    }

    @Test
    void shouldNotCacheNullTenantIdResolvedByCode() {
        stubCachedIdQuery(null);

        Long id = tenantService.getIdByCode("T9999");

        assertNull(id);
        cacheUtilsMock.verify(() -> CacheUtils.put(anyString(), any(), any()), never());
    }

    @Test
    void shouldReturnCachedTenantIdWithoutLoading() {
        when(CacheUtils.get(TenantCacheConstants.TENANT_KEY_PREFIX, "cached.example.com"))
            .thenReturn(77L);

        Long id = tenantService.getIdByDomain("cached.example.com");

        assertEquals(77L, id);
        verify(tenantMapper, never()).lambdaQuery();
    }

    @Test
    void shouldCountTenantsByPackageIds() {
        LambdaQueryChainWrapper<TenantDO> queryChain = mockChain();
        doReturn(queryChain).when(tenantMapper).lambdaQuery();
        when(queryChain.count()).thenReturn(3L);

        assertEquals(3L, tenantService.countByPackageIds(List.of(PACKAGE_ID)));
    }

    private TenantReq req() {
        TenantReq req = new TenantReq();
        req.setName("测试租户");
        req.setDomain("a.example.com");
        req.setPackageId(PACKAGE_ID);
        return req;
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<TenantDO> mockChain() {
        LambdaQueryChainWrapper<TenantDO> queryChain = mock(LambdaQueryChainWrapper.class);
        doReturn(queryChain).when(queryChain).select(any(SFunction.class));
        doReturn(queryChain).when(queryChain).eq(any(SFunction.class), any());
        doReturn(queryChain).when(queryChain).ne(anyBoolean(), any(SFunction.class), any());
        doReturn(queryChain).when(queryChain).in(any(SFunction.class),
            any(java.util.Collection.class));
        return queryChain;
    }

    /**
     * 桩重复检查查询
     *
     * @param nameExists 名称重复检查结果
     */
    private void stubExistsQuery(boolean nameExists) {
        stubExistsQuery(nameExists, false);
    }

    private void stubExistsQuery(boolean nameExists, boolean domainExists) {
        LambdaQueryChainWrapper<TenantDO> queryChain = mockChain();
        doReturn(queryChain).when(tenantMapper).lambdaQuery();
        when(queryChain.exists()).thenReturn(nameExists, domainExists);
    }

    private void stubCachedIdQuery(Long id) {
        LambdaQueryChainWrapper<TenantDO> queryChain = mockChain();
        doReturn(queryChain).when(tenantMapper).lambdaQuery();
        doReturn(queryChain).when(queryChain).select(any(SFunction.class));
        lenient().doReturn(queryChain).when(queryChain)
            .eq(any(SFunction.class), any());
        if (id != null) {
            TenantDO tenant = new TenantDO();
            tenant.setId(id);
            when(queryChain.oneOpt()).thenReturn(java.util.Optional.of(tenant));
        } else {
            when(queryChain.oneOpt()).thenReturn(java.util.Optional.empty());
        }
    }

    private void stubListIdByPackageId(List<Long> ids) {
        LambdaQueryChainWrapper<TenantDO> queryChain = mockChain();
        doReturn(queryChain).when(tenantMapper).lambdaQuery();
        List<TenantDO> tenants = ids.stream().map(id -> {
            TenantDO tenant = new TenantDO();
            tenant.setId(id);
            return tenant;
        }).toList();
        when(queryChain.list()).thenReturn(tenants);
    }
}
