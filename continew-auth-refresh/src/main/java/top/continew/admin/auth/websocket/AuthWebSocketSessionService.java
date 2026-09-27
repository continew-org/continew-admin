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

package top.continew.admin.auth.websocket;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import top.continew.admin.auth.api.AuthSessionConstants;
import top.continew.admin.auth.api.AccessSessionValidator;
import top.continew.admin.auth.api.AuthSessionRevocationNotifier;
import top.continew.admin.auth.config.RefreshTokenProperties;
import top.continew.starter.messaging.websocket.dao.WebSocketSessionDao;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Refresh Session 与 WebSocket 连接联动服务。
 *
 * <p>Starter 使用 Access Token 作为 WebSocket 客户端 ID。本服务通过 Access Token 中的
 * {@code sid} 找到同一 Refresh Session 的连接，并使用 Redis Topic 通知所有应用实例，
 * 避免用户禁用、强退或密码修改后已建立的连接继续存活。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthWebSocketSessionService implements AuthSessionRevocationNotifier {

    private static final String REVOCATION_TOPIC = "AUTH:REFRESH:V1:WEBSOCKET:REVOKED";

    private final RedissonClient redissonClient;
    private final ObjectProvider<WebSocketSessionDao> sessionDaoProvider;
    private final ObjectProvider<AccessSessionValidator> accessSessionValidatorProvider;
    private final RefreshTokenProperties refreshTokenProperties;

    /** 本实例 Access Token → Refresh Session ID 的本地索引，避免每次撤销通知回源 Redis。 */
    private final Map<String, String> tokenSessionIdCache = new ConcurrentHashMap<>();

    private RTopic revocationTopic;
    private Integer listenerId;

    /** 订阅集群内 Refresh Session 撤销通知。 */
    @PostConstruct
    public void subscribe() {
        revocationTopic = redissonClient.getTopic(REVOCATION_TOPIC);
        listenerId = revocationTopic.addListener(String.class, (channel, sessionId) -> {
            try {
                this.closeLocal(sessionId);
            } catch (RuntimeException e) {
                // 与 notifyRevoked 的降级策略一致：DAO/Redis 抖动时不阻断监听器，
                // 本次撤销消息丢失由周期校验与本地缓存 TTL 兜底。
                log.warn("收到撤销通知后关闭会话 [{}] 的本地 WebSocket 连接失败", sessionId, e);
            }
        });
    }

    /** 释放 Redis Topic 监听器。 */
    @PreDestroy
    public void unsubscribe() {
        if (revocationTopic != null && listenerId != null) {
            revocationTopic.removeListener(listenerId);
        }
    }

    /**
     * 关闭本实例连接并通知集群内其他实例关闭同一登录会话的连接。
     *
     * @param sessionId Refresh Session ID
     */
    @Override
    public void notifyRevoked(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        try {
            revocationTopic.publish(sessionId);
        } catch (RuntimeException e) {
            // Refresh Session 已经由认证服务删除，实时通知只是加速关闭连接的旁路机制。
            log.warn("发布 Refresh Session [{}] 的 WebSocket 撤销通知失败", sessionId, e);
        }
        try {
            this.closeLocal(sessionId);
        } catch (RuntimeException e) {
            log.warn("关闭 Refresh Session [{}] 的本地 WebSocket 连接失败", sessionId, e);
        }
    }

    /**
     * 周期校验本实例的 WebSocket 凭证，兜底 Redis Pub/Sub 丢消息和 Token 自然过期。
     */
    @Scheduled(fixedDelayString = "#{@refreshTokenProperties.websocketValidationInterval}")
    public void validateLocalSessions() {
        WebSocketSessionDao sessionDao = sessionDaoProvider.getIfAvailable();
        AccessSessionValidator accessSessionValidator =
            accessSessionValidatorProvider.getIfAvailable();
        if (sessionDao == null || accessSessionValidator == null) {
            return;
        }
        for (String accessToken : new LinkedHashSet<>(sessionDao.listAllSessionIds())) {
            try {
                if (accessSessionValidator.isInvalid(accessToken)) {
                    this.closeLocalAccessToken(sessionDao, accessToken);
                    this.tokenSessionIdCache.remove(accessToken);
                }
            } catch (RuntimeException e) {
                // 基础设施短暂异常时保留连接，下一轮继续校验，避免误杀全部实时连接。
                log.warn("校验 WebSocket Access Token 所属登录会话失败", e);
            }
        }
        // 连接已关闭的 Token 不再保留本地索引条目，避免缓存无限增长。
        Set<String> currentTokens = new LinkedHashSet<>(sessionDao.listAllSessionIds());
        this.tokenSessionIdCache.keySet().removeIf(token -> !currentTokens.contains(token));
    }

    private void closeLocal(String sessionId) {
        WebSocketSessionDao sessionDao = sessionDaoProvider.getIfAvailable();
        if (sessionDao == null) {
            return;
        }
        // DAO 的 Key 是握手时保存的 Access Token。复制一份，避免关闭回调同步删除时
        // 修改正在遍历的集合。
        Set<String> tokens = new LinkedHashSet<>(sessionDao.listAllSessionIds());
        for (String accessToken : tokens) {
            if (!this.belongsToSession(accessToken, sessionId)) {
                continue;
            }
            this.closeLocalAccessToken(sessionDao, accessToken);
            this.tokenSessionIdCache.remove(accessToken);
        }
        // 连接已关闭的 Token 不再保留本地索引条目，避免缓存无限增长。
        this.tokenSessionIdCache.keySet().removeIf(token -> !tokens.contains(token));
    }

    private void closeLocalAccessToken(WebSocketSessionDao sessionDao, String accessToken) {
        if (sessionDao instanceof MultiWebSocketSessionDao multiSessionDao) {
            // 浏览器多标签页共用同一 Access Token 时，关闭该 Token 的全部连接。
            for (WebSocketSession session : multiSessionDao.listByKey(accessToken)) {
                this.closeSession(session);
            }
            multiSessionDao.removeAll(accessToken);
            return;
        }
        this.closeSession(sessionDao.get(accessToken));
        sessionDao.delete(accessToken);
    }

    private void closeSession(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return;
        }
        try {
            if (webSocketSession.isOpen()) {
                webSocketSession.close(CloseStatus.POLICY_VIOLATION);
            }
        } catch (IOException e) {
            log.warn("关闭失效认证会话的 WebSocket 连接失败", e);
        }
    }

    private boolean belongsToSession(String accessToken, String sessionId) {
        String cachedSessionId = tokenSessionIdCache.get(accessToken);
        if (cachedSessionId != null) {
            return Objects.equals(sessionId, cachedSessionId);
        }
        try {
            String claimedSessionId = Convert.toStr(StpUtil.getExtra(accessToken,
                AuthSessionConstants.SESSION_ID_CLAIM));
            // Access Token 的会话声明在签发后不可变，可以安全缓存，批量撤销时
            // 只需首次回源 Redis，后续通知全部命中本地索引。
            if (claimedSessionId != null) {
                tokenSessionIdCache.put(accessToken, claimedSessionId);
            }
            return Objects.equals(sessionId, claimedSessionId);
        } catch (RuntimeException e) {
            log.debug("忽略无法解析会话声明的 WebSocket Access Token", e);
            return false;
        }
    }
}
