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

package top.continew.admin.controller.common;

import com.alicp.jetcache.anno.CachePenetrationProtect;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.model.resp.dashboard.*;
import top.continew.admin.system.service.DashboardService;
import top.continew.starter.core.util.validate.ValidationUtils;
import top.continew.starter.log.core.annotation.Log;

import java.util.List;

/**
 * 仪表盘 API
 *
 * @author Charles7c
 * @since 2023/1/22 21:48
 */
@Tag(name = "仪表盘 API")
@Log(ignore = true)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "查询公告列表", description = "查询公告列表")
    @GetMapping("/notice")
    public List<DashboardNoticeResp> listNotice() {
        return dashboardService.listNotice();
    }

    @Operation(summary = "查询PV总览", description = "查询PV总览")
    @GetMapping("/analysis/overview/pv")
    public DashboardOverviewCommonResp getOverviewPv() {
        return dashboardService.getOverviewPv();
    }

    @Operation(summary = "查询IP总览", description = "查询IP总览")
    @GetMapping("/analysis/overview/ip")
    public DashboardOverviewCommonResp getOverviewIp() {
        return dashboardService.getOverviewIp();
    }

    @Operation(summary = "查询访问趋势信息", description = "查询访问趋势信息")
    @Parameter(name = "days", description = "日期数", example = "30", in = ParameterIn.PATH)
    @GetMapping("/access/trend/{days}")
    @CachePenetrationProtect
    @CacheRefresh(refresh = 7200)
    @Cached(key = "#days", name = CacheConstants.DASHBOARD_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public List<DashboardAccessTrendResp> listAccessTrend(@PathVariable Integer days) {
        ValidationUtils.throwIf(7 != days && 30 != days, "仅支持查询近 7/30 天访问趋势信息");
        return dashboardService.listAccessTrend(days);
    }

    @Operation(summary = "查询访问时段分析", description = "查询访问时段分析")
    @GetMapping("/analysis/timeslot")
    @CachePenetrationProtect
    @CacheRefresh(refresh = 7200)
    @Cached(key = "'TIMESLOT'", name = CacheConstants.DASHBOARD_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public List<DashboardChartCommonResp> getAnalysisTimeslot() {
        return dashboardService.getAnalysisTimeslot();
    }

    @Operation(summary = "查询地域分析", description = "查询地域分析")
    @GetMapping("/analysis/geo")
    @CachePenetrationProtect
    @CacheRefresh(refresh = 7200)
    @Cached(key = "'GEO'", name = CacheConstants.DASHBOARD_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public List<DashboardChartCommonResp> getAnalysisGeo() {
        return dashboardService.getAnalysisGeo();
    }

    @Operation(summary = "查询模块分析", description = "查询模块分析")
    @GetMapping("/analysis/module")
    @CachePenetrationProtect
    @CacheRefresh(refresh = 7200)
    @Cached(key = "'MODULE'", name = CacheConstants.DASHBOARD_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public List<DashboardChartCommonResp> getAnalysisModule() {
        return dashboardService.getAnalysisModule();
    }

    @Operation(summary = "查询终端分析", description = "查询终端分析")
    @GetMapping("/analysis/os")
    @CachePenetrationProtect
    @CacheRefresh(refresh = 7200)
    @Cached(key = "'OS'", name = CacheConstants.DASHBOARD_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public List<DashboardChartCommonResp> getAnalysisOs() {
        return dashboardService.getAnalysisOs();
    }

    @Operation(summary = "查询浏览器分析", description = "查询浏览器分析")
    @GetMapping("/analysis/browser")
    @CachePenetrationProtect
    @CacheRefresh(refresh = 7200)
    @Cached(key = "'BROWSER'", name = CacheConstants.DASHBOARD_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public List<DashboardChartCommonResp> getAnalysisBrowser() {
        return dashboardService.getAnalysisBrowser();
    }
}
