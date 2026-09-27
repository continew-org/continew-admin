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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.model.SessionView;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.auth.service.SessionInvalidationService;
import top.continew.admin.auth.service.SessionQueryService;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 在线用户业务实现
 *
 * @author Charles7c
 * @since 2023/3/25 22:49
 */
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private final SessionInvalidationService sessionInvalidationService;
    private final SessionQueryService sessionQueryService;

    @Override
    public PageResp<OnlineUserResp> page(OnlineUserQuery query, PageQuery pageQuery) {
        List<OnlineUserResp> list = this.list(query);
        return PageResp.build(pageQuery.getPage(), pageQuery.getSize(), list);
    }

    @Override
    public List<OnlineUserResp> list(OnlineUserQuery query) {
        List<OnlineUserResp> list = new ArrayList<>();
        Long tenantId = TenantContextHolder.isTenantEnabled() && !UserContextHolder.isSuperAdmin()
            ? TenantContextHolder.getTenantId()
            : null;
        // Refresh Session 才是可恢复登录态的事实源。Access Token 即使已经自然过期，
        // 只要长期会话仍有效，管理员就必须能够看到并撤销该设备登录。
        for (SessionView session : sessionQueryService.listSessions(tenantId)) {
            if (query.getUserId() != null && !Objects.equals(query.getUserId(), session.getUserId())
                || !this.isMatchNickname(query.getNickname(), session)
                || !this.isMatchClientId(query.getClientId(), session.getClientId())) {
                continue;
            }
            LocalDateTime loginTime = DateUtil.date(session.getCreatedAt()).toLocalDateTime();
            if (!this.isMatchLoginTime(query.getLoginTime(), loginTime)) {
                continue;
            }
            OnlineUserResp resp = new OnlineUserResp();
            resp.setId(session.getUserId());
            resp.setSessionId(session.getSessionId());
            resp.setUsername(session.getUsername());
            resp.setNickname(session.getNickname());
            resp.setClientType(session.getClientType());
            resp.setClientId(session.getClientId());
            resp.setIp(session.getIp());
            resp.setAddress(session.getAddress());
            resp.setBrowser(session.getBrowser());
            resp.setOs(session.getOs());
            resp.setLoginTime(loginTime);
            resp.setLastRefreshTime(DateUtil.date(session.getLastRefreshAt()).toLocalDateTime());
            list.add(resp);
        }
        // 登录时间可能缺失，排序必须空值安全。
        CollUtil.sort(list, Comparator.comparing(OnlineUserResp::getLoginTime, Comparator
            .nullsFirst(Comparator.naturalOrder())).reversed());
        return list;
    }

    @Override
    public void kickOut(Long userId) {
        // 认证会话在事务提交后统一失效；Access Token 校验会立即拒绝已失效会话。
        sessionInvalidationService.invalidateUser(userId);
    }

    /**
     * 是否匹配昵称
     *
     * @param nickname 昵称
     * @param session  登录会话
     * @return 是否匹配昵称
     */
    private boolean isMatchNickname(String nickname, SessionView session) {
        if (StrUtil.isBlank(nickname)) {
            return true;
        }
        return StrUtil.contains(session.getUsername(), nickname)
            || StrUtil.contains(session.getNickname(), nickname);
    }

    /**
     * 是否匹配客户端 ID
     *
     * @param clientId       客户端 ID
     * @param userClientId   令牌对应的客户端 ID
     * @return 是否匹配客户端 ID
     */
    private boolean isMatchClientId(String clientId, String userClientId) {
        if (StrUtil.isBlank(clientId)) {
            return true;
        }
        return Objects.equals(userClientId, clientId);
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
        // 查询参数来自外部请求，必须防御只传一个时间点或空边界。
        if (loginTime == null || loginTimeList.size() < 2 || loginTimeList.get(0) == null
            || loginTimeList.get(1) == null) {
            return false;
        }
        return loginTime.isAfter(loginTimeList.get(0)) && loginTime.isBefore(loginTimeList.get(1));
    }

}
