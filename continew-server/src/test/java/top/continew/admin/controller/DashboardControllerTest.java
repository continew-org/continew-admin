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

package top.continew.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import top.continew.admin.system.model.resp.dashboard.DashboardAccessTrendResp;
import top.continew.admin.system.model.resp.dashboard.DashboardChartCommonResp;
import top.continew.admin.system.model.resp.dashboard.DashboardNoticeResp;
import top.continew.admin.system.model.resp.dashboard.DashboardOverviewCommonResp;
import top.continew.admin.system.service.DashboardService;
import top.continew.starter.core.exception.BadRequestException;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.List;

/**
 * 仪表盘 API 委托单元测试。
 *
 * <p>覆盖各查询端点到仪表盘服务的委托传递与访问趋势的天数入参约束（仅允许 7/30）。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController controller;

    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        // 访问趋势接口经 ValidationUtils 校验入参，其静态块经 SpringUtil 取 Bean 校验器
        springUtilMock = Mockito.mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldDelegateListNotice() {
        List<DashboardNoticeResp> notices = List.of(new DashboardNoticeResp());
        when(dashboardService.listNotice()).thenReturn(notices);

        assertSame(notices, controller.listNotice());
    }

    @Test
    void shouldDelegateOverviewPv() {
        DashboardOverviewCommonResp overview = new DashboardOverviewCommonResp();
        when(dashboardService.getOverviewPv()).thenReturn(overview);

        assertSame(overview, controller.getOverviewPv());
    }

    @Test
    void shouldDelegateOverviewIp() {
        DashboardOverviewCommonResp overview = new DashboardOverviewCommonResp();
        when(dashboardService.getOverviewIp()).thenReturn(overview);

        assertSame(overview, controller.getOverviewIp());
    }

    @Test
    void shouldDelegateAnalysisGeo() throws IOException {
        List<DashboardChartCommonResp> chart = List.of(new DashboardChartCommonResp());
        when(dashboardService.getAnalysisGeo()).thenReturn(chart);

        assertSame(chart, controller.getAnalysisGeo());
    }

    @Test
    void shouldDelegateAccessTrendWhenDaysAllowed() {
        List<DashboardAccessTrendResp> trend = List.of(new DashboardAccessTrendResp());
        when(dashboardService.listAccessTrend(30)).thenReturn(trend);

        assertSame(trend, controller.listAccessTrend(30));
        verify(dashboardService).listAccessTrend(30);
    }

    @Test
    void shouldRejectAccessTrendWithUnsupportedDays() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> controller.listAccessTrend(15));

        assertEquals("仅支持查询近 7/30 天访问趋势信息", exception.getMessage());
    }

    @Test
    void shouldDelegateAnalysisTimeslot() {
        List<DashboardChartCommonResp> chart = List.of(new DashboardChartCommonResp());
        when(dashboardService.getAnalysisTimeslot()).thenReturn(chart);

        assertSame(chart, controller.getAnalysisTimeslot());
    }

    @Test
    void shouldDelegateAnalysisModule() {
        List<DashboardChartCommonResp> chart = List.of(new DashboardChartCommonResp());
        when(dashboardService.getAnalysisModule()).thenReturn(chart);

        assertSame(chart, controller.getAnalysisModule());
    }

    @Test
    void shouldDelegateAnalysisOs() {
        List<DashboardChartCommonResp> chart = List.of(new DashboardChartCommonResp());
        when(dashboardService.getAnalysisOs()).thenReturn(chart);

        assertSame(chart, controller.getAnalysisOs());
    }

    @Test
    void shouldDelegateAnalysisBrowser() {
        List<DashboardChartCommonResp> chart = List.of(new DashboardChartCommonResp());
        when(dashboardService.getAnalysisBrowser()).thenReturn(chart);

        assertSame(chart, controller.getAnalysisBrowser());
    }
}
