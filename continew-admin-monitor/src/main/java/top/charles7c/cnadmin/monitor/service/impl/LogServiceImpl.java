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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageInfo;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.ReflectUtils;
import top.charles7c.cnadmin.common.util.helper.QueryHelper;
import top.charles7c.cnadmin.monitor.mapper.LogMapper;
import top.charles7c.cnadmin.monitor.model.entity.SysLog;
import top.charles7c.cnadmin.monitor.model.query.LoginLogQuery;
import top.charles7c.cnadmin.monitor.model.query.OperationLogQuery;
import top.charles7c.cnadmin.monitor.model.vo.LogVO;
import top.charles7c.cnadmin.monitor.model.vo.LoginLogVO;
import top.charles7c.cnadmin.monitor.model.vo.OperationLogVO;
import top.charles7c.cnadmin.monitor.service.LogService;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 系统日志业务实现类
 *
 * @author Charles7c
 * @since 2022/12/23 20:12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;
    private final UserService userService;

    @Async
    @EventListener
    public void save(SysLog sysLog) {
        logMapper.insert(sysLog);
    }

    @Override
    public PageInfo<OperationLogVO> list(OperationLogQuery query, PageQuery pageQuery) {
        QueryWrapper<SysLog> queryWrapper = QueryHelper.build(query);

        // 限定查询信息
        String[] fieldsName = ReflectUtils.getNonStaticFieldsName(OperationLogVO.class);
        List<String> columns = Arrays.stream(fieldsName).map(StrUtil::toUnderlineCase)
            .filter(n -> !n.endsWith("string")).collect(Collectors.toList());
        queryWrapper.select(columns);

        // 分页查询
        IPage<SysLog> page = logMapper.selectPage(pageQuery.toPage(), queryWrapper);
        PageInfo<OperationLogVO> pageInfo = PageInfo.build(page, OperationLogVO.class);

        // 填充数据（如果是查询个人操作日志，只查询一次用户信息即可）
        if (query.getUid() != null) {
            String nickname = ExceptionUtils.exToNull(() -> userService.getById(query.getUid()).getNickname());
            pageInfo.getList().forEach(o -> o.setCreateUserString(nickname));
        } else {
            pageInfo.getList().forEach(this::fill);
        }
        return pageInfo;
    }

    @Override
    public PageInfo<LoginLogVO> list(LoginLogQuery query, PageQuery pageQuery) {
        QueryWrapper<SysLog> queryWrapper = QueryHelper.build(query);
        queryWrapper.and(qw -> qw.like("request_url", "/auth/login").or().like("request_url", "/auth/logout"));

        // 限定查询信息
        String[] fieldsName = ReflectUtils.getNonStaticFieldsName(LoginLogVO.class);
        List<String> columns = Arrays.stream(fieldsName).map(StrUtil::toUnderlineCase)
            .filter(n -> !n.endsWith("string")).collect(Collectors.toList());
        queryWrapper.select(columns);

        // 分页查询
        IPage<SysLog> page = logMapper.selectPage(pageQuery.toPage(), queryWrapper);
        PageInfo<LoginLogVO> pageInfo = PageInfo.build(page, LoginLogVO.class);

        // 填充数据
        pageInfo.getList().forEach(this::fill);
        return pageInfo;
    }

    /**
     * 填充数据
     *
     * @param vo
     *            VO
     */
    private void fill(LogVO vo) {
        Long createUser = vo.getCreateUser();
        if (createUser == null) {
            return;
        }
        vo.setCreateUserString(ExceptionUtils.exToNull(() -> userService.getById(vo.getCreateUser())).getNickname());
    }
}
