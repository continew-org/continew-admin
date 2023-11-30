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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.continew.admin.common.constant.SysConstants;
import top.charles7c.continew.admin.monitor.mapper.LogMapper;
import top.charles7c.continew.admin.monitor.model.entity.LogDO;
import top.charles7c.continew.admin.monitor.model.query.LoginLogQuery;
import top.charles7c.continew.admin.monitor.model.query.OperationLogQuery;
import top.charles7c.continew.admin.monitor.model.query.SystemLogQuery;
import top.charles7c.continew.admin.monitor.model.resp.*;
import top.charles7c.continew.admin.monitor.service.LogService;
import top.charles7c.continew.starter.core.util.ExceptionUtils;
import top.charles7c.continew.starter.extension.crud.base.CommonUserService;
import top.charles7c.continew.starter.extension.crud.model.query.PageQuery;
import top.charles7c.continew.starter.extension.crud.model.resp.PageDataResp;
import top.charles7c.continew.starter.extension.crud.util.QueryHelper;
import top.charles7c.continew.starter.extension.crud.util.ReflectUtils;
import top.charles7c.continew.starter.extension.crud.util.validate.CheckUtils;

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
    private final CommonUserService commonUserService;

    @Async
    @EventListener
    public void save(LogDO logDO) {
        logMapper.insert(logDO);
    }

    @Override
    public PageDataResp<OperationLogResp> page(OperationLogQuery query, PageQuery pageQuery) {
        QueryWrapper<LogDO> queryWrapper = QueryHelper.build(query);

        // 限定查询信息
        List<String> fieldNameList = ReflectUtils.getNonStaticFieldsName(OperationLogResp.class);
        List<String> columnNameList =
            fieldNameList.stream().filter(n -> !n.endsWith(SysConstants.DESCRIPTION_FIELD_SUFFIX))
                .map(StrUtil::toUnderlineCase).collect(Collectors.toList());
        queryWrapper.select(columnNameList);

        // 分页查询
        IPage<LogDO> page = logMapper.selectPage(pageQuery.toPage(), queryWrapper);
        PageDataResp<OperationLogResp> pageDataResp = PageDataResp.build(page, OperationLogResp.class);

        // 填充数据（如果是查询个人操作日志，只查询一次用户信息即可）
        if (null != query.getUid()) {
            String nickname = ExceptionUtils.exToNull(() -> commonUserService.getNicknameById(query.getUid()));
            pageDataResp.getList().forEach(o -> o.setCreateUserString(nickname));
        } else {
            pageDataResp.getList().forEach(this::fill);
        }
        return pageDataResp;
    }

    @Override
    public PageDataResp<LoginLogResp> page(LoginLogQuery query, PageQuery pageQuery) {
        QueryWrapper<LogDO> queryWrapper = QueryHelper.build(query);
        queryWrapper.eq("module", "登录");

        // 限定查询信息
        List<String> fieldNameList = ReflectUtils.getNonStaticFieldsName(LoginLogResp.class);
        List<String> columnNameList =
            fieldNameList.stream().filter(n -> !n.endsWith(SysConstants.DESCRIPTION_FIELD_SUFFIX))
                .map(StrUtil::toUnderlineCase).collect(Collectors.toList());
        queryWrapper.select(columnNameList);

        // 分页查询
        IPage<LogDO> page = logMapper.selectPage(pageQuery.toPage(), queryWrapper);
        PageDataResp<LoginLogResp> pageDataResp = PageDataResp.build(page, LoginLogResp.class);

        // 填充数据
        pageDataResp.getList().forEach(this::fill);
        return pageDataResp;
    }

    @Override
    public PageDataResp<SystemLogResp> page(SystemLogQuery query, PageQuery pageQuery) {
        QueryWrapper<LogDO> queryWrapper = QueryHelper.build(query);

        // 限定查询信息
        List<String> fieldNameList = ReflectUtils.getNonStaticFieldsName(SystemLogResp.class);
        List<String> columnNameList =
            fieldNameList.stream().filter(n -> !n.endsWith(SysConstants.DESCRIPTION_FIELD_SUFFIX))
                .map(StrUtil::toUnderlineCase).collect(Collectors.toList());
        queryWrapper.select(columnNameList);

        // 分页查询
        IPage<LogDO> page = logMapper.selectPage(pageQuery.toPage(), queryWrapper);
        PageDataResp<SystemLogResp> pageDataResp = PageDataResp.build(page, SystemLogResp.class);

        // 填充数据
        pageDataResp.getList().forEach(this::fill);
        return pageDataResp;
    }

    @Override
    public SystemLogDetailResp get(Long id) {
        LogDO logDO = logMapper.selectById(id);
        CheckUtils.throwIfNotExists(logDO, "LogDO", "ID", id);
        SystemLogDetailResp detail = BeanUtil.copyProperties(logDO, SystemLogDetailResp.class);
        this.fill(detail);
        return detail;
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

    /**
     * 填充数据
     *
     * @param logResp
     *            日志信息
     */
    private void fill(LogResp logResp) {
        Long createUser = logResp.getCreateUser();
        if (null == createUser) {
            return;
        }
        logResp.setCreateUserString(ExceptionUtils.exToNull(() -> commonUserService.getNicknameById(createUser)));
    }
}
