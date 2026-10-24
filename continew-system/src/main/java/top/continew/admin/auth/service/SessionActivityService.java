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

package top.continew.admin.auth.service;

import java.util.Collection;
import java.util.Map;

/**
 * 会话活跃时间业务接口
 *
 * <p>活跃时间只用于两件事：判断会话是否已经闲置到该失效，以及在线列表展示「最后活跃」。
 * 它不是凭据，不参与任何鉴权，丢失（例如升级前的存量会话、键已过期）时按「无法判断」处理。</p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
public interface SessionActivityService {

    /**
     * 记录一次活跃
     *
     * <p>同一会话在一个节流窗口内只会写入一次缓存，调用方可以放心地在每个业务请求上调用。</p>
     *
     * @param sessionId 登录会话 ID
     * @param clientId  客户端 ID
     */
    void touch(String sessionId, String clientId);

    /**
     * 查询最后活跃时间
     *
     * @param sessionId 登录会话 ID
     * @return 最后活跃时间（epoch 毫秒），没有记录时返回 null
     */
    Long getLastActiveTime(String sessionId);

    /**
     * 批量查询最后活跃时间
     *
     * @param sessionIds 登录会话 ID 集合
     * @return 登录会话 ID 与最后活跃时间的映射，没有记录的会话不会出现在结果中
     */
    Map<String, Long> getLastActiveTimes(Collection<String> sessionIds);

    /**
     * 会话是否已经闲置到该失效
     *
     * @param sessionId          登录会话 ID
     * @param idleTimeoutSeconds 闲置失效阈值（单位：秒，0：不限制）
     * @return 是否已闲置失效；没有活跃记录时一律返回 false
     */
    boolean isIdleExpired(String sessionId, long idleTimeoutSeconds);
}
