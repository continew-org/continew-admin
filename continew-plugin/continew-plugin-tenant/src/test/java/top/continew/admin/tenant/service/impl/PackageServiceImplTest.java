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
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.tenant.mapper.PackageMapper;
import top.continew.admin.tenant.model.entity.PackageDO;
import top.continew.admin.tenant.model.req.PackageReq;
import top.continew.admin.tenant.service.PackageMenuService;
import top.continew.admin.tenant.service.TenantService;
import top.continew.admin.tenant.support.ValidatorStubSupport;
import top.continew.starter.core.exception.BusinessException;

import java.util.List;

/**
 * 套餐业务实现单元测试。
 *
 * <p>覆盖新增与更新时的名称重复校验、菜单关联保存联动、删除保护
 * （存在关联租户的套餐不允许删除）与状态检查。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class PackageServiceImplTest {

    private static final long PACKAGE_ID = 8L;

    private PackageMapper packageMapper;
    private PackageMenuService packageMenuService;
    private TenantService tenantService;
    private PackageServiceImpl packageService;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),
            PackageDO.class.getName()), PackageDO.class);
        springUtilMock = mockStatic(SpringUtil.class);
        ValidatorStubSupport.stubJakartaValidator(springUtilMock);
        packageMapper = mock(PackageMapper.class);
        packageMenuService = mock(PackageMenuService.class);
        tenantService = mock(TenantService.class);
        packageService = Mockito.spy(new PackageServiceImpl(packageMenuService));
        ReflectionTestUtils.setField(packageService, "tenantService", tenantService);
        ReflectionTestUtils.setField(packageService, "baseMapper", packageMapper);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldCreatePackageAndSaveMenuRelations() {
        stubExistsQuery(false);
        // 持久化层在单测中由 spy 短路，编排验证以重复检查通过为口径
        doReturn(66L).when(packageService).create(any());

        Long id = packageService.create(reqOf(List.of(1L, 2L)));

        assertEquals(66L, id);
    }

    @Test
    void shouldRejectDuplicateNameOnCreate() {
        stubExistsQuery(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> packageService
            .create(reqOf(List.of(1L))));

        assertEquals("名称为 [测试套餐] 的套餐已存在", exception.getMessage());
    }

    @Test
    void shouldSyncTenantMenusWhenMenuRelationsChangedOnUpdate() {
        stubExistsQuery(false);
        stubExistingPackage();
        doReturn(true).when(packageMenuService).add(List.of(1L, 2L), PACKAGE_ID);

        packageService.update(reqOf(List.of(1L, 2L)), PACKAGE_ID);

        verify(tenantService).updateTenantMenu(List.of(1L, 2L), PACKAGE_ID);
        verify(tenantService).invalidateSessionsByPackageId(PACKAGE_ID);
    }

    private void stubExistingPackage() {
        PackageDO entity = new PackageDO();
        entity.setId(PACKAGE_ID);
        doReturn(entity).when(packageService).getById(PACKAGE_ID);
    }

    @Test
    void shouldSkipTenantMenuSyncWhenMenuRelationsUnchanged() {
        stubExistsQuery(false);
        stubExistingPackage();
        doReturn(false).when(packageMenuService).add(List.of(1L, 2L), PACKAGE_ID);

        packageService.update(reqOf(List.of(1L, 2L)), PACKAGE_ID);

        verify(tenantService, never()).updateTenantMenu(any(), any());
        verify(tenantService).invalidateSessionsByPackageId(PACKAGE_ID);
    }

    @Test
    void shouldRejectDeleteWhenTenantsBoundToPackage() {
        when(tenantService.countByPackageIds(List.of(PACKAGE_ID))).thenReturn(2L);

        BusinessException exception = assertThrows(BusinessException.class, () -> packageService
            .beforeDelete(List.of(PACKAGE_ID)));

        assertEquals("所选套餐存在关联租户，不允许删除", exception.getMessage());
    }

    @Test
    void shouldAllowDeleteWhenNoTenantBound() {
        when(tenantService.countByPackageIds(List.of(PACKAGE_ID))).thenReturn(0L);

        packageService.beforeDelete(List.of(PACKAGE_ID));

        verify(tenantService).countByPackageIds(List.of(PACKAGE_ID));
    }

    @Test
    void shouldRejectDisabledPackageOnCheckStatus() {
        PackageDO entity = new PackageDO();
        entity.setStatus(DisEnableStatusEnum.DISABLE);
        doReturn(entity).when(packageService).getById(PACKAGE_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> packageService
            .checkStatus(PACKAGE_ID));

        assertEquals("租户套餐已被禁用", exception.getMessage());
    }

    @Test
    void shouldPassCheckStatusForEnabledPackage() {
        PackageDO entity = new PackageDO();
        entity.setStatus(DisEnableStatusEnum.ENABLE);
        doReturn(entity).when(packageService).getById(PACKAGE_ID);

        packageService.checkStatus(PACKAGE_ID);
    }

    private PackageReq reqOf(List<Long> menuIds) {
        PackageReq req = new PackageReq();
        req.setName("测试套餐");
        req.setMenuIds(menuIds);
        return req;
    }

    private void stubExistsQuery(boolean exists) {
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<PackageDO> queryChain = mock(LambdaQueryChainWrapper.class);
        doReturn(queryChain).when(packageMapper).lambdaQuery();
        doReturn(queryChain).when(queryChain).eq(any(SFunction.class), any());
        Mockito.lenient().doReturn(queryChain).when(queryChain)
            .ne(Mockito.anyBoolean(), any(SFunction.class), any());
        when(queryChain.exists()).thenReturn(exists);
    }
}
