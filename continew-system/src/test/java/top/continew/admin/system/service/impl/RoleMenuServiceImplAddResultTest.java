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

package top.continew.admin.system.service.impl;

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
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.system.mapper.RoleMenuMapper;
import top.continew.admin.system.model.entity.RoleMenuDO;

import java.util.List;

/**
 * 角色菜单关联保存结果语义的测试。
 *
 * <p>回归：菜单权限置空撤销时没有新关联可插入，插入空集合的结果不能代表关联关系未变更，
 * 否则 {@code RoleServiceImpl.updatePermission} 会跳过在线会话的权限快照同步，已撤销权限
 * 在在线用户身上继续生效。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RoleMenuServiceImplAddResultTest {

    private static final long ROLE_ID = 7L;
    private static final long MENU_ID_A = 101L;
    private static final long MENU_ID_B = 102L;

    private RoleMenuServiceImpl roleMenuService;
    private RoleMenuMapper baseMapper;

    @BeforeEach
    void setUp() {
        baseMapper = mock(RoleMenuMapper.class);
        roleMenuService = new RoleMenuServiceImpl();
        ReflectionTestUtils.setField(roleMenuService, "baseMapper", baseMapper);
    }

    @Test
    void shouldReportChangedWhenRevokeAllMenus() {
        this.stubOldMenus(List.of(this.roleMenu(MENU_ID_A)));
        LambdaUpdateChainWrapper<RoleMenuDO> updateChain = this.mockUpdateChain();

        boolean changed = roleMenuService.add(List.of(), ROLE_ID);

        assertTrue(changed);
        verify(updateChain).remove();
        verify(baseMapper, never()).insertBatch(anyList());
    }

    @Test
    void shouldReportChangedWhenMenusChanged() {
        this.stubOldMenus(List.of(this.roleMenu(MENU_ID_A)));
        this.mockUpdateChain();
        when(baseMapper.insertBatch(anyList())).thenReturn(true);

        assertTrue(roleMenuService.add(List.of(MENU_ID_A, MENU_ID_B), ROLE_ID));
    }

    @Test
    void shouldReturnFalseWhenNoDiff() {
        this.stubOldMenus(List.of(this.roleMenu(MENU_ID_A), this.roleMenu(MENU_ID_B)));

        assertFalse(roleMenuService.add(List.of(MENU_ID_B, MENU_ID_A), ROLE_ID));
    }

    private void stubOldMenus(List<RoleMenuDO> oldRoleMenus) {
        LambdaQueryChainWrapper<RoleMenuDO> queryChain = mock(LambdaQueryChainWrapper.class);
        doReturn(queryChain).when(baseMapper).lambdaQuery();
        doReturn(queryChain).when(queryChain).select(any(SFunction.class));
        doReturn(queryChain).when(queryChain).eq(any(SFunction.class), any());
        when(queryChain.list()).thenReturn(oldRoleMenus);
    }

    private LambdaUpdateChainWrapper<RoleMenuDO> mockUpdateChain() {
        LambdaUpdateChainWrapper<RoleMenuDO> updateChain = mock(LambdaUpdateChainWrapper.class);
        doReturn(updateChain).when(baseMapper).lambdaUpdate();
        doReturn(updateChain).when(updateChain).eq(any(SFunction.class), any());
        when(updateChain.remove()).thenReturn(true);
        return updateChain;
    }

    private RoleMenuDO roleMenu(Long menuId) {
        return new RoleMenuDO(ROLE_ID, menuId);
    }
}
