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

package top.charles7c.continew.admin.monitor.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import top.charles7c.continew.admin.monitor.model.entity.LogDO;
import top.charles7c.continew.admin.monitor.model.resp.DashboardAccessTrendResp;
import top.charles7c.continew.admin.monitor.model.resp.DashboardPopularModuleResp;
import top.charles7c.continew.admin.monitor.model.resp.DashboardTotalResp;
import top.charles7c.continew.starter.data.mybatis.plus.base.BaseMapper;

/**
 * 系统日志 Mapper
 *
 * @author Charles7c
 * @since 2022/12/22 21:47
 */
public interface LogMapper extends BaseMapper<LogDO> {

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
     *
     * @return 仪表盘访问趋势信息
     */
    List<DashboardAccessTrendResp> selectListDashboardAccessTrend(@Param("days") Integer days);

    /**
     * 查询仪表盘热门模块列表
     *
     * @return 仪表盘热门模块列表
     */
    List<DashboardPopularModuleResp> selectListDashboardPopularModule();

    /**
     * 查询仪表盘访客地域分布信息
     *
     * @return 仪表盘访客地域分布信息
     */
    List<Map<String, Object>> selectListDashboardGeoDistribution();
}
