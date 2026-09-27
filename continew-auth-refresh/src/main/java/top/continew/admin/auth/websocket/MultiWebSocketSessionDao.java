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
import top.continew.starter.messaging.websocket.dao.WebSocketSessionDao;

import java.util.Collection;

/**
 * 支持同一客户端 Key 挂载多条连接的 WebSocket 会话 DAO。
 *
 * <p>浏览器多标签页共用同一 Access Token 时，Starter 默认 DAO 以 Key 为唯一维度、
 * 后建连接覆盖前者；本接口补充按 Key 枚举与整组删除能力，供撤销路径全量关闭。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
public interface MultiWebSocketSessionDao extends WebSocketSessionDao {

    /**
     * 获取指定 Key 挂载的全部连接。
     *
     * @param key 客户端 Key（Access Token）
     * @return 连接集合（可能为空）
     */
    Collection<WebSocketSession> listByKey(String key);

    /**
     * 删除指定 Key 的全部连接登记。
     *
     * @param key 客户端 Key（Access Token）
     */
    void removeAll(String key);
}
