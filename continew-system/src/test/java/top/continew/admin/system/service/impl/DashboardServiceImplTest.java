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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.core.io.ClassPathResource;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.model.resp.dashboard.DashboardAccessTrendResp;
import top.continew.admin.system.model.resp.dashboard.DashboardChartCommonResp;
import top.continew.admin.system.model.resp.dashboard.DashboardNoticeResp;
import top.continew.admin.system.model.resp.dashboard.DashboardOverviewCommonResp;
import top.continew.admin.system.service.NoticeService;
import top.continew.starter.cache.multicache.util.CacheUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 仪表盘业务实现单元测试。
 *
 * <p>覆盖总览增长率（昨日为零与非零）、月度/时段/访问趋势缺失补点、
 * 地域按省汇总、模块查询透传与饼图「其他」归并。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class DashboardServiceImplTest {

    private LogMapper logMapper;
    private NoticeService noticeService;
    private DashboardServiceImpl dashboardService;
    private MockedStatic<CacheUtils> cacheUtilsMock;

    @BeforeEach
    void setUp() {
        logMapper = mock(LogMapper.class);
        noticeService = mock(NoticeService.class);
        dashboardService = new DashboardServiceImpl(logMapper, noticeService);
        cacheUtilsMock = mockStatic(CacheUtils.class);
    }

    @AfterEach
    void tearDown() {
        cacheUtilsMock.close();
    }

    @Test
    void shouldDelegateListNotice() {
        List<DashboardNoticeResp> notices = List.of();
        when(noticeService.listDashboard()).thenReturn(notices);

        assertEquals(notices, dashboardService.listNotice());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFillGrowthAndMonthlyDataForPv() {
        DashboardOverviewCommonResp overview = new DashboardOverviewCommonResp();
        overview.setToday(150L);
        overview.setYesterday(100L);
        String firstMonth = YearMonth.now(GlobalConstants.DEFAULT_ZONE_ID).minusMonths(12)
            .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        when(logMapper.selectDashboardOverviewPv()).thenReturn(overview);
        when(logMapper.selectListDashboardAnalysisPv(anyList()))
            .thenReturn(new ArrayList<>(List.of(
                new DashboardChartCommonResp(firstMonth, 10L))));
        stubCacheLoader();

        DashboardOverviewCommonResp result = dashboardService.getOverviewPv();

        assertEquals(0, new BigDecimal("50.0").compareTo(result.getGrowth()));
        assertEquals(12, result.getDataList().size());
        cacheUtilsMock.verify(() -> CacheUtils.get(
            eq(CacheConstants.DASHBOARD_KEY_PREFIX + "PV:"), any(), any()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUseHundredGrowthWhenYesterdayIsZeroForIp() {
        DashboardOverviewCommonResp overview = new DashboardOverviewCommonResp();
        overview.setToday(5L);
        overview.setYesterday(0L);
        when(logMapper.selectDashboardOverviewIp()).thenReturn(overview);
        List<DashboardChartCommonResp> fullData = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            fullData.add(new DashboardChartCommonResp("2025-%02d".formatted(i + 1), (long) i));
        }
        when(logMapper.selectListDashboardAnalysisIp(anyList())).thenReturn(fullData);
        stubCacheLoader();

        DashboardOverviewCommonResp result = dashboardService.getOverviewIp();

        assertEquals(0, BigDecimal.valueOf(100).compareTo(result.getGrowth()));
        assertEquals(12, result.getDataList().size());
        cacheUtilsMock.verify(() -> CacheUtils.get(
            eq(CacheConstants.DASHBOARD_KEY_PREFIX + "IP:"), any(), any()));
    }

    @Test
    void shouldAggregateGeoByProvince() throws IOException {
        when(logMapper.selectListDashboardAnalysisGeo()).thenReturn(new ArrayList<>(List.of(
            new DashboardChartCommonResp("北京朝阳", 5L),
            new DashboardChartCommonResp("北京海淀", 3L),
            new DashboardChartCommonResp("上海浦东", 7L),
            new DashboardChartCommonResp("广东深圳", 100L))));
        String chinaJson = "{\"children\":[{\"name\":\"北京\",\"fullname\":\"北京市\"},"
            + "{\"name\":\"上海\",\"fullname\":\"上海市\"}]}";

        try (MockedConstruction<ClassPathResource> ignored = mockConstruction(
            ClassPathResource.class,
            (mock, context) -> when(mock.getInputStream()).thenReturn(
                new ByteArrayInputStream(chinaJson.getBytes(StandardCharsets.UTF_8))))) {
            List<DashboardChartCommonResp> result = dashboardService.getAnalysisGeo();

            assertEquals(2, result.size());
            assertEquals("北京市", result.get(0).getName());
            assertEquals(8L, result.get(0).getValue());
            assertEquals("上海市", result.get(1).getName());
            assertEquals(7L, result.get(1).getValue());
        }
    }

    @Test
    void shouldUseQueryRangeWithoutFillWhenAccessTrendComplete() {
        LocalDate today = LocalDate.now(GlobalConstants.DEFAULT_ZONE_ID);
        List<DashboardAccessTrendResp> full = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            full.add(new DashboardAccessTrendResp(today.minusDays(3 - i)
                .format(DateTimeFormatter.ISO_LOCAL_DATE), (long) i, (long) i));
        }
        when(logMapper.selectListDashboardAccessTrend(any(), any())).thenReturn(full);

        List<DashboardAccessTrendResp> result = dashboardService.listAccessTrend(3);

        assertEquals(3, result.size());
        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(logMapper).selectListDashboardAccessTrend(start.capture(), end.capture());
        assertEquals(today.atStartOfDay(), end.getValue());
        assertEquals(today.minusDays(3).atStartOfDay(), start.getValue());
    }

    @Test
    void shouldFillMissingDatesWhenAccessTrendIncomplete() {
        LocalDate today = LocalDate.now(GlobalConstants.DEFAULT_ZONE_ID);
        String d0 = today.minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String d1 = today.minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String d2 = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        when(logMapper.selectListDashboardAccessTrend(any(), any()))
            .thenReturn(new ArrayList<>(List.of(new DashboardAccessTrendResp(d1, 10L, 5L))));

        List<DashboardAccessTrendResp> result = dashboardService.listAccessTrend(3);

        assertEquals(3, result.size());
        assertEquals(d0, result.get(0).getDate());
        assertEquals(0L, result.get(0).getPvCount());
        assertEquals(d1, result.get(1).getDate());
        assertEquals(10L, result.get(1).getPvCount());
        assertEquals(d2, result.get(2).getDate());
        assertEquals(0L, result.get(2).getPvCount());
    }

    @Test
    void shouldFillMissingTimeslots() {
        when(logMapper.selectListDashboardAnalysisTimeslot())
            .thenReturn(new ArrayList<>(List.of(new DashboardChartCommonResp("00:00", 5L))));

        List<DashboardChartCommonResp> result = dashboardService.getAnalysisTimeslot();

        assertEquals(12, result.size());
        assertEquals("00:00", result.get(0).getName());
        assertEquals(5L, result.get(0).getValue());
        assertEquals("02:00", result.get(1).getName());
        assertEquals(0L, result.get(1).getValue());
    }

    @Test
    void shouldSkipFillWhenTimeslotsComplete() {
        List<DashboardChartCommonResp> full = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            full.add(new DashboardChartCommonResp("%02d:00".formatted(i * 2), (long) i));
        }
        when(logMapper.selectListDashboardAnalysisTimeslot()).thenReturn(full);

        assertEquals(12, dashboardService.getAnalysisTimeslot().size());
    }

    @Test
    void shouldPassTopToModuleAnalysis() {
        when(logMapper.selectListDashboardAnalysisModule(10)).thenReturn(List.of());

        dashboardService.getAnalysisModule();

        verify(logMapper).selectListDashboardAnalysisModule(10);
    }

    @Test
    void shouldAppendOtherForOs() {
        when(logMapper.selectListDashboardAnalysisOs(4)).thenReturn(new ArrayList<>(List.of(
            new DashboardChartCommonResp("Windows", 30L),
            new DashboardChartCommonResp("Mac", 20L))));
        when(logMapper.selectTotalCount()).thenReturn(100L);

        List<DashboardChartCommonResp> result = dashboardService.getAnalysisOs();

        assertEquals(3, result.size());
        assertEquals("其他", result.get(2).getName());
        assertEquals(50L, result.get(2).getValue());
    }

    @Test
    void shouldNotAppendOtherForOsWhenCovered() {
        when(logMapper.selectListDashboardAnalysisOs(4)).thenReturn(new ArrayList<>(List.of(
            new DashboardChartCommonResp("Windows", 60L),
            new DashboardChartCommonResp("Mac", 40L))));
        when(logMapper.selectTotalCount()).thenReturn(100L);

        List<DashboardChartCommonResp> result = dashboardService.getAnalysisOs();

        assertEquals(2, result.size());
    }

    @Test
    void shouldAppendOtherForBrowser() {
        when(logMapper.selectListDashboardAnalysisBrowser(4)).thenReturn(new ArrayList<>(List.of(
            new DashboardChartCommonResp("Chrome", 70L))));
        when(logMapper.selectTotalCount()).thenReturn(100L);

        List<DashboardChartCommonResp> result = dashboardService.getAnalysisBrowser();

        assertEquals(2, result.size());
        assertEquals("其他", result.get(1).getName());
        assertEquals(30L, result.get(1).getValue());
    }

    @SuppressWarnings("unchecked")
    private void stubCacheLoader() {
        cacheUtilsMock.when(() -> CacheUtils.get(anyString(), any(), any()))
            .thenAnswer(invocation -> {
                Callable<List<DashboardChartCommonResp>> loader = invocation.getArgument(2);
                return loader.call();
            });
    }
}
