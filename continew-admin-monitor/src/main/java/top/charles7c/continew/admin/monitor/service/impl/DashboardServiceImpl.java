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

package top.charles7c.continew.admin.monitor.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;

import top.charles7c.continew.admin.common.constant.CacheConstants;
import top.charles7c.continew.admin.monitor.model.resp.DashboardAccessTrendResp;
import top.charles7c.continew.admin.monitor.model.resp.DashboardGeoDistributionResp;
import top.charles7c.continew.admin.monitor.model.resp.DashboardPopularModuleResp;
import top.charles7c.continew.admin.monitor.model.resp.DashboardTotalResp;
import top.charles7c.continew.admin.monitor.service.DashboardService;
import top.charles7c.continew.admin.monitor.service.LogService;
import top.charles7c.continew.admin.system.model.resp.DashboardAnnouncementResp;
import top.charles7c.continew.admin.system.service.AnnouncementService;

/**
 * 仪表盘业务实现
 *
 * @author Charles7c
 * @since 2023/9/8 21:32
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheConstants.DASHBOARD_KEY_PREFIX)
public class DashboardServiceImpl implements DashboardService {

    private final LogService logService;
    private final AnnouncementService announcementService;

    @Override
    public DashboardTotalResp getTotal() {
        DashboardTotalResp totalResp = logService.getDashboardTotal();
        Long todayPvCount = totalResp.getTodayPvCount();
        Long yesterdayPvCount = totalResp.getYesterdayPvCount();
        BigDecimal newPvCountFromYesterday = NumberUtil.sub(todayPvCount, yesterdayPvCount);
        BigDecimal newPvFromYesterday = (0 == yesterdayPvCount)
            ? BigDecimal.valueOf(100)
            : NumberUtil.round(NumberUtil.mul(NumberUtil.div(newPvCountFromYesterday, yesterdayPvCount), 100), 1);
        totalResp.setNewPvFromYesterday(newPvFromYesterday);
        return totalResp;
    }

    @Override
    @Cacheable(key = "#days")
    public List<DashboardAccessTrendResp> listAccessTrend(Integer days) {
        return logService.listDashboardAccessTrend(days);
    }

    @Override
    public List<DashboardPopularModuleResp> listPopularModule() {
        List<DashboardPopularModuleResp> popularModuleList = logService.listDashboardPopularModule();
        for (DashboardPopularModuleResp popularModule : popularModuleList) {
            Long todayPvCount = popularModule.getTodayPvCount();
            Long yesterdayPvCount = popularModule.getYesterdayPvCount();
            BigDecimal newPvCountFromYesterday = NumberUtil.sub(todayPvCount, yesterdayPvCount);
            BigDecimal newPvFromYesterday = (0 == yesterdayPvCount)
                ? BigDecimal.valueOf(100)
                : NumberUtil.round(NumberUtil.mul(NumberUtil.div(newPvCountFromYesterday, yesterdayPvCount), 100), 1);
            popularModule.setNewPvFromYesterday(newPvFromYesterday);
        }
        return popularModuleList;
    }

    @Override
    public DashboardGeoDistributionResp getGeoDistribution() {
        List<Map<String, Object>> locationIpStatistics = logService.listDashboardGeoDistribution();
        DashboardGeoDistributionResp geoDistribution = new DashboardGeoDistributionResp();
        geoDistribution.setLocationIpStatistics(locationIpStatistics);
        geoDistribution.setLocations(locationIpStatistics.stream().map(m -> Convert.toStr(m.get("name"))).toList());
        return geoDistribution;
    }

    @Override
    public List<DashboardAnnouncementResp> listAnnouncement() {
        return announcementService.listDashboard();
    }
}
