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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.mapper.DeptMapper;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.req.DeptReq;
import top.continew.admin.system.service.RoleDeptService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.sql.Connection;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

/**
 * 部门业务实现单元测试。
 *
 * <p>覆盖新增与更新的名称查重、新增时祖先链拼接、系统内置部门禁用与变更上级保护、
 * 禁用联动下级与启用联动上级校验、禁用后踢出在线用户，以及删除守卫（系统内置/
 * 下级存在/用户关联）与角色部门关联的级联清理。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class DeptServiceImplTest {

    @Mock
    private DeptMapper deptMapper;
    @Mock
    private RoleDeptService roleDeptService;
    @Mock
    private DataSource dataSource;
    @Mock
    private UserService userService;
    @Mock
    private OnlineUserService onlineUserService;

    @Spy
    @InjectMocks
    private DeptServiceImpl deptService;

    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(deptService, "baseMapper", deptMapper);
        // CheckUtils 的静态块经 SpringUtil 取 Bean 校验器，以本地 Validator 供给
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldAppendAncestorsFromParentOnCreate() {
        stubNameNotRepeat();
        DeptDO parent = new DeptDO();
        parent.setId(5L);
        parent.setAncestors("0");
        doReturn(parent).when(deptMapper).selectById(5L);
        DeptReq req = new DeptReq();
        req.setName("研发部");
        req.setParentId(5L);

        deptService.beforeCreate(req);

        assertEquals("0,5", req.getAncestors());
    }

    @Test
    void shouldRejectDuplicateNameOnCreate() {
        LambdaQueryChainWrapper<DeptDO> query = chainExists(true);
        doReturn(query).when((top.continew.starter.data.mapper.BaseMapper) deptMapper)
            .lambdaQuery();
        DeptReq req = new DeptReq();
        req.setName("研发部");
        req.setParentId(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> deptService
            .beforeCreate(req));
        assertEquals("名称为 [研发部] 的部门已存在", exception.getMessage());
    }

    @Test
    void shouldRejectDisableSystemDept() {
        DeptDO oldDept = dept(1L, "研发中心", true, DisEnableStatusEnum.ENABLE, 0L);
        stubOldDept(oldDept);
        stubNameNotRepeat();
        DeptReq req = deptReq("研发中心", 0L, DisEnableStatusEnum.DISABLE);

        BusinessException exception = assertThrows(BusinessException.class, () -> deptService
            .beforeUpdate(req, 1L));
        assertEquals("[研发中心] 是系统内置部门，不允许禁用", exception.getMessage());
    }

    @Test
    void shouldRejectParentChangeForSystemDept() {
        DeptDO oldDept = dept(1L, "研发中心", true, DisEnableStatusEnum.ENABLE, 0L);
        stubOldDept(oldDept);
        stubNameNotRepeat();
        DeptReq req = deptReq("研发中心", 2L, DisEnableStatusEnum.ENABLE);

        BusinessException exception = assertThrows(BusinessException.class, () -> deptService
            .beforeUpdate(req, 1L));
        assertEquals("[研发中心] 是系统内置部门，不允许变更上级部门", exception.getMessage());
    }

    @Test
    void shouldRejectDisableWithEnabledChildren() {
        DeptDO oldDept = dept(1L, "研发中心", false, DisEnableStatusEnum.ENABLE, 0L);
        stubOldDept(oldDept);
        stubNameNotRepeat();
        doReturn(List.of(childDept(DisEnableStatusEnum.ENABLE))).when(deptService)
            .listChildren(1L);
        DeptReq req = deptReq("研发中心", 0L, DisEnableStatusEnum.DISABLE);

        BusinessException exception = assertThrows(BusinessException.class, () -> deptService
            .beforeUpdate(req, 1L));
        assertEquals("禁用 [研发中心] 前，请先禁用其所有下级部门", exception.getMessage());
    }

    @Test
    void shouldRejectEnableWithDisabledParent() {
        DeptDO oldDept = dept(2L, "研发一部", false, DisEnableStatusEnum.DISABLE, 1L);
        stubOldDept(oldDept);
        stubNameNotRepeat();
        doReturn(List.of()).when(deptService).listChildren(2L);
        DeptDO parent = dept(1L, "研发中心", false, DisEnableStatusEnum.DISABLE, 0L);
        doReturn(parent).when(deptMapper).selectById(1L);
        DeptReq req = deptReq("研发一部", 1L, DisEnableStatusEnum.ENABLE);

        BusinessException exception = assertThrows(BusinessException.class, () -> deptService
            .beforeUpdate(req, 2L));
        assertEquals("启用 [研发一部] 前，请先启用其所有上级部门", exception.getMessage());
    }

    @Test
    void shouldKickOutOnlineUsersAfterDisable() {
        ReflectionTestUtils.setField(deptService, "userService", userService);
        ReflectionTestUtils.setField(deptService, "onlineUserService", onlineUserService);
        DeptDO entity = dept(3L, "市场部", false, DisEnableStatusEnum.ENABLE, 0L);
        entity.setId(3L);
        DeptReq req = deptReq("市场部", 0L, DisEnableStatusEnum.DISABLE);
        when(userService.listIdByDeptId(3L)).thenReturn(List.of(11L, 12L));

        deptService.afterUpdate(req, entity);

        verify(onlineUserService).kickOut(11L);
        verify(onlineUserService).kickOut(12L);
    }

    @Test
    void shouldNotKickOutUsersAfterEnable() {
        ReflectionTestUtils.setField(deptService, "userService", userService);
        ReflectionTestUtils.setField(deptService, "onlineUserService", onlineUserService);
        DeptDO entity = dept(3L, "市场部", false, DisEnableStatusEnum.ENABLE, 0L);
        entity.setId(3L);
        DeptReq req = deptReq("市场部", 0L, DisEnableStatusEnum.ENABLE);

        deptService.afterUpdate(req, entity);

        verifyNoInteractions(onlineUserService);
    }

    @Test
    void shouldRejectDeleteSystemDept() {
        ReflectionTestUtils.setField(deptService, "userService", userService);
        LambdaQueryChainWrapper<DeptDO> query = chainList(List.of(dept(1L, "研发中心", true,
            DisEnableStatusEnum.ENABLE, 0L)), 0L);
        doReturn(query).when((top.continew.starter.data.mapper.BaseMapper) deptMapper)
            .lambdaQuery();
        List<Long> ids = List.of(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> deptService
            .beforeDelete(ids));
        assertEquals("所选部门 [研发中心] 是系统内置部门，不允许删除", exception.getMessage());
        verifyNoInteractions(roleDeptService);
    }

    @Test
    void shouldRejectDeleteWithChildren() {
        ReflectionTestUtils.setField(deptService, "userService", userService);
        stubDatabaseTypeFallback();
        LambdaQueryChainWrapper<DeptDO> listQuery = chainList(List.of(), 2L);
        doReturn(listQuery).when((top.continew.starter.data.mapper.BaseMapper) deptMapper)
            .lambdaQuery();
        List<Long> ids = List.of(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> deptService
            .beforeDelete(ids));
        assertEquals("所选部门存在下级部门，不允许删除", exception.getMessage());
    }

    @Test
    void shouldRejectDeleteWithUsers() {
        ReflectionTestUtils.setField(deptService, "userService", userService);
        ReflectionTestUtils.setField(deptService, "onlineUserService", onlineUserService);
        stubDatabaseTypeFallback();
        LambdaQueryChainWrapper<DeptDO> listQuery = chainList(List.of(), 0L);
        doReturn(listQuery).when((top.continew.starter.data.mapper.BaseMapper) deptMapper)
            .lambdaQuery();
        when(userService.countByDeptIds(List.of(1L))).thenReturn(3L);
        List<Long> ids = List.of(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> deptService
            .beforeDelete(ids));
        assertEquals("所选部门存在用户关联，请解除关联后重试", exception.getMessage());
        verify(roleDeptService, never()).deleteByDeptIds(any());
    }

    @Test
    void shouldDeleteRoleDeptRelationAfterChecksPass() {
        ReflectionTestUtils.setField(deptService, "userService", userService);
        stubDatabaseTypeFallback();
        LambdaQueryChainWrapper<DeptDO> listQuery = chainList(List.of(), 0L);
        doReturn(listQuery).when((top.continew.starter.data.mapper.BaseMapper) deptMapper)
            .lambdaQuery();
        when(userService.countByDeptIds(List.of(1L))).thenReturn(0L);

        deptService.beforeDelete(List.of(1L));

        verify(roleDeptService).deleteByDeptIds(List.of(1L));
    }

    @Test
    void shouldReturnEmptyListForEmptyDeptNames() {
        assertEquals(List.of(), deptService.listByNames(List.of()));
        assertEquals(0, deptService.countByNames(Set.of()));
    }

    private DeptDO dept(Long id, String name, boolean isSystem, DisEnableStatusEnum status,
        Long parentId) {
        DeptDO dept = new DeptDO();
        dept.setId(id);
        dept.setName(name);
        dept.setIsSystem(isSystem);
        dept.setStatus(status);
        dept.setParentId(parentId);
        dept.setAncestors("0," + parentId);
        return dept;
    }

    private DeptDO childDept(DisEnableStatusEnum status) {
        DeptDO child = new DeptDO();
        child.setId(99L);
        child.setStatus(status);
        return child;
    }

    private DeptReq deptReq(String name, Long parentId, DisEnableStatusEnum status) {
        DeptReq req = new DeptReq();
        req.setName(name);
        req.setParentId(parentId);
        req.setStatus(status);
        return req;
    }

    private void stubOldDept(DeptDO oldDept) {
        doReturn(oldDept).when(deptService).getById(oldDept.getId());
    }

    private void stubNameNotRepeat() {
        LambdaQueryChainWrapper<DeptDO> query = chainExists(false);
        doReturn(query).when((top.continew.starter.data.mapper.BaseMapper) deptMapper)
            .lambdaQuery();
    }

    private void stubDatabaseTypeFallback() {
        // MetaUtils 经 DataSource 元数据识别数据库类型；返回不可识别的产品名使其回落默认 MySQL
        try {
            Connection connection = mock(Connection.class);
            java.sql.DatabaseMetaData metaData = mock(java.sql.DatabaseMetaData.class);
            doReturn(connection).when(dataSource).getConnection();
            doReturn("Unknown-Database").when(metaData).getDatabaseProductName();
            doReturn(metaData).when(connection).getMetaData();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<DeptDO> chainExists(boolean exists) {
        LambdaQueryChainWrapper<DeptDO> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(query).eq(any(), any());
        doReturn(query).when(query).ne(anyBoolean(), any(), any());
        when(query.exists()).thenReturn(exists);
        return query;
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<DeptDO> chainList(List<DeptDO> list, Long count) {
        LambdaQueryChainWrapper<DeptDO> query = mock(LambdaQueryChainWrapper.class);
        // 链上条件方法与终止方法按用例选择性触达，桩统一宽松处理避免逐用例裁剪
        lenient().doReturn(query).when(query).in(any(), anyCollection());
        lenient().doReturn(query).when(query).apply(anyString());
        lenient().doReturn(query).when(query).select(any(SFunction[].class));
        lenient().when(query.list()).thenReturn(list);
        lenient().when(query.count()).thenReturn(count);
        return query;
    }
}
