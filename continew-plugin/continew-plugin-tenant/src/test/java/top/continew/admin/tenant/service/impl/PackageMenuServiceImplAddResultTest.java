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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.continew.admin.tenant.mapper.PackageMenuMapper;
import top.continew.admin.tenant.model.entity.PackageMenuDO;

import java.util.List;

/**
 * 套餐菜单关联保存结果语义的测试。
 *
 * <p>套餐菜单置空撤销时没有新关联可插入，插入空集合的结果不能代表关联关系未变更，
 * 否则 {@code PackageServiceImpl.update} 会跳过 {@code updateTenantMenu}，租户角色的关联
 * 菜单在数据层残留，撤销在用户重新登录后仍不生效。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class PackageMenuServiceImplAddResultTest {

    private static final long PACKAGE_ID = 8L;
    private static final long MENU_ID_A = 201L;
    private static final long MENU_ID_B = 202L;

    private PackageMenuMapper baseMapper;
    private PackageMenuServiceImpl packageMenuService;

    @BeforeEach
    void setUp() {
        baseMapper = mock(PackageMenuMapper.class);
        packageMenuService = new PackageMenuServiceImpl(baseMapper);
    }

    @Test
    void shouldReportChangedWhenRevokeAllMenus() {
        this.stubOldMenus(List.of(this.packageMenu(MENU_ID_A)));
        LambdaUpdateChainWrapper<PackageMenuDO> updateChain = this.mockUpdateChain();

        boolean changed = packageMenuService.add(List.of(), PACKAGE_ID);

        assertTrue(changed);
        verify(updateChain).remove();
        verify(baseMapper, never()).insertBatch(anyList());
    }

    @Test
    void shouldReportChangedWhenMenusChanged() {
        this.stubOldMenus(List.of(this.packageMenu(MENU_ID_A)));
        this.mockUpdateChain();
        when(baseMapper.insertBatch(anyList())).thenReturn(true);

        assertTrue(packageMenuService.add(List.of(MENU_ID_A, MENU_ID_B), PACKAGE_ID));
    }

    @Test
    void shouldReturnFalseWhenNoDiff() {
        this.stubOldMenus(List.of(this.packageMenu(MENU_ID_A), this.packageMenu(MENU_ID_B)));

        assertFalse(packageMenuService.add(List.of(MENU_ID_B, MENU_ID_A), PACKAGE_ID));
    }

    private void stubOldMenus(List<PackageMenuDO> oldPackageMenus) {
        LambdaQueryChainWrapper<PackageMenuDO> queryChain = mock(LambdaQueryChainWrapper.class);
        doReturn(queryChain).when(baseMapper).lambdaQuery();
        doReturn(queryChain).when(queryChain).select(any(SFunction.class));
        doReturn(queryChain).when(queryChain).eq(any(SFunction.class), any());
        when(queryChain.list()).thenReturn(oldPackageMenus);
    }

    private LambdaUpdateChainWrapper<PackageMenuDO> mockUpdateChain() {
        LambdaUpdateChainWrapper<PackageMenuDO> updateChain = mock(LambdaUpdateChainWrapper.class);
        doReturn(updateChain).when(baseMapper).lambdaUpdate();
        doReturn(updateChain).when(updateChain).eq(any(SFunction.class), any());
        when(updateChain.remove()).thenReturn(true);
        return updateChain;
    }

    private PackageMenuDO packageMenu(Long menuId) {
        return new PackageMenuDO(PACKAGE_ID, menuId);
    }
}
