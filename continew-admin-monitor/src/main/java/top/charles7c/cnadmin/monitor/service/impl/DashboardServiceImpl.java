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

package top.charles7c.cnadmin.monitor.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;

import top.charles7c.cnadmin.monitor.model.vo.DashboardGeoDistributionVO;
import top.charles7c.cnadmin.monitor.model.vo.DashboardPopularModuleVO;
import top.charles7c.cnadmin.monitor.model.vo.DashboardTotalVO;
import top.charles7c.cnadmin.monitor.service.DashboardService;
import top.charles7c.cnadmin.monitor.service.LogService;
import top.charles7c.cnadmin.system.model.vo.DashboardAnnouncementVO;
import top.charles7c.cnadmin.system.service.AnnouncementService;

/**
 * 仪表盘业务实现
 *
 * @author Charles7c
 * @since 2023/9/8 21:32
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final LogService logService;
    private final AnnouncementService announcementService;

    @Override
    public DashboardTotalVO getTotal() {
        DashboardTotalVO totalVO = logService.getDashboardTotal();
        Long todayPvCount = totalVO.getTodayPvCount();
        Long yesterdayPvCount = totalVO.getYesterdayPvCount();
        BigDecimal newPvCountFromYesterday = NumberUtil.sub(todayPvCount, yesterdayPvCount);
        BigDecimal newPvFromYesterday = (0 == yesterdayPvCount) ? BigDecimal.valueOf(100)
            : NumberUtil.round(NumberUtil.mul(NumberUtil.div(newPvCountFromYesterday, yesterdayPvCount), 100), 1);
        totalVO.setNewPvFromYesterday(newPvFromYesterday);
        return totalVO;
    }

    @Override
    public List<DashboardPopularModuleVO> listPopularModule() {
        List<DashboardPopularModuleVO> popularModuleList = logService.listDashboardPopularModule();
        for (DashboardPopularModuleVO popularModule : popularModuleList) {
            Long todayPvCount = popularModule.getTodayPvCount();
            Long yesterdayPvCount = popularModule.getYesterdayPvCount();
            BigDecimal newPvCountFromYesterday = NumberUtil.sub(todayPvCount, yesterdayPvCount);
            BigDecimal newPvFromYesterday = (0 == yesterdayPvCount) ? BigDecimal.valueOf(100)
                : NumberUtil.round(NumberUtil.mul(NumberUtil.div(newPvCountFromYesterday, yesterdayPvCount), 100), 1);
            popularModule.setNewPvFromYesterday(newPvFromYesterday);
        }
        return popularModuleList;
    }

    @Override
    public DashboardGeoDistributionVO getGeoDistribution() {
        List<Map<String, Object>> locationIpStatistics = logService.listDashboardGeoDistribution();
        DashboardGeoDistributionVO geoDistribution = new DashboardGeoDistributionVO();
        geoDistribution.setLocationIpStatistics(locationIpStatistics);
        geoDistribution.setLocations(
            locationIpStatistics.stream().map(m -> Convert.toStr(m.get("name"))).collect(Collectors.toList()));
        return geoDistribution;
    }

    @Override
    public List<DashboardAnnouncementVO> listAnnouncement() {
        return announcementService.listDashboard();
    }
}
