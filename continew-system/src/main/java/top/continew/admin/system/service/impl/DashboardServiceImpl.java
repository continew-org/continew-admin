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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.model.resp.dashboard.DashboardAccessTrendResp;
import top.continew.admin.system.model.resp.dashboard.DashboardChartCommonResp;
import top.continew.admin.system.model.resp.dashboard.DashboardNoticeResp;
import top.continew.admin.system.model.resp.dashboard.DashboardOverviewCommonResp;
import top.continew.admin.system.service.DashboardService;
import top.continew.admin.system.service.NoticeService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.CollUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * 仪表盘业务实现
 *
 * @author Charles7c
 * @since 2023/9/8 21:32
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /**
     * 月份格式（yyyy-MM）
     */
    private static final DateTimeFormatter MONTH_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM");

    private final LogMapper logMapper;
    private final NoticeService noticeService;

    @Override
    public List<DashboardNoticeResp> listNotice() {
        return noticeService.listDashboard();
    }

    @Override
    public DashboardOverviewCommonResp getOverviewPv() {
        DashboardOverviewCommonResp resp = logMapper.selectDashboardOverviewPv();
        resp.setGrowth(this.calcGrowthFromYesterday(resp.getToday(), resp.getYesterday()));
        List<String> last12MonthList = this.getLast12Months();
        List<DashboardChartCommonResp> dataList =
            logMapper.selectListDashboardAnalysisPv(last12MonthList);
        if (dataList.size() < 12) {
            // 填充缺失的数据
            this.fillMissingDateData(last12MonthList, dataList);
        }
        resp.setDataList(dataList);
        return resp;
    }

    @Override
    public DashboardOverviewCommonResp getOverviewIp() {
        DashboardOverviewCommonResp resp = logMapper.selectDashboardOverviewIp();
        resp.setGrowth(this.calcGrowthFromYesterday(resp.getToday(), resp.getYesterday()));
        List<String> last12MonthList = this.getLast12Months();
        List<DashboardChartCommonResp> dataList =
            logMapper.selectListDashboardAnalysisIp(last12MonthList);
        if (dataList.size() < 12) {
            // 填充缺失的数据
            this.fillMissingDateData(last12MonthList, dataList);
        }
        resp.setDataList(dataList);
        return resp;
    }

    @Override
    public List<DashboardChartCommonResp> getAnalysisGeo() throws IOException {
        List<DashboardChartCommonResp> originList = logMapper.selectListDashboardAnalysisGeo();
        List<DashboardChartCommonResp> list = new ArrayList<>(34);
        // 获取省份数据
        String chinaJson = IoUtil.readUtf8(new ClassPathResource("china.json").getInputStream());
        JSONArray jsonArr = JSONUtil.parseObj(chinaJson).getJSONArray("children");
        List<String> provinceList = CollUtils.mapToList(jsonArr, item -> {
            JSONObject itemJsonObj = JSONUtil.parseObj(item);
            return "%s:%s".formatted(itemJsonObj.getStr("name"), itemJsonObj.getStr("fullname"));
        });
        // 汇总各省份访问数据
        for (String province : provinceList) {
            String[] split = province.split(StringConstants.COLON);
            String name = split[0];
            String fullName = split[1];
            long sum = originList.stream()
                .filter(item -> item.getName().contains(name))
                .mapToLong(DashboardChartCommonResp::getValue)
                .sum();
            list.add(new DashboardChartCommonResp(fullName, sum));
        }
        return list;
    }

    @Override
    public List<DashboardAccessTrendResp> listAccessTrend(Integer days) {
        // 按业务时区确定自然日边界，避免 JVM 默认时区与业务时区不一致时落入错误的日期分桶
        ZoneId zoneId = GlobalConstants.DEFAULT_ZONE_ID;
        LocalDate today = LocalDate.now(zoneId);
        LocalDate startDate = today.minusDays(days);
        Date startTime = Date.from(startDate.atStartOfDay(zoneId).toInstant());
        Date endTime = Date.from(today.atStartOfDay(zoneId).toInstant().minusMillis(1));
        List<DashboardAccessTrendResp> list =
            logMapper.selectListDashboardAccessTrend(startTime, endTime);
        if (list.size() < days) {
            List<String> all = Stream
                .iterate(startDate, date -> date.isBefore(today), date -> date.plusDays(1))
                .map(DateTimeFormatter.ISO_LOCAL_DATE::format)
                .toList();
            Collection<String> missings = CollUtil.disjunction(all, CollUtils
                .mapToList(list, DashboardAccessTrendResp::getDate));
            list.addAll(CollUtils.mapToList(missings,
                missing -> new DashboardAccessTrendResp(missing, 0L, 0L)));
            list.sort(Comparator.comparing(DashboardAccessTrendResp::getDate));
        }
        return list;
    }

    @Override
    public List<DashboardChartCommonResp> getAnalysisTimeslot() {
        List<DashboardChartCommonResp> list = logMapper.selectListDashboardAnalysisTimeslot();
        if (list.size() < 12) {
            // 获取所有时间段
            List<String> allTimeSlotList = new ArrayList<>(12);
            for (int hour = 0; hour < 24; hour += 2) {
                allTimeSlotList.add(String.format("%02d:00", hour));
            }
            // 填充缺失的数据
            this.fillMissingDateData(allTimeSlotList, list);
        }
        return list;
    }

    @Override
    public List<DashboardChartCommonResp> getAnalysisModule() {
        return logMapper.selectListDashboardAnalysisModule(10);
    }

    @Override
    public List<DashboardChartCommonResp> getAnalysisOs() {
        List<DashboardChartCommonResp> list = logMapper.selectListDashboardAnalysisOs(4);
        return this.buildOtherPieChartData(list);
    }

    @Override
    public List<DashboardChartCommonResp> getAnalysisBrowser() {
        List<DashboardChartCommonResp> list = logMapper.selectListDashboardAnalysisBrowser(4);
        return this.buildOtherPieChartData(list);
    }

    /**
     * 计算增长百分比
     *
     * @param today     今日数量
     * @param yesterday 昨日数量
     * @return 增长百分比
     */
    private BigDecimal calcGrowthFromYesterday(Long today, Long yesterday) {
        return (0 == yesterday)
            ? BigDecimal.valueOf(100)
            : NumberUtil.round(
                NumberUtil.mul(NumberUtil.div(NumberUtil.sub(today, yesterday), yesterday), 100),
                1);
    }

    /**
     * 构建其他饼图数据
     *
     * @param list 饼图数据列表
     * @return 饼图数据列表
     */
    private List<DashboardChartCommonResp> buildOtherPieChartData(
        List<DashboardChartCommonResp> list) {
        Long totalCount = logMapper.selectTotalCount();
        long sumCount = list.stream().mapToLong(DashboardChartCommonResp::getValue).sum();
        if (sumCount < totalCount) {
            list.add(new DashboardChartCommonResp("其他", totalCount - sumCount));
        }
        return list;
    }

    /**
     * 填充缺失时间段的数据
     *
     * @param all  所有时间段
     * @param list 待填充数据
     */
    private void fillMissingDateData(List<String> all, List<DashboardChartCommonResp> list) {
        Collection<String> missings = CollUtil.disjunction(all, CollUtils
            .mapToList(list, DashboardChartCommonResp::getName));
        list.addAll(
            CollUtils.mapToList(missings, missing -> new DashboardChartCommonResp(missing, 0L)));
        list.sort(Comparator.comparing(DashboardChartCommonResp::getName));
    }

    /**
     * 获取最近12个月的月份列表
     *
     * @return 月份列表
     */
    private List<String> getLast12Months() {
        // 与访问趋势一致，按业务时区确定当前月份
        YearMonth currentMonth = YearMonth.now(GlobalConstants.DEFAULT_ZONE_ID);
        return Stream
            .iterate(currentMonth.minusMonths(12), month -> month.isBefore(currentMonth),
                month -> month.plusMonths(1))
            .map(MONTH_FORMATTER::format)
            .toList();
    }
}
