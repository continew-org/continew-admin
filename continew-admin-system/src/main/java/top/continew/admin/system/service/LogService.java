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

package top.continew.admin.system.service;

import jakarta.servlet.http.HttpServletResponse;
import top.continew.admin.system.model.query.LogQuery;
import top.continew.admin.system.model.resp.dashboard.DashboardAccessTrendResp;
import top.continew.admin.system.model.resp.dashboard.DashboardChartCommonResp;
import top.continew.admin.system.model.resp.dashboard.DashboardTotalResp;
import top.continew.admin.system.model.resp.log.LogDetailResp;
import top.continew.admin.system.model.resp.log.LogResp;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 系统日志业务接口
 *
 * @author Charles7c
 * @since 2022/12/23 20:12
 */
public interface LogService {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResp<LogResp> page(LogQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    LogDetailResp get(Long id);

    /**
     * 导出登录日志
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void exportLoginLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response);

    /**
     * 导出操作日志
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void exportOperationLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response);

    /**
     * 查询仪表盘总计信息
     *
     * @return 仪表盘总计信息
     */
    DashboardTotalResp getDashboardTotal();

    /**
     * 查询仪表盘访问趋势信息
     *
     * @param days 日期数
     * @return 仪表盘访问趋势信息
     */
    List<DashboardAccessTrendResp> listDashboardAccessTrend(Integer days);

    /**
     * 查询仪表盘访问时段分析信息
     *
     * @return 仪表盘访问时段分析信息
     */
    List<DashboardChartCommonResp> listDashboardAnalysisTimeslot();

    /**
     * 查询仪表盘地域分析信息
     * 
     * @param top 显示数量
     * @return 仪表盘地域分析信息
     */
    List<DashboardChartCommonResp> listDashboardAnalysisGeo(int top);

    /**
     * 查询仪表盘模块分析信息
     * 
     * @param top 显示数量
     * @return 仪表盘模块分析信息
     */
    List<DashboardChartCommonResp> listDashboardAnalysisModule(int top);

    /**
     * 查询仪表盘终端分析信息
     *
     * @param top 显示数量
     * @return 仪表盘终端分析信息
     */
    List<DashboardChartCommonResp> listDashboardAnalysisOs(int top);

    /**
     * 查询仪表盘浏览器分析信息
     *
     * @param top 显示数量
     * @return 仪表盘浏览器分析信息
     */
    List<DashboardChartCommonResp> listDashboardAnalysisBrowser(int top);
}
