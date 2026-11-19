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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import top.continew.admin.system.enums.LogStatusEnum;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.model.entity.LogDO;
import top.continew.admin.system.model.query.LogQuery;
import top.continew.admin.system.model.resp.log.LogDetailResp;
import top.continew.admin.system.model.resp.log.LogResp;
import top.continew.admin.system.model.resp.log.LoginLogExportResp;
import top.continew.admin.system.model.resp.log.OperationLogExportResp;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.excel.util.ExcelUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统日志业务单元测试。
 *
 * <p>覆盖分页查询的动态条件组装与排序传参、详情查询的存在性校验与字段拷贝、
 * 登录/操作日志导出对 ExcelUtils 的委托契约。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class LogServiceImplTest {

    private static final long LOG_ID = 1L;

    private LogMapper logMapper;
    private LogServiceImpl logService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<ExcelUtils> excelUtilsMock;

    @BeforeEach
    void setUp() {
        // CheckUtils 类惰性初始化时经 SpringUtil 取校验器，须在首次调用前供给
        springUtilMock = mockStatic(SpringUtil.class);
        Validator validator = mock(Validator.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(validator);
        excelUtilsMock = mockStatic(ExcelUtils.class);
        logMapper = mock(LogMapper.class);
        logService = new LogServiceImpl(logMapper);
    }

    @AfterEach
    void tearDown() {
        excelUtilsMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldAssembleDynamicConditionsOnPage() {
        LogQuery query = new LogQuery();
        query.setDescription("新增");
        query.setModule("部门管理");
        query.setIp("192.168.1.1");
        query.setCreateUserString("admin");
        query.setStatus(LogStatusEnum.FAILURE);
        LocalDateTime begin = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 2, 23, 59, 59);
        query.setCreateTime(List.of(begin, end));
        IPage<LogResp> page = new Page<>(1, 10);
        when(logMapper.selectLogPage(any(), any())).thenReturn(page);

        logService.page(query, new PageQuery(1, 10));

        ArgumentCaptor<QueryWrapper<LogDO>> captor = forWrapper();
        verify(logMapper).selectLogPage(any(), captor.capture());
        String segment = captor.getValue().getSqlSegment();
        assertTrue(segment.contains("t1.description LIKE"), () -> "缺少描述模糊条件：" + segment);
        assertTrue(segment.contains("OR t1.module LIKE"), () -> "缺少模块模糊条件：" + segment);
        assertTrue(segment.contains("t1.module ="), () -> "缺少模块等值条件：" + segment);
        assertTrue(segment.contains("t1.ip LIKE"), () -> "缺少 IP 模糊条件：" + segment);
        assertTrue(segment.contains("OR t1.address LIKE"), () -> "缺少归属地模糊条件：" + segment);
        assertTrue(segment.contains("t2.username LIKE"), () -> "缺少操作人模糊条件：" + segment);
        assertTrue(segment.contains("OR t2.nickname LIKE"), () -> "缺少操作人昵称模糊条件：" + segment);
        assertTrue(segment.contains("t1.status ="), () -> "缺少状态等值条件：" + segment);
        assertTrue(segment.contains("t1.create_time BETWEEN"), () -> "缺少操作时间范围条件：" + segment);
        Map<String, Object> params = captor.getValue().getParamNameValuePairs();
        // like 条件由 QueryWrapper 自动包裹通配符
        assertEquals("%新增%", params.get("MPGENVAL1"));
        assertEquals("部门管理", params.get("MPGENVAL3"));
        assertEquals(LogStatusEnum.FAILURE, params.get("MPGENVAL8"));
        assertEquals(begin, params.get("MPGENVAL9"));
        assertEquals(end, params.get("MPGENVAL10"));
    }

    @Test
    void shouldPassSortedWrapperOnPage() {
        PageQuery pageQuery = new PageQuery(1, 10);
        pageQuery.setSort(new String[] {"createTime", "desc"});
        IPage<LogResp> page = new Page<>(1, 10);
        when(logMapper.selectLogPage(any(), any())).thenReturn(page);

        logService.page(new LogQuery(), pageQuery);

        ArgumentCaptor<QueryWrapper<LogDO>> captor = forWrapper();
        verify(logMapper).selectLogPage(any(), captor.capture());
        assertTrue(captor.getValue().getSqlSegment().endsWith("ORDER BY create_time DESC"),
            () -> "排序未生效：" + captor.getValue().getSqlSegment());
    }

    @Test
    void shouldBuildEmptyWrapperWhenQueryBlank() {
        IPage<LogResp> page = new Page<>(1, 10);
        when(logMapper.selectLogPage(any(), any())).thenReturn(page);

        PageResp<LogResp> resp = logService.page(new LogQuery(), new PageQuery(2, 5));

        ArgumentCaptor<QueryWrapper<LogDO>> captor = forWrapper();
        verify(logMapper).selectLogPage(any(), captor.capture());
        assertEquals("", captor.getValue().getSqlSegment(), "无查询条件时不应拼出 WHERE 片段");
        assertEquals(page.getTotal(), resp.getTotal());
        assertEquals(List.of(), resp.getList());
    }

    @Test
    void shouldRejectGetWhenLogAbsent() {
        when(logMapper.selectById(LOG_ID)).thenReturn(null);

        BusinessException exception =
            assertThrows(BusinessException.class, () -> logService.get(LOG_ID));

        assertEquals("ID 为 [1] 的 Log 记录已不存在", exception.getMessage());
    }

    @Test
    void shouldCopyEntityFieldsOnGet() {
        LogDO entity = new LogDO();
        entity.setId(LOG_ID);
        entity.setDescription("新增数据");
        entity.setModule("部门管理");
        entity.setRequestUrl("http://api.continew.top/system/dept");
        entity.setRequestMethod("POST");
        entity.setStatusCode(200);
        entity.setStatus(LogStatusEnum.SUCCESS);
        entity.setIp("192.168.1.1");
        entity.setCreateTime(LocalDateTime.of(2026, 1, 1, 8, 0));
        when(logMapper.selectById(LOG_ID)).thenReturn(entity);

        LogDetailResp resp = logService.get(LOG_ID);

        assertEquals(LOG_ID, resp.getId());
        assertEquals("新增数据", resp.getDescription());
        assertEquals("部门管理", resp.getModule());
        assertEquals("POST", resp.getRequestMethod());
        assertEquals(200, resp.getStatusCode());
        assertEquals(LogStatusEnum.SUCCESS, resp.getStatus());
        assertEquals("192.168.1.1", resp.getIp());
        assertEquals(entity.getCreateTime(), resp.getCreateTime());
    }

    @Test
    void shouldExportLoginLogWithFixedFileName() {
        stubListForExport("账号登录", LogStatusEnum.SUCCESS);

        HttpServletResponse response = mock(HttpServletResponse.class);
        logService.exportLoginLog(new LogQuery(), new SortQuery(), response);

        ArgumentCaptor<List<LoginLogExportResp>> listCaptor = forList();
        excelUtilsMock.verify(() -> ExcelUtils.export(listCaptor.capture(), eq("导出登录日志数据"), eq(
            LoginLogExportResp.class), eq(response)));
        assertEquals(1, listCaptor.getValue().size());
        assertEquals("账号登录", listCaptor.getValue().get(0).getDescription());
        assertEquals(LogStatusEnum.SUCCESS, listCaptor.getValue().get(0).getStatus());
    }

    @Test
    void shouldExportOperationLogWithFixedFileName() {
        stubListForExport("新增部门", LogStatusEnum.FAILURE);

        HttpServletResponse response = mock(HttpServletResponse.class);
        logService.exportOperationLog(new LogQuery(), new SortQuery(), response);

        ArgumentCaptor<List<OperationLogExportResp>> listCaptor = forList();
        excelUtilsMock.verify(() -> ExcelUtils.export(listCaptor.capture(), eq("导出操作日志数据"), eq(
            OperationLogExportResp.class), eq(response)));
        assertEquals(1, listCaptor.getValue().size());
        assertEquals("新增部门", listCaptor.getValue().get(0).getDescription());
        assertEquals(LogStatusEnum.FAILURE, listCaptor.getValue().get(0).getStatus());
    }

    private void stubListForExport(String description, LogStatusEnum status) {
        LogResp row = new LogResp();
        row.setId(LOG_ID);
        row.setDescription(description);
        row.setStatus(status);
        when(logMapper.selectLogList(any())).thenReturn(List.of(row));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<QueryWrapper<LogDO>> forWrapper() {
        return ArgumentCaptor.forClass((Class) QueryWrapper.class);
    }

    @SuppressWarnings("unchecked")
    private <T> ArgumentCaptor<List<T>> forList() {
        return ArgumentCaptor.forClass((Class) List.class);
    }
}
