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
import cn.hutool.core.date.*;
import cn.hutool.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.model.resp.dashboard.DashboardAccessTrendResp;
import top.continew.admin.system.model.resp.dashboard.DashboardChartCommonResp;
import top.continew.admin.system.model.resp.dashboard.DashboardNoticeResp;
import top.continew.admin.system.model.resp.dashboard.DashboardOverviewCommonResp;
import top.continew.admin.system.service.DashboardService;
import top.continew.admin.system.service.NoticeService;

import java.math.BigDecimal;
import java.util.*;

/**
 * 仪表盘业务实现
 *
 * @author Charles7c
 * @since 2023/9/8 21:32
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

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
        List<DashboardChartCommonResp> dataList = logMapper.selectListDashboardAnalysisPv(last12MonthList);
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
        List<DashboardChartCommonResp> dataList = logMapper.selectListDashboardAnalysisIp(last12MonthList);
        if (dataList.size() < 12) {
            // 填充缺失的数据
            this.fillMissingDateData(last12MonthList, dataList);
        }
        resp.setDataList(dataList);
        return resp;
    }

    @Override
    public List<DashboardAccessTrendResp> listAccessTrend(Integer days) {
        DateTime currentDate = DateUtil.date();
        Date startTime = DateUtil.beginOfDay(DateUtil.offsetDay(currentDate, -days)).toJdkDate();
        Date endTime = DateUtil.endOfDay(DateUtil.offsetDay(currentDate, -1)).toJdkDate();
        List<DashboardAccessTrendResp> list = logMapper.selectListDashboardAccessTrend(startTime, endTime);
        if (list.size() < days) {
            List<String> all = DateUtil.rangeToList(startTime, endTime, DateField.DAY_OF_MONTH)
                .stream()
                .map(date -> date.toString(DatePattern.NORM_DATE_FORMAT))
                .toList();
            Collection<String> missings = CollUtil.disjunction(all, list.stream()
                .map(DashboardAccessTrendResp::getDate)
                .toList());
            list.addAll(missings.stream().map(missing -> new DashboardAccessTrendResp(missing, 0L, 0L)).toList());
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
    public List<DashboardChartCommonResp> getAnalysisGeo() {
        List<DashboardChartCommonResp> list = logMapper.selectListDashboardAnalysisGeo(9);
        return this.buildOtherPieChartData(list);
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
            : NumberUtil.round(NumberUtil.mul(NumberUtil.div(NumberUtil.sub(today, yesterday), yesterday), 100), 1);
    }

    /**
     * 构建其他饼图数据
     *
     * @param list 饼图数据列表
     * @return 饼图数据列表
     */
    private List<DashboardChartCommonResp> buildOtherPieChartData(List<DashboardChartCommonResp> list) {
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
        Collection<String> missings = CollUtil.disjunction(all, list.stream()
            .map(DashboardChartCommonResp::getName)
            .toList());
        list.addAll(missings.stream().map(missing -> new DashboardChartCommonResp(missing, 0L)).toList());
        list.sort(Comparator.comparing(DashboardChartCommonResp::getName));
    }

    /**
     * 获取最近12个月的月份列表
     *
     * @return 月份列表
     */
    private List<String> getLast12Months() {
        DateTime currentMonth = DateUtil.beginOfMonth(DateUtil.date());
        return DateUtil.rangeToList(DateUtil.offsetMonth(currentMonth, -12), DateUtil
            .offsetMonth(currentMonth, -1), DateField.MONTH)
            .stream()
            .map(dateTime -> DateUtil.format(dateTime, DatePattern.NORM_MONTH_FORMAT))
            .toList();
    }
}
