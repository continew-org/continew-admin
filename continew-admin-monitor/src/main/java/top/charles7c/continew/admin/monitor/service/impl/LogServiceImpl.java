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

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.charles7c.continew.admin.common.constant.SysConstants;
import top.charles7c.continew.admin.monitor.mapper.LogMapper;
import top.charles7c.continew.admin.monitor.model.entity.LogDO;
import top.charles7c.continew.admin.monitor.model.query.LoginLogQuery;
import top.charles7c.continew.admin.monitor.model.query.OperationLogQuery;
import top.charles7c.continew.admin.monitor.model.query.SystemLogQuery;
import top.charles7c.continew.admin.monitor.model.resp.*;
import top.charles7c.continew.admin.monitor.service.LogService;
import top.charles7c.continew.starter.core.util.ReflectUtils;
import top.charles7c.continew.starter.core.util.validate.CheckUtils;
import top.charles7c.continew.starter.data.mybatis.plus.query.QueryHelper;
import top.charles7c.continew.starter.extension.crud.model.query.PageQuery;
import top.charles7c.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final LogMapper logMapper;

    @Override
    @AutoOperate(type = OperationLogResp.class, on = "list")
    public PageResp<OperationLogResp> page(OperationLogQuery query, PageQuery pageQuery) {
        QueryWrapper<LogDO> queryWrapper = QueryHelper.build(query);
        // 限定查询信息
        List<String> fieldNameList = ReflectUtils.getNonStaticFieldsName(OperationLogResp.class);
        List<String> columnNameList = fieldNameList.stream()
            .filter(n -> !n.endsWith(SysConstants.DESCRIPTION_FIELD_SUFFIX))
            .map(StrUtil::toUnderlineCase)
            .collect(Collectors.toList());
        queryWrapper.select(columnNameList);
        // 分页查询
        IPage<LogDO> page = logMapper.selectPage(pageQuery.toPage(), queryWrapper);
        return PageResp.build(page, OperationLogResp.class);
    }

    @Override
    @AutoOperate(type = LoginLogResp.class, on = "list")
    public PageResp<LoginLogResp> page(LoginLogQuery query, PageQuery pageQuery) {
        QueryWrapper<LogDO> queryWrapper = QueryHelper.build(query);
        queryWrapper.eq("module", "登录");
        // 限定查询信息
        List<String> fieldNameList = ReflectUtils.getNonStaticFieldsName(LoginLogResp.class);
        List<String> columnNameList = fieldNameList.stream()
            .filter(n -> !n.endsWith(SysConstants.DESCRIPTION_FIELD_SUFFIX))
            .map(StrUtil::toUnderlineCase)
            .collect(Collectors.toList());
        queryWrapper.select(columnNameList);
        // 分页查询
        IPage<LogDO> page = logMapper.selectPage(pageQuery.toPage(), queryWrapper);
        return PageResp.build(page, LoginLogResp.class);
    }

    @Override
    @AutoOperate(type = SystemLogResp.class, on = "list")
    public PageResp<SystemLogResp> page(SystemLogQuery query, PageQuery pageQuery) {
        QueryWrapper<LogDO> queryWrapper = QueryHelper.build(query);
        // 限定查询信息
        List<String> fieldNameList = ReflectUtils.getNonStaticFieldsName(SystemLogResp.class);
        List<String> columnNameList = fieldNameList.stream()
            .filter(n -> !n.endsWith(SysConstants.DESCRIPTION_FIELD_SUFFIX))
            .map(StrUtil::toUnderlineCase)
            .collect(Collectors.toList());
        queryWrapper.select(columnNameList);
        // 分页查询
        IPage<LogDO> page = logMapper.selectPage(pageQuery.toPage(), queryWrapper);
        return PageResp.build(page, SystemLogResp.class);
    }

    @Override
    @AutoOperate(type = SystemLogDetailResp.class)
    public SystemLogDetailResp get(Long id) {
        LogDO logDO = logMapper.selectById(id);
        CheckUtils.throwIfNotExists(logDO, "LogDO", "ID", id);
        return BeanUtil.copyProperties(logDO, SystemLogDetailResp.class);
    }

    @Override
    public DashboardTotalResp getDashboardTotal() {
        return logMapper.selectDashboardTotal();
    }

    @Override
    public List<DashboardAccessTrendResp> listDashboardAccessTrend(Integer days) {
        return logMapper.selectListDashboardAccessTrend(days);
    }

    @Override
    public List<DashboardPopularModuleResp> listDashboardPopularModule() {
        return logMapper.selectListDashboardPopularModule();
    }

    @Override
    public List<Map<String, Object>> listDashboardGeoDistribution() {
        return logMapper.selectListDashboardGeoDistribution();
    }
}
