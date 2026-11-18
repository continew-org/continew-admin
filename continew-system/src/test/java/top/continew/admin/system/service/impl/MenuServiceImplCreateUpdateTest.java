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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.system.constant.SystemConstants;
import top.continew.admin.system.enums.MenuTypeEnum;
import top.continew.admin.system.mapper.MenuMapper;
import top.continew.admin.system.model.entity.MenuDO;
import top.continew.admin.system.model.query.MenuQuery;
import top.continew.admin.system.model.req.MenuReq;
import top.continew.admin.system.service.RoleService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import jakarta.validation.Validator;
import java.util.Collection;
import java.util.List;

/**
 * 菜单业务实现单元测试。
 *
 * <p>覆盖新增与更新的重复校验（标题/组件名，按钮类型跳过组件名校验）、目录类型
 * 组件缺省、类型不可变更、级联删除、角色菜单查询（超管全量）与租户排除菜单计算。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class MenuServiceImplCreateUpdateTest {

    private static final long MENU_ID = 30L;

    private MenuMapper menuMapper;
    private RoleService roleService;
    private MenuServiceImpl menuService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<CacheUtils> cacheUtilsMock;
    private MockedStatic<TenantContextHolder> tenantContextHolderMock;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),
            MenuDO.class.getName()), MenuDO.class);
        // 校验工具的校验器实例取自 Spring 上下文，单测环境以恒通过的 mock 供给
        Validator validator = mock(Validator.class);
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class)).thenReturn(validator);
        cacheUtilsMock = mockStatic(CacheUtils.class);
        tenantContextHolderMock = mockStatic(TenantContextHolder.class);
        menuMapper = mock(MenuMapper.class);
        roleService = mock(RoleService.class);
        // CRUD 钩子（getById/create/update 的持久化层）以 spy 短路，仅验证业务编排
        menuService = spy(new MenuServiceImpl(new TenantExtensionProperties()));
        ReflectionTestUtils.setField(menuService, "roleService", roleService);
        ReflectionTestUtils.setField(menuService, "baseMapper", menuMapper);
    }

    @AfterEach
    void tearDown() {
        tenantContextHolderMock.close();
        cacheUtilsMock.close();
        springUtilMock.close();
        TenantContextHolder.clear();
    }

    @Test
    void shouldDefaultComponentToLayoutForDir() {
        stubRepeatQueries(false);
        // 实体类缓存预置，绕开父类从 Mapper 代理元数据推导实体类型
        ReflectionTestUtils.setField(menuService, "entityClass", MenuDO.class);

        menuService.create(req(MenuTypeEnum.DIR, null));

        cacheUtilsMock.verify(() -> CacheUtils.evictAll(CacheConstants.ROLE_MENU_KEY_PREFIX));
    }

    @Test
    void shouldRejectDuplicateTitleOnCreate() {
        stubRepeatQueries(true, false);
        MenuReq createReq = req(MenuTypeEnum.MENU, "ComponentA");

        BusinessException exception = assertThrows(BusinessException.class, () -> menuService
            .create(createReq));

        assertEquals("标题为 [测试菜单] 的菜单已存在", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateComponentNameForNonButton() {
        stubRepeatQueries(false, true);
        MenuReq createReq = req(MenuTypeEnum.MENU, "ComponentA");

        BusinessException exception = assertThrows(BusinessException.class, () -> menuService
            .create(createReq));

        assertEquals("组件名称为 [ComponentA] 的菜单已存在", exception.getMessage());
    }

    @Test
    void shouldSkipComponentNameCheckForButton() {
        stubRepeatQueries(false);
        ReflectionTestUtils.setField(menuService, "entityClass", MenuDO.class);

        menuService.create(req(MenuTypeEnum.BUTTON, null));

        // 按钮类型无组件概念，组件名称查重不触发（lambdaQuery 仅被标题查重消费一次）
        verify(menuMapper, times(1)).lambdaQuery();
    }

    @Test
    void shouldRejectTypeChangeOnUpdate() {
        stubRepeatQueries(false);
        MenuDO oldMenu = menu(MenuTypeEnum.DIR);
        doReturn(oldMenu).when(menuService).getById(MENU_ID);
        MenuReq updateReq = req(MenuTypeEnum.MENU, "ComponentA");

        BusinessException exception = assertThrows(BusinessException.class, () -> menuService
            .update(updateReq, MENU_ID));

        assertEquals("不允许修改菜单类型", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateTitleOnUpdate() {
        stubRepeatQueries(true, false);
        MenuDO oldMenu = menu(MenuTypeEnum.MENU);
        doReturn(oldMenu).when(menuService).getById(MENU_ID);
        MenuReq updateReq = req(MenuTypeEnum.MENU, "ComponentA");

        BusinessException exception = assertThrows(BusinessException.class, () -> menuService
            .update(updateReq, MENU_ID));

        assertEquals("标题为 [测试菜单] 的菜单已存在", exception.getMessage());
    }

    @Test
    void shouldCascadeDeleteChildren() {
        // 首轮查到子菜单 40，递归第二轮无子级
        MenuDO child = menu(MenuTypeEnum.MENU);
        child.setId(40L);
        stubChildQuery(List.of(child), List.of());

        menuService.delete(List.of(MENU_ID));

        verify(menuMapper).deleteByIds(List.of(MENU_ID, 40L));
        cacheUtilsMock.verify(() -> CacheUtils.evictAll(CacheConstants.ROLE_MENU_KEY_PREFIX));
    }

    @Test
    void shouldDeleteWithoutChildrenWhenLeaf() {
        stubChildQuery(List.of(), List.of());

        menuService.delete(List.of(MENU_ID));

        verify(menuMapper).deleteByIds(List.of(MENU_ID));
    }

    @Test
    void shouldListAllMenusForSuperAdminRole() {
        doReturn(List.of()).when(menuService).list(any(MenuQuery.class), isNull());

        menuService.listByRoleId(SystemConstants.SUPER_ADMIN_ROLE_ID);

        verify(menuMapper, never()).selectListByRoleId(SystemConstants.SUPER_ADMIN_ROLE_ID);
    }

    @Test
    void shouldExcludeTenantAdminMenus() {
        MenuDO allMenuA = menu(MenuTypeEnum.MENU);
        allMenuA.setId(11L);
        MenuDO allMenuB = menu(MenuTypeEnum.MENU);
        allMenuB.setId(12L);
        doReturn(List.of(allMenuA, allMenuB)).when(menuService).list();
        when(roleService.getIdByCode(RoleCodeEnum.TENANT_ADMIN.getCode())).thenReturn(3L);
        MenuDO tenantMenu = menu(MenuTypeEnum.MENU);
        tenantMenu.setId(11L);
        when(menuMapper.selectListByRoleId(3L)).thenReturn(List.of(tenantMenu));

        List<Long> excluded = menuService.listExcludeTenantMenu();

        // 对称差：全量菜单中租户管理员没有的那部分
        assertEquals(List.of(12L), excluded);
    }

    private MenuReq req(MenuTypeEnum type, String name) {
        MenuReq req = new MenuReq();
        req.setTitle("测试菜单");
        req.setType(type);
        req.setParentId(0L);
        req.setName(name);
        return req;
    }

    private MenuDO menu(MenuTypeEnum type) {
        MenuDO menu = new MenuDO();
        menu.setId(MENU_ID);
        menu.setType(type);
        return menu;
    }

    private void stubRepeatQueries(boolean exists) {
        stubRepeatQueries(exists, exists);
    }

    private void stubRepeatQueries(boolean titleExists, boolean nameExists) {
        LambdaQueryChainWrapper<MenuDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when(menuMapper).lambdaQuery();
        doReturn(chain).when(chain).eq(any(SFunction.class), any());
        lenient().doReturn(chain).when(chain)
            .ne(anyBoolean(), any(SFunction.class), any());
        lenient().doReturn(chain).when(chain).ne(any(SFunction.class), any());
        // 标题查重与组件名查重依次消费 exists()
        when(chain.exists()).thenReturn(titleExists, nameExists);
    }

    private void stubChildQuery(List<MenuDO> firstRound, List<MenuDO> secondRound) {
        LambdaQueryChainWrapper<MenuDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when(menuMapper).lambdaQuery();
        doReturn(chain).when(chain).select(any(SFunction.class));
        doReturn(chain).when(chain).in(any(SFunction.class), any(Collection.class));
        when(chain.list()).thenReturn(firstRound, secondRound);
    }
}
