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

package top.continew.admin.auth.support;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.continew.admin.system.mapper.ClientMapper;
import top.continew.admin.system.model.entity.ClientDO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端认证策略本地缓存
 *
 * <p>活跃时间判定要按请求读取客户端的闲置失效阈值，在线列表也要按客户端换算预计失效时间，
 * 而客户端配置极少变更，因此整表缓存 {@value #EXPIRE_MILLIS} 毫秒后重载，避免把数据库查询
 * 引入每个请求的链路。</p>
 *
 * <p>直接使用 Mapper 而不是 ClientService：ClientService 依赖在线用户服务，在线用户服务又需要
 * 这里的策略，走 Service 会形成循环依赖。sys_client 属于全局表，不需要携带租户上下文。</p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientPolicyCache {

    /**
     * 缓存有效期（毫秒）
     */
    private static final long EXPIRE_MILLIS = 60_000;

    private final ClientMapper clientMapper;

    private volatile Map<String, ClientPolicy> policies = Collections.emptyMap();

    private volatile long expireAt;

    /**
     * 查询客户端认证策略
     *
     * @param clientId 客户端 ID
     * @return 认证策略，客户端不存在时返回不限闲置的默认策略
     */
    public ClientPolicy get(String clientId) {
        if (clientId == null) {
            return ClientPolicy.DEFAULT;
        }
        return this.snapshot().getOrDefault(clientId, ClientPolicy.DEFAULT);
    }

    /**
     * 查询全部客户端认证策略
     *
     * @return 客户端 ID 与认证策略的映射
     */
    public Map<String, ClientPolicy> all() {
        return this.snapshot();
    }

    private Map<String, ClientPolicy> snapshot() {
        long now = System.currentTimeMillis();
        if (now < this.expireAt) {
            return this.policies;
        }
        synchronized (this) {
            if (now < this.expireAt) {
                return this.policies;
            }
            try {
                this.policies = this.load();
            } catch (Exception e) {
                // 数据库抖动时沿用上一次快照，宁可短暂使用旧策略，也不要让认证链路整体失败。
                log.warn("加载客户端认证策略失败，沿用上一次缓存：{}", e.getMessage());
            } finally {
                // 失败也要退避一个周期：否则数据库异常期间每个请求都会重试查询一次。
                this.expireAt = now + EXPIRE_MILLIS;
            }
            return this.policies;
        }
    }

    private Map<String, ClientPolicy> load() {
        List<ClientDO> clients = clientMapper.selectList(new LambdaQueryWrapper<ClientDO>()
            .select(ClientDO::getClientId,
                ClientDO::getRefreshTokenIdleTimeout,
                ClientDO::getRefreshTokenTimeout));
        Map<String, ClientPolicy> result = new HashMap<>(clients.size());
        for (ClientDO client : clients) {
            result.put(client.getClientId(), new ClientPolicy(ObjectUtil
                .defaultIfNull(client.getRefreshTokenIdleTimeout(), 0L),
                ObjectUtil
                    .defaultIfNull(client.getRefreshTokenTimeout(),
                        ClientPolicy.DEFAULT_REFRESH_TIMEOUT)));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * 客户端认证策略
     *
     * @param idleTimeoutSeconds       闲置失效阈值（单位：秒，0：不限制）
     * @param refreshTokenTimeoutSeconds Refresh Token 绝对有效期（单位：秒）
     */
    public record ClientPolicy(long idleTimeoutSeconds, long refreshTokenTimeoutSeconds) {

        /**
         * 默认 Refresh Token 绝对有效期（30 天），与数据库字段默认值保持一致
         */
        public static final long DEFAULT_REFRESH_TIMEOUT = 2_592_000L;

        /**
         * 默认策略：不限闲置失效
         */
        public static final ClientPolicy DEFAULT = new ClientPolicy(0L, DEFAULT_REFRESH_TIMEOUT);
    }
}
