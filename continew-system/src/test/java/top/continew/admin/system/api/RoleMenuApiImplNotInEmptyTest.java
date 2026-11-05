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

package top.continew.admin.system.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.continew.admin.system.model.entity.RoleMenuDO;
import top.continew.admin.system.service.RoleMenuService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 角色菜单关联 API 空菜单列表行为的测试
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RoleMenuApiImplNotInEmptyTest {

    private RoleMenuService baseService;
    private RoleMenuApiImpl roleMenuApi;

    @BeforeEach
    void setUp() {
        baseService = mock(RoleMenuService.class);
        roleMenuApi = new RoleMenuApiImpl(baseService);
    }

    @Test
    void shouldDeleteAllWhenMenuIdsEmpty() {
        LambdaUpdateChainWrapper<RoleMenuDO> updateChain = mock(LambdaUpdateChainWrapper.class);
        doReturn(updateChain).when(baseService).lambdaUpdate();
        doReturn(updateChain).when(updateChain).notIn(anyBoolean(), any(SFunction.class),
            any(Collection.class));
        when(updateChain.remove()).thenReturn(true);

        roleMenuApi.deleteByNotInMenuIds(List.of());

        verify(updateChain).remove();
        verify(updateChain).notIn(eq(false), any(SFunction.class), any(Collection.class));
    }

    @Test
    void shouldDeleteNotInWhenMenuIdsPresent() {
        LambdaUpdateChainWrapper<RoleMenuDO> updateChain = mock(LambdaUpdateChainWrapper.class);
        doReturn(updateChain).when(baseService).lambdaUpdate();
        doReturn(updateChain).when(updateChain).notIn(anyBoolean(), any(SFunction.class),
            any(Collection.class));
        when(updateChain.remove()).thenReturn(true);

        roleMenuApi.deleteByNotInMenuIds(List.of(1L, 2L));

        verify(updateChain).notIn(eq(true), any(SFunction.class), any(Collection.class));
        verify(updateChain).remove();
    }

    @Test
    void shouldListAllRolesWhenMenuIdsEmpty() {
        RoleMenuDO roleMenu = new RoleMenuDO(1L, 100L);
        LambdaQueryChainWrapper<RoleMenuDO> queryChain = mock(LambdaQueryChainWrapper.class);
        doReturn(queryChain).when(baseService).lambdaQuery();
        doReturn(queryChain).when(queryChain).select(any(SFunction.class));
        doReturn(queryChain).when(queryChain).notIn(anyBoolean(), any(SFunction.class),
            any(Collection.class));
        when(queryChain.list()).thenReturn(List.of(roleMenu));

        Set<Long> roleIds = roleMenuApi.listRoleIdByNotInMenuIds(List.of());

        assertEquals(Set.of(1L), roleIds);
        verify(queryChain).notIn(eq(false), any(SFunction.class), any(Collection.class));
    }

    @Test
    void shouldListNotInWhenMenuIdsPresent() {
        LambdaQueryChainWrapper<RoleMenuDO> queryChain = mock(LambdaQueryChainWrapper.class);
        doReturn(queryChain).when(baseService).lambdaQuery();
        doReturn(queryChain).when(queryChain).select(any(SFunction.class));
        doReturn(queryChain).when(queryChain).notIn(anyBoolean(), any(SFunction.class),
            any(Collection.class));
        when(queryChain.list()).thenReturn(List.of());

        roleMenuApi.listRoleIdByNotInMenuIds(List.of(1L, 2L));

        verify(queryChain).notIn(eq(true), any(SFunction.class), any(Collection.class));
    }
}
