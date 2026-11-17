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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.common.enums.DataScopeEnum;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.common.util.TransactionUtils;
import top.continew.admin.system.constant.SystemConstants;
import top.continew.admin.system.mapper.RoleMapper;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.req.RoleReq;
import top.continew.admin.system.service.MenuService;
import top.continew.admin.system.service.RoleDeptService;
import top.continew.admin.system.service.RoleMenuService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import jakarta.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 角色业务实现单元测试。
 *
 * <p>覆盖新增与更新的重复校验、超管编码保护、系统内置角色数据权限保护、
 * 删除守卫（内置角色/用户关联）、分配守卫与超管全量权限语义。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class RoleServiceImplGuardTest {

    private static final long ROLE_ID = 3L;

    private RoleMapper roleMapper;
    private UserRoleService userRoleService;
    private MenuService menuService;
    private RoleServiceImpl roleService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<CacheUtils> cacheUtilsMock;
    private MockedStatic<TransactionUtils> transactionUtilsMock;
    private MockedStatic<TenantContextHolder> tenantContextHolderMock;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),
            RoleDO.class.getName()), RoleDO.class);
        // 校验工具的校验器实例取自 Spring 上下文，单测环境以恒通过的 mock 供给
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(mock(Validator.class));
        cacheUtilsMock = mockStatic(CacheUtils.class);
        transactionUtilsMock = mockStatic(TransactionUtils.class);
        tenantContextHolderMock = mockStatic(TenantContextHolder.class);
        // getSuperRoleCodes 需判定默认租户，供给默认配置属性（默认租户 ID=0）
        springUtilMock.when(() -> SpringUtil.getBean(TenantExtensionProperties.class))
            .thenReturn(new TenantExtensionProperties());
        roleMapper = mock(RoleMapper.class);
        userRoleService = mock(UserRoleService.class);
        menuService = mock(MenuService.class);
        roleService = Mockito.spy(new RoleServiceImpl(mock(RoleMenuService.class), mock(
            RoleDeptService.class)));
        ReflectionTestUtils.setField(roleService, "userRoleService", userRoleService);
        ReflectionTestUtils.setField(roleService, "menuService", menuService);
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
        // 父类 getById 经实体类缓存推导实体类型，mock Mapper 缺少代理元数据，预置绕开
        ReflectionTestUtils.setField(roleService, "entityClass", RoleDO.class);
    }

    @AfterEach
    void tearDown() {
        tenantContextHolderMock.close();
        transactionUtilsMock.close();
        cacheUtilsMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldRejectSuperAdminCodeOnCreate() {
        stubRepeatQueries(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleService
            .create(req(RoleCodeEnum.SUPER_ADMIN.getCode())));

        assertEquals("编码 [super_admin] 禁止使用", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateNameOnCreate() {
        stubRepeatQueries(true, false);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleService
            .create(req("custom")));

        assertEquals("名称为 [测试角色] 的角色已存在", exception.getMessage());
    }

    @Test
    void shouldRejectCodeChangeOnUpdate() {
        stubRepeatQueries(false);
        doReturn(role("custom", false, DataScopeEnum.DEPT)).when(roleService).getById(ROLE_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleService
            .update(req("changed"), ROLE_ID));

        assertEquals("角色编码不允许修改", exception.getMessage());
    }

    @Test
    void shouldRejectDataScopeChangeForSystemRole() {
        stubRepeatQueries(false);
        doReturn(role("custom", true, DataScopeEnum.DEPT)).when(roleService).getById(ROLE_ID);
        RoleReq req = req("custom");
        req.setDataScope(DataScopeEnum.ALL);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleService
            .update(req, ROLE_ID));

        assertEquals("[测试角色] 是系统内置角色，不允许修改角色数据权限", exception.getMessage());
    }

    @Test
    void shouldSkipDeptSyncForSuperRoleCode() {
        stubRepeatQueries(false);
        // 默认租户下超管族仅超级管理员编码，其部门关联由平台统一管理，更新流程跳过部门同步
        doReturn(role(RoleCodeEnum.SUPER_ADMIN.getCode(), true, DataScopeEnum.DEPT))
            .when(roleService)
            .getById(ROLE_ID);

        roleService.update(req(RoleCodeEnum.SUPER_ADMIN.getCode()), ROLE_ID);

        verify(roleDeptService(), never()).add(any(), eq(ROLE_ID));
    }

    @Test
    void shouldRejectDeleteOfSystemRole() {
        stubRoleQuery(List.of(role("测试角色", true, DataScopeEnum.DEPT)));

        BusinessException exception = assertThrows(BusinessException.class, () -> roleService
            .beforeDelete(List.of(ROLE_ID)));

        assertEquals("所选角色 [测试角色] 是系统内置角色，不允许删除", exception.getMessage());
    }

    @Test
    void shouldRejectDeleteWhenUsersAssigned() {
        stubRoleQuery(List.of(role("测试角色", false, DataScopeEnum.DEPT)));
        when(userRoleService.isRoleIdExists(List.of(ROLE_ID))).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleService
            .beforeDelete(List.of(ROLE_ID)));

        assertEquals("所选角色存在用户关联，请解除关联后重试", exception.getMessage());
    }

    @Test
    void shouldRejectAssignToUsersForSystemRole() {
        doReturn(role("测试角色", true, DataScopeEnum.DEPT)).when(roleService).getById(ROLE_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleService
            .assignToUsers(ROLE_ID, List.of(9L)));

        assertEquals("[测试角色] 是系统内置角色，不允许分配角色给其他用户", exception.getMessage());
    }

    @Test
    void shouldGrantAllPermissionForSuperAdmin() {
        when(userRoleService.listRoleIdByUserId(9L)).thenReturn(List.of(ROLE_ID));
        stubRoleCodeQuery(List.of(role(RoleCodeEnum.SUPER_ADMIN.getCode(), false, null)));

        Set<String> permissions = roleService.listPermissionByUserId(9L);

        assertEquals(Set.of(SystemConstants.ALL_PERMISSION), permissions);
        verify(menuService, never()).listPermissionByUserId(9L);
    }

    @Test
    void shouldQueryMenuPermissionForNormalRole() {
        when(userRoleService.listRoleIdByUserId(9L)).thenReturn(List.of(ROLE_ID));
        stubRoleCodeQuery(List.of(role("custom", false, null)));
        when(menuService.listPermissionByUserId(9L)).thenReturn(Set.of("system:user:list"));

        Set<String> permissions = roleService.listPermissionByUserId(9L);

        assertEquals(Set.of("system:user:list"), permissions);
    }

    @Test
    void shouldReturnEmptyRoleCodesWhenUserHasNoRole() {
        when(userRoleService.listRoleIdByUserId(9L)).thenReturn(List.of());

        assertEquals(Set.of(), roleService.listCodeByUserId(9L));
    }

    @Test
    void shouldReturnEmptyRolesWhenUserHasNoRole() {
        when(userRoleService.listRoleIdByUserId(9L)).thenReturn(List.of());

        assertEquals(Set.of(), roleService.listByUserId(9L));
    }

    @Test
    void shouldReturnNullWhenRoleCodeNotFound() {
        stubIdQuery(java.util.Optional.empty());

        assertNull(roleService.getIdByCode("not-exist"));
    }

    @Test
    void shouldAssignToUsersAndRefreshContext() {
        doReturn(role("custom", false, DataScopeEnum.DEPT)).when(roleService).getById(ROLE_ID);

        roleService.assignToUsers(ROLE_ID, List.of(9L));

        verify(userRoleService).assignRoleToUsers(ROLE_ID, List.of(9L));
    }

    private RoleReq req(String code) {
        RoleReq req = new RoleReq();
        req.setName("测试角色");
        req.setCode(code);
        req.setDataScope(DataScopeEnum.DEPT);
        req.setDeptIds(List.of(1L));
        return req;
    }

    private RoleDO role(String code, boolean isSystem, DataScopeEnum dataScope) {
        RoleDO role = new RoleDO();
        role.setId(ROLE_ID);
        role.setCode(code);
        role.setName("测试角色");
        role.setIsSystem(isSystem);
        role.setDataScope(dataScope);
        return role;
    }

    private top.continew.admin.system.service.RoleDeptService roleDeptService() {
        return (top.continew.admin.system.service.RoleDeptService) ReflectionTestUtils.getField(
            roleService, "roleDeptService");
    }

    private void stubRepeatQueries(boolean exists) {
        stubRepeatQueries(exists, exists);
    }

    private void stubRepeatQueries(boolean nameExists, boolean codeExists) {
        LambdaQueryChainWrapper<RoleDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when(roleMapper).lambdaQuery();
        doReturn(chain).when(chain).eq(any(SFunction.class), any());
        Mockito.lenient().doReturn(chain).when(chain)
            .ne(Mockito.anyBoolean(), any(SFunction.class), any());
        // 名称查重与编码查重依次消费 exists()
        when(chain.exists()).thenReturn(nameExists, codeExists);
    }

    private void stubRoleQuery(List<RoleDO> roles) {
        LambdaQueryChainWrapper<RoleDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when(roleMapper).lambdaQuery();
        Mockito.lenient().doReturn(chain).when(chain)
            .select(Mockito.<SFunction<RoleDO, ?>>any(), Mockito.<SFunction<RoleDO, ?>>any());
        doReturn(chain).when(chain).in(any(SFunction.class), any(Collection.class));
        when(chain.list()).thenReturn(roles);
    }

    private void stubRoleCodeQuery(List<RoleDO> roles) {
        LambdaQueryChainWrapper<RoleDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when(roleMapper).lambdaQuery();
        doReturn(chain).when(chain).select(any(SFunction.class));
        doReturn(chain).when(chain).in(any(SFunction.class), any(Collection.class));
        when(chain.list()).thenReturn(roles);
    }

    private void stubIdQuery(java.util.Optional<RoleDO> result) {
        LambdaQueryChainWrapper<RoleDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when(roleMapper).lambdaQuery();
        doReturn(chain).when(chain).eq(any(SFunction.class), any());
        when(chain.oneOpt()).thenReturn(result);
    }
}
