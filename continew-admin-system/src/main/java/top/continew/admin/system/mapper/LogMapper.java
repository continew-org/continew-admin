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

package top.continew.admin.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import top.continew.admin.system.model.entity.LogDO;
import top.continew.admin.system.model.resp.dashboard.DashboardAccessTrendResp;
import top.continew.admin.system.model.resp.dashboard.DashboardChartCommonResp;
import top.continew.admin.system.model.resp.dashboard.DashboardTotalResp;
import top.continew.admin.system.model.resp.log.LogResp;
import top.continew.starter.data.mp.base.BaseMapper;

import java.util.List;

/**
 * 系统日志 Mapper
 *
 * @author Charles7c
 * @since 2022/12/22 21:47
 */
public interface LogMapper extends BaseMapper<LogDO> {

    /**
     * 分页查询列表
     *
     * @param page         分页条件
     * @param queryWrapper 查询条件
     * @return 分页列表信息
     */
    IPage<LogResp> selectLogPage(@Param("page") IPage<LogDO> page,
                                 @Param(Constants.WRAPPER) QueryWrapper<LogDO> queryWrapper);

    /**
     * 查询列表
     *
     * @param queryWrapper 查询条件
     * @return 列表信息
     */
    List<LogResp> selectLogList(@Param(Constants.WRAPPER) QueryWrapper<LogDO> queryWrapper);

    /**
     * 查询仪表盘总计信息
     *
     * @return 仪表盘总计信息
     */
    DashboardTotalResp selectDashboardTotal();

    /**
     * 查询仪表盘访问趋势信息
     *
     * @param days 日期数
     * @return 仪表盘访问趋势信息
     */
    List<DashboardAccessTrendResp> selectListDashboardAccessTrend(@Param("days") Integer days);

    /**
     * 查询仪表盘访问时段分析信息
     *
     * @return 仪表盘访问时段分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisTimeslot();

    /**
     * 查询仪表盘地域分析信息
     *
     * @param top 显示数量
     * @return 仪表盘地域分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisGeo(@Param("top") Integer top);

    /**
     * 查询仪表盘模块分析信息
     *
     * @param top 显示数量
     * @return 仪表盘模块分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisModule(@Param("top") Integer top);

    /**
     * 查询仪表盘终端分析信息
     *
     * @param top 显示数量
     * @return 仪表盘终端分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisOs(@Param("top") Integer top);

    /**
     * 查询仪表盘浏览器分析信息
     *
     * @param top 显示数量
     * @return 仪表盘浏览器分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisBrowser(@Param("top") Integer top);
}
