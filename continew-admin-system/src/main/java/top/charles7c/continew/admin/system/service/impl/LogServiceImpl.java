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

package top.charles7c.continew.admin.system.service.impl;

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.charles7c.continew.admin.system.mapper.LogMapper;
import top.charles7c.continew.admin.system.model.entity.LogDO;
import top.charles7c.continew.admin.system.model.query.LogQuery;
import top.charles7c.continew.admin.system.model.resp.*;
import top.charles7c.continew.admin.system.service.LogService;
import top.charles7c.continew.starter.core.util.validate.CheckUtils;
import top.charles7c.continew.starter.core.util.validate.ValidationUtils;
import top.charles7c.continew.starter.extension.crud.model.query.PageQuery;
import top.charles7c.continew.starter.extension.crud.model.resp.PageResp;

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

    private final LogMapper logMapper;

    @Override
    public PageResp<LogResp> page(LogQuery query, PageQuery pageQuery) {
        QueryWrapper<LogDO> queryWrapper = new QueryWrapper<>();
        // 构建条件
        String description = query.getDescription();
        if (StrUtil.isNotBlank(description)) {
            queryWrapper.and(q -> q.like("t1.description", description).or().like("t1.module", description));
        }
        String module = query.getModule();
        if (StrUtil.isNotBlank(module)) {
            queryWrapper.eq("t1.module", module);
        }
        String ip = query.getIp();
        if (StrUtil.isNotBlank(ip)) {
            queryWrapper.and(q -> q.like("t1.ip", ip).or().like("t1.address", ip));
        }
        String createUserString = query.getCreateUserString();
        if (StrUtil.isNotBlank(createUserString)) {
            queryWrapper.and(q -> q.like("t2.username", createUserString).or().like("t2.nickname", createUserString));
        }
        List<Date> createTimeList = query.getCreateTime();
        if (CollUtil.isNotEmpty(createTimeList)) {
            ValidationUtils.throwIf(createTimeList.size() != 2, "[{}] 必须是一个范围", "createTime");
            queryWrapper.between("t1.create_time", createTimeList.get(0), createTimeList.get(1));
        }
        Integer status = query.getStatus();
        if (null != status) {
            queryWrapper.eq("t1.status", status);
        }
        IPage<LogResp> page = logMapper.selectLogPage(pageQuery.toPage(), queryWrapper);
        return PageResp.build(page);
    }

    @Override
    @AutoOperate(type = LogDetailResp.class)
    public LogDetailResp get(Long id) {
        LogDO logDO = logMapper.selectById(id);
        CheckUtils.throwIfNotExists(logDO, "LogDO", "ID", id);
        return BeanUtil.copyProperties(logDO, LogDetailResp.class);
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
