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

package top.continew.admin.auth.service.impl;

import cn.crane4j.annotation.AutoOperate;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.config.properties.TenantProperties;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.context.UserExtraContext;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线用户业务实现
 *
 * @author Charles7c
 * @since 2023/3/25 22:49
 */
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private final TenantProperties tenantProperties;

    @Override
    @AutoOperate(type = OnlineUserResp.class, on = "list")
    public PageResp<OnlineUserResp> page(OnlineUserQuery query, PageQuery pageQuery) {
        List<OnlineUserResp> list = this.list(query);
        return PageResp.build(pageQuery.getPage(), pageQuery.getSize(), list);
    }

    @Override
    public List<OnlineUserResp> list(OnlineUserQuery query) {
        List<OnlineUserResp> list = new ArrayList<>();
        // 查询所有在线 Token
        List<String> tokenKeyList = StpUtil.searchTokenValue(StringConstants.EMPTY, 0, -1, false);
        Map<Long, List<String>> tokenMap = tokenKeyList.stream()
            // 提前映射，避免重复调用
            .map(tokenKey -> StrUtil.subAfter(tokenKey, StringConstants.COLON, true))
            .map(token -> {
                Object loginIdObj = StpUtil.getLoginIdByToken(token);
                long tokenTimeout = StpUtil.getStpLogic().getTokenActiveTimeoutByToken(token);
                // 将相关信息打包成对象或简单的Entry对，便于后续过滤与归类
                return new AbstractMap.SimpleEntry<>(token, new AbstractMap.SimpleEntry<>(loginIdObj, tokenTimeout));
            })
            // 过滤出未过期且loginId存在的Token
            .filter(entry -> {
                Object loginIdObj = entry.getValue().getKey();
                long tokenTimeout = entry.getValue().getValue();
                return loginIdObj != null && tokenTimeout >= SaTokenDao.NEVER_EXPIRE;
            })
            // 此时数据都有效，进行收集
            .collect(Collectors.groupingBy(entry -> Convert.toLong(entry.getValue().getKey()), Collectors
                .mapping(AbstractMap.SimpleEntry::getKey, Collectors.toList())));
        // 筛选数据
        for (Map.Entry<Long, List<String>> entry : tokenMap.entrySet()) {
            Long userId = entry.getKey();
            UserContext userContext = UserContextHolder.getContext(userId);
            if (userContext == null || !this.isMatchNickname(query.getNickname(), userContext) || !this
                .isMatchClientId(query.getClientId(), userContext)) {
                continue;
            }
            //租户数据过滤
            if (tenantProperties.isEnabled()) {
                if (!TenantContextHolder.getTenantId().equals(userContext.getTenantId())) {
                    continue;
                }
            }
            List<LocalDateTime> loginTimeList = query.getLoginTime();
            entry.getValue().parallelStream().forEach(token -> {
                UserExtraContext extraContext = UserContextHolder.getExtraContext(token);
                if (!this.isMatchLoginTime(loginTimeList, extraContext.getLoginTime())) {
                    return;
                }
                OnlineUserResp resp = BeanUtil.copyProperties(userContext, OnlineUserResp.class);
                BeanUtil.copyProperties(extraContext, resp);
                resp.setToken(token);
                list.add(resp);
            });
        }
        // 设置排序
        CollUtil.sort(list, Comparator.comparing(OnlineUserResp::getLoginTime).reversed());
        return list;
    }

    @Override
    public LocalDateTime getLastActiveTime(String token) {
        long lastActiveTime = StpUtil.getStpLogic().getTokenLastActiveTime(token);
        return lastActiveTime == SaTokenDao.NOT_VALUE_EXPIRE ? null : DateUtil.date(lastActiveTime).toLocalDateTime();
    }

    @Override
    public void kickOut(Long userId) {
        if (!StpUtil.isLogin(userId)) {
            return;
        }
        StpUtil.logout(userId);
    }

    /**
     * 是否匹配昵称
     *
     * @param nickname    昵称
     * @param userContext 用户上下文信息
     * @return 是否匹配昵称
     */
    private boolean isMatchNickname(String nickname, UserContext userContext) {
        if (StrUtil.isBlank(nickname)) {
            return true;
        }
        return StrUtil.contains(userContext.getUsername(), nickname) || StrUtil.contains(UserContextHolder
            .getNickname(userContext.getId()), nickname);
    }

    /**
     * 是否匹配客户端 ID
     *
     * @param clientId    客户端 ID
     * @param userContext 用户上下文信息
     * @return 是否匹配客户端 ID
     */
    private boolean isMatchClientId(String clientId, UserContext userContext) {
        if (StrUtil.isBlank(clientId)) {
            return true;
        }
        return Objects.equals(userContext.getClientId(), clientId);
    }

    /**
     * 是否匹配登录时间
     *
     * @param loginTimeList 登录时间列表
     * @param loginTime     登录时间
     * @return 是否匹配登录时间
     */
    private boolean isMatchLoginTime(List<LocalDateTime> loginTimeList, LocalDateTime loginTime) {
        if (CollUtil.isEmpty(loginTimeList)) {
            return true;
        }
        return loginTime.isAfter(loginTimeList.get(0)) && loginTime.isBefore(loginTimeList.get(1));
    }
}
