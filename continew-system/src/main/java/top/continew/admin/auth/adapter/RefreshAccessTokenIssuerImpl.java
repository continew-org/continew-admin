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

package top.continew.admin.auth.adapter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.AuthTokenService;
import top.continew.admin.auth.service.SessionActivityService;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.auth.refresh.token.exception.RefreshTokenException;
import top.continew.starter.auth.refresh.token.model.IssuedAccessToken;
import top.continew.starter.auth.refresh.token.model.RefreshSession;
import top.continew.starter.auth.refresh.token.service.RefreshAccessTokenIssuer;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicReference;

/**
 * system 对刷新主体状态和 Access Token 签发的适配实现
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshAccessTokenIssuerImpl implements RefreshAccessTokenIssuer {

    private final AuthTokenService authTokenService;
    private final ClientService clientService;
    private final TenantApi tenantApi;
    private final UserService userService;
    private final SessionActivityService sessionActivityService;

    @Override
    public IssuedAccessToken issue(RefreshSession session, HttpServletRequest request,
        HttpServletResponse response) {
        ClientResp client = clientService.getByClientId(session.getClientId());
        if (client == null || DisEnableStatusEnum.DISABLE.equals(client.getStatus())) {
            throw RefreshTokenException.unauthorized("登录状态已失效，请重新登录");
        }
        this.checkIdleTimeout(session, client);
        try {
            tenantApi.checkStatus(session.getTenantId());
        } catch (BusinessException e) {
            throw RefreshTokenException.unauthorized(e.getMessage());
        }
        AtomicReference<UserDO> userReference = new AtomicReference<>();
        TenantUtils.execute(session.getTenantId(),
            () -> userReference.set(userService.getById(session.getUserId())));
        if (userReference.get() == null || StrUtil.isBlank(userReference.get().getUsername())) {
            throw RefreshTokenException.unauthorized("登录状态已失效，请重新登录");
        }
        LoginResp loginResp = authTokenService.issueAccessToken(userReference.get(), client,
            session.getTenantId(), session, request, response);
        return new IssuedAccessToken(loginResp.getAccessToken(), loginResp.getExpiresIn(),
            loginResp.getTenantId());
    }

    /**
     * 检查会话是否已闲置超时。
     *
     * <p>调用时机：刷新流程完成令牌轮换（{@code advanceTokenGeneration} 已改写
     * {@code lastRefreshAt}）之后、签发新 Access Token 之前。此处抛出的 401 会被认证组件
     * 捕获并撤销整个会话（{@code issueAndCompleteRotation} → {@code revokeSessionLocked}），
     * 是闲置会话真正下线的唯一入口；拦截器只拒绝当次请求，不删除会话记录。</p>
     *
     * <p>判定依据是活跃时间缓存（与会话记录分开存放，避免轮换的「读-改-写」互相覆盖）。
     * 没有活跃记录时按 {@code createdAt} 兜底，原因见下方行内注释。</p>
     *
     * <p>边界：严格「大于」阈值才算闲置（{@code now - base > 阈值}）；阈值 0 表示不限制，
     * 直接跳过，连缓存都不读。</p>
     *
     * @param session 登录会话
     * @param client  客户端
     */
    private void checkIdleTimeout(RefreshSession session, ClientResp client) {
        long idleTimeoutSeconds = ObjectUtil.defaultIfNull(client.getRefreshTokenIdleTimeout(), 0L);
        if (idleTimeoutSeconds <= 0) {
            return;
        }
        Long lastActive = sessionActivityService.getLastActiveTime(session.getSessionId());
        // 兜底口径与拦截器的差异是已知取舍：拦截器对「无活跃记录」一律不判定，这里必须判定，
        // 否则「只刷新、从不调业务接口」的会话永远不会被回收。浏览器场景下刷新前必然已有
        // 活跃记录，只有上述客户端才会走到 createdAt 兜底。代价是 Redis 在 maxmemory 压力下
        // 淘汰活跃键、且恰好落在节流窗口（最长 60 秒）内时，可能撤掉刚活跃过的会话，
        // 概率极低，接受。
        long baseTime = lastActive == null ? session.getCreatedAt() : lastActive;
        long idleMillis = System.currentTimeMillis() - baseTime;
        if (idleMillis > idleTimeoutSeconds * 1000) {
            log.info("会话闲置超时，撤销登录：sessionId={}，userId={}，闲置时长={}秒，闲置阈值={}秒",
                session.getSessionId(), session.getUserId(), idleMillis / 1000, idleTimeoutSeconds);
            throw RefreshTokenException.unauthorized("长时间未操作，请重新登录");
        }
    }
}
