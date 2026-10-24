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
import top.continew.admin.auth.config.OnlineUserProperties;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.auth.service.SessionActivityService;
import top.continew.admin.auth.support.ClientPolicyCache;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.auth.refresh.token.model.SessionView;
import top.continew.starter.auth.refresh.token.service.SessionInvalidationService;
import top.continew.starter.auth.refresh.token.service.SessionQueryService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 在线用户业务实现
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private final SessionInvalidationService sessionInvalidationService;
    private final SessionQueryService sessionQueryService;
    private final SessionActivityService sessionActivityService;
    private final ClientPolicyCache clientPolicyCache;
    private final OnlineUserProperties onlineUserProperties;

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
        long now = System.currentTimeMillis();
        // Refresh Session 才是可恢复登录态的事实源。Access Token 即使已经自然过期，
        // 只要长期会话仍有效，管理员就必须能够看到并撤销该设备登录。
        List<SessionView> sessions = sessionQueryService.listSessions(tenantId).stream()
            // 只有绝对有效期到期才隐藏：此时会话确实不可用。闲置超时的会话要等到下一次请求
            // （刷新时判定并撤销）才消失，在此之前它仍占用登录名额，必须继续展示，否则管理员
            // 既看不到、也没法强退这台还没被回收的设备。
            .filter(session -> session.getExpiresAt() > now)
            .filter(session -> this.isMatch(query, session, DateUtil.date(session.getCreatedAt())
                .toLocalDateTime()))
            .toList();
        Map<String, Long> lastActiveTimes = sessionActivityService.getLastActiveTimes(sessions
            .stream()
            .map(SessionView::getSessionId)
            .toList());
        Map<String, ClientPolicyCache.ClientPolicy> policies = clientPolicyCache.all();
        long presenceWindowMillis = onlineUserProperties.getPresenceWindow().toMillis();
        for (SessionView session : sessions) {
            Long recordedActiveTime = lastActiveTimes.get(session.getSessionId());
            // 没有活跃记录时按最近刷新时间推算，并标记为估算值：不能当作「从未活跃」。
            long lastActiveTime = recordedActiveTime == null
                ? session.getLastRefreshAt()
                : recordedActiveTime;
            final long idleTimeoutMillis =
                policies.getOrDefault(session.getClientId(), ClientPolicyCache.ClientPolicy.DEFAULT)
                    .idleTimeoutSeconds() * 1000;
            // 没有活跃记录时不判「已闲置」：此时时间是按最近刷新推算出来的，断言过期会误标；
            // 与拦截器、刷新兜底两处「无记录不判定」的口径保持一致。
            final boolean hasActiveRecord = recordedActiveTime != null;
            final boolean idleExpired = hasActiveRecord && idleTimeoutMillis > 0
                && now - lastActiveTime > idleTimeoutMillis;
            // 已经闲置超时的会话不再计入在线：闲置阈值可能比在线展示窗口还短，
            // 若只按活跃时间判断，会出现「闲置超时待失效」和「在线」同时成立的矛盾展示。
            boolean online = !idleExpired && now - lastActiveTime <= presenceWindowMillis;
            if (query.getOnline() != null && query.getOnline() != online) {
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
            resp.setLoginTime(DateUtil.date(session.getCreatedAt()).toLocalDateTime());
            resp.setLastRefreshTime(DateUtil.date(session.getLastRefreshAt()).toLocalDateTime());
            resp.setLastActiveTime(DateUtil.date(lastActiveTime).toLocalDateTime());
            resp.setActiveTimeExact(hasActiveRecord);
            resp.setSessionExpireTime(DateUtil.date(session.getExpiresAt()).toLocalDateTime());
            // 没有活跃记录时不给出「预计失效」：时间本身是推算的，算出来的阈值时刻可能已过去，
            // 会让人误以为这台设备马上要掉线。此时前端回落到会话的绝对到期时间。
            resp.setIdleExpireTime(hasActiveRecord && idleTimeoutMillis > 0
                ? DateUtil.date(lastActiveTime + idleTimeoutMillis).toLocalDateTime()
                : null);
            resp.setIdleExpired(idleExpired);
            resp.setOnline(online);
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
     * 是否匹配查询条件
     *
     * @param query     查询条件
     * @param session   登录会话
     * @param loginTime 登录时间
     * @return 是否匹配
     */
    private boolean isMatch(OnlineUserQuery query, SessionView session,
        LocalDateTime loginTime) {
        if (query.getUserId() != null
            && !Objects.equals(query.getUserId(), session.getUserId())) {
            return false;
        }
        return this.isMatchNickname(query.getNickname(), session)
            && this.isMatchClientId(query.getClientId(), session.getClientId())
            && this.isMatchLoginTime(query.getLoginTime(), loginTime);
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
