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

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import top.continew.admin.system.mapper.LogMapper;
import top.continew.admin.system.model.entity.LogDO;
import top.continew.admin.system.model.query.LogQuery;
import top.continew.admin.system.model.resp.DashboardAccessTrendResp;
import top.continew.admin.system.model.resp.DashboardPopularModuleResp;
import top.continew.admin.system.model.resp.DashboardTotalResp;
import top.continew.admin.system.model.resp.log.LogDetailResp;
import top.continew.admin.system.model.resp.log.LogResp;
import top.continew.admin.system.model.resp.log.LoginLogExportResp;
import top.continew.admin.system.model.resp.log.OperationLogExportResp;
import top.continew.admin.system.service.LogService;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.file.excel.util.ExcelUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 系统日志业务实现
 *
 * @author Charles7c
 * @since 2022/12/23 20:12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogMapper baseMapper;

    @Override
    public PageResp<LogResp> page(LogQuery query, PageQuery pageQuery) {
        QueryWrapper<LogDO> queryWrapper = this.buildQueryWrapper(query);
        IPage<LogResp> page = baseMapper.selectLogPage(pageQuery.toPage(), queryWrapper);
        return PageResp.build(page);
    }

    @Override
    @AutoOperate(type = LogDetailResp.class)
    public LogDetailResp get(Long id) {
        LogDO logDO = baseMapper.selectById(id);
        CheckUtils.throwIfNotExists(logDO, "LogDO", "ID", id);
        return BeanUtil.copyProperties(logDO, LogDetailResp.class);
    }

    @Override
    public void exportLoginLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response) {
        List<LoginLogExportResp> list = BeanUtil.copyToList(this.list(query, sortQuery), LoginLogExportResp.class);
        ExcelUtils.export(list, "导出登录日志数据", LoginLogExportResp.class, response);
    }

    @Override
    public void exportOperationLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response) {
        List<OperationLogExportResp> list = BeanUtil.copyToList(this
            .list(query, sortQuery), OperationLogExportResp.class);
        ExcelUtils.export(list, "导出操作日志数据", OperationLogExportResp.class, response);
    }

    @Override
    public DashboardTotalResp getDashboardTotal() {
        return baseMapper.selectDashboardTotal();
    }

    @Override
    public List<DashboardAccessTrendResp> listDashboardAccessTrend(Integer days) {
        return baseMapper.selectListDashboardAccessTrend(days);
    }

    @Override
    public List<DashboardPopularModuleResp> listDashboardPopularModule() {
        return baseMapper.selectListDashboardPopularModule();
    }

    @Override
    public List<Map<String, Object>> listDashboardGeoDistribution() {
        return baseMapper.selectListDashboardGeoDistribution();
    }

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 列表信息
     */
    private List<LogResp> list(LogQuery query, SortQuery sortQuery) {
        QueryWrapper<LogDO> queryWrapper = this.buildQueryWrapper(query);
        this.sort(queryWrapper, sortQuery);
        return baseMapper.selectLogList(queryWrapper);
    }

    /**
     * 设置排序
     *
     * @param queryWrapper 查询条件封装对象
     * @param sortQuery    排序查询条件
     */
    private void sort(QueryWrapper<LogDO> queryWrapper, SortQuery sortQuery) {
        Sort sort = Opt.ofNullable(sortQuery).orElseGet(SortQuery::new).getSort();
        for (Sort.Order order : sort) {
            if (null != order) {
                String property = order.getProperty();
                queryWrapper.orderBy(true, order.isAscending(), CharSequenceUtil.toUnderlineCase(property));
            }
        }
    }

    /**
     * 构建 QueryWrapper
     *
     * @param query 查询条件
     * @return QueryWrapper
     */
    private QueryWrapper<LogDO> buildQueryWrapper(LogQuery query) {
        String description = query.getDescription();
        String module = query.getModule();
        String ip = query.getIp();
        String createUserString = query.getCreateUserString();
        Integer status = query.getStatus();
        List<Date> createTimeList = query.getCreateTime();
        return new QueryWrapper<LogDO>().and(StrUtil.isNotBlank(description), q -> q.like("t1.description", description)
            .or()
            .like("t1.module", description))
            .eq(StrUtil.isNotBlank(module), "t1.module", module)
            .and(StrUtil.isNotBlank(ip), q -> q.like("t1.ip", ip).or().like("t1.address", ip))
            .and(StrUtil.isNotBlank(createUserString), q -> q.like("t2.username", createUserString)
                .or()
                .like("t2.nickname", createUserString))
            .eq(null != status, "t1.status", status)
            .between(CollUtil.isNotEmpty(createTimeList), "t1.create_time", CollUtil.getFirst(createTimeList), CollUtil
                .getLast(createTimeList));
    }
}
