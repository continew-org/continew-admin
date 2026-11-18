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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.continew.admin.system.mapper.RoleDeptMapper;
import top.continew.admin.system.model.entity.RoleDeptDO;

import java.util.Collection;
import java.util.List;

/**
 * 角色和部门关联业务实现单元测试。
 *
 * <p>覆盖新增关联时的变更检测与先删后插、按角色/部门批量删除的空集合早返回与逻辑删除、
 * 按角色查询部门 ID 列表的委托。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RoleDeptServiceImplTest {

    private static final Long ROLE_ID = 3L;

    private RoleDeptMapper baseMapper;
    private RoleDeptServiceImpl roleDeptService;

    @BeforeEach
    void setUp() {
        baseMapper = mock(RoleDeptMapper.class);
        roleDeptService = new RoleDeptServiceImpl(baseMapper);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSkipAddWhenNoChange() {
        stubExistingDeptIds(List.of(1L, 2L));

        boolean result = roleDeptService.add(List.of(1L, 2L), ROLE_ID);

        assertFalse(result);
        verify(baseMapper, never()).lambdaUpdate();
        verify(baseMapper, never()).insertBatch(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteThenInsertWhenChanged() {
        stubExistingDeptIds(List.of(1L));
        LambdaUpdateChainWrapper<RoleDeptDO> updateChain = mock(LambdaUpdateChainWrapper.class);
        when(baseMapper.lambdaUpdate()).thenReturn(updateChain);
        lenient().when(updateChain.eq(any(SFunction.class), any())).thenReturn(updateChain);
        when(updateChain.remove()).thenReturn(true);
        when(baseMapper.insertBatch(any())).thenReturn(true);

        boolean result = roleDeptService.add(List.of(1L, 2L), ROLE_ID);

        assertTrue(result);
        verify(updateChain).remove();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<RoleDeptDO>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(baseMapper).insertBatch(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void shouldSkipDeleteByRoleIdsWhenEmpty() {
        roleDeptService.deleteByRoleIds(List.of());

        verify(baseMapper, never()).lambdaUpdate();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteByRoleIdsWhenPresent() {
        LambdaUpdateChainWrapper<RoleDeptDO> chain = mock(LambdaUpdateChainWrapper.class);
        when(baseMapper.lambdaUpdate()).thenReturn(chain);
        when(chain.in(any(SFunction.class), any(Collection.class))).thenReturn(chain);
        when(chain.remove()).thenReturn(true);

        roleDeptService.deleteByRoleIds(List.of(ROLE_ID));

        verify(chain).remove();
    }

    @Test
    void shouldSkipDeleteByDeptIdsWhenEmpty() {
        roleDeptService.deleteByDeptIds(List.of());

        verify(baseMapper, never()).lambdaUpdate();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteByDeptIdsWhenPresent() {
        LambdaUpdateChainWrapper<RoleDeptDO> chain = mock(LambdaUpdateChainWrapper.class);
        when(baseMapper.lambdaUpdate()).thenReturn(chain);
        when(chain.in(any(SFunction.class), any(Collection.class))).thenReturn(chain);
        when(chain.remove()).thenReturn(true);

        roleDeptService.deleteByDeptIds(List.of(10L));

        verify(chain).remove();
    }

    @Test
    void shouldListDeptIdsByRoleId() {
        when(baseMapper.selectDeptIdByRoleId(ROLE_ID)).thenReturn(List.of(1L, 2L));

        assertEquals(List.of(1L, 2L), roleDeptService.listDeptIdByRoleId(ROLE_ID));
    }

    @SuppressWarnings("unchecked")
    private void stubExistingDeptIds(List<Long> deptIds) {
        LambdaQueryChainWrapper<RoleDeptDO> chain = mock(LambdaQueryChainWrapper.class);
        when(baseMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.select(any(SFunction.class))).thenReturn(chain);
        lenient().when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        when(chain.list()).thenReturn(deptIds.stream().map(deptId -> {
            RoleDeptDO rd = new RoleDeptDO();
            rd.setDeptId(deptId);
            return rd;
        }).toList());
    }
}
