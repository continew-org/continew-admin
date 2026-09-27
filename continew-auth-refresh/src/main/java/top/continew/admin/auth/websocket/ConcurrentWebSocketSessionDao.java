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

import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持多标签页的 WebSocket 会话 DAO 内存实现。
 *
 * <p>以 {@code Key → (连接 ID → 连接)} 两级登记：同一 Access Token 的多个标签页
 * 连接互不覆盖。Starter 的 {@code afterConnectionClosed}/{@code handleTransportError}
 * 关闭回调只携带 Key，因此 {@link #delete(String)} 采用「只移除已关闭连接」的语义，
 * 保留同一 Key 下仍存活的其它标签页连接。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
public class ConcurrentWebSocketSessionDao implements MultiWebSocketSessionDao {

    private final Map<String, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void add(String key, WebSocketSession session) {
        sessions.computeIfAbsent(key, k -> new ConcurrentHashMap<>())
            .put(session.getId(), session);
    }

    @Override
    public void delete(String key) {
        Map<String, WebSocketSession> byId = sessions.get(key);
        if (byId == null) {
            return;
        }
        byId.values().removeIf(session -> !session.isOpen());
        if (byId.isEmpty()) {
            sessions.remove(key);
        }
    }

    @Override
    public WebSocketSession get(String key) {
        Map<String, WebSocketSession> byId = sessions.get(key);
        if (byId == null || byId.isEmpty()) {
            return null;
        }
        // 推送是单接收方语义（WebSocketUtils.sendMessage），取最新一条存活连接。
        WebSocketSession latest = null;
        for (WebSocketSession session : byId.values()) {
            if (session.isOpen()) {
                latest = session;
            }
        }
        return latest;
    }

    @Override
    public Collection<WebSocketSession> listAll() {
        return sessions.values().stream().flatMap(byId -> byId.values().stream()).toList();
    }

    @Override
    public Set<String> listAllSessionIds() {
        return sessions.keySet();
    }

    @Override
    public Collection<WebSocketSession> listByKey(String key) {
        Map<String, WebSocketSession> byId = sessions.get(key);
        return byId == null ? List.of() : List.copyOf(byId.values());
    }

    @Override
    public void removeAll(String key) {
        sessions.remove(key);
    }
}
