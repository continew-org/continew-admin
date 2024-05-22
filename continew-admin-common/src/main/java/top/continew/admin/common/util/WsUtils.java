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

package top.continew.admin.common.util;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import top.continew.admin.common.model.dto.WsMsg;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket工具类
 *
 * @author WeiRan
 * @since 2024.03.13 16:45
 */
@Slf4j
public class WsUtils {
    /**
     * 静态变量，用来记录当前用户的session，线程安全的类。
     */
    public static ConcurrentHashMap<String, WebSocketSession> webSocketSessionMap = new ConcurrentHashMap<>();

    /**
     * 静态变量，用来记录当前在线连接数，线程安全的类。
     */
    public static AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 绑定用户连接
     *
     * @param userId  用户ID
     * @param session WebSocketSession信息
     */
    public static void bindUser(String userId, WebSocketSession session) {
        webSocketSessionMap.put(userId, session);
    }

    /**
     * 删除连接
     *
     * @param session WebSocketSession信息
     */
    public static void remove(WebSocketSession session) {
        //在线数减1
        onlineCount.decrementAndGet();
        webSocketSessionMap.remove(getUserId(session));
    }

    /**
     * 关闭 WebSocket 连接的方法。
     *
     * @param webSocketSession WebSocketSession信息，表示当前的 WebSocket 连接。
     * @param msg              发送消息内容，用于记录关闭连接的原因或相关信息。
     * @throws IOException 当关闭 WebSocket 连接时可能抛出的 IO 异常。
     */
    public static void close(WebSocketSession webSocketSession, String msg) throws IOException {
        String userId = getUserId(webSocketSession);
        if (!webSocketSession.isOpen()) {
            WsUtils.remove(webSocketSession);
            log.warn("连接对象【{}】已关闭：{}", userId, msg);
        } else {
            webSocketSession.close(CloseStatus.SERVER_ERROR);
            WsUtils.remove(webSocketSession);
            log.error("服务端主动关闭连接：用户{}信息：{}", userId, msg);
        }
    }

    /**
     * 通过用户ID获取对应的 WebSocketSession。
     *
     * @param userId 用户ID，用于查找对应的 WebSocketSession。
     * @return WebSocketSession 如果存在与给定用户ID对应的 WebSocketSession，则返回该对象；如果不存在，则返回 null。
     */
    public static WebSocketSession getWebSocketSession(String userId) {
        return webSocketSessionMap.get(userId);
    }

    /**
     * 获取用户的session
     *
     * @param session WebSocketSession对象，包含用户的会话信息
     * @return 用户的ID，类型为字符串
     */
    public static String getUserId(WebSocketSession session) {
        return (String)session.getAttributes().get("userId");
    }

    /**
     * 获取用户的IP地址
     *
     * @param session WebSocketSession对象，包含用户的会话信息
     * @return 用户的IP地址，类型为字符串
     */
    public static String getIp(WebSocketSession session) {
        return (String)session.getAttributes().get("ip");
    }

    /**
     * 判断链接是否存在
     *
     * @param userId 用户ID，用于查找对应的WebSocket会话
     * @return 如果映射中存在该用户ID对应的链接，返回true；否则返回false
     */
    public static boolean contains(String userId) {
        return WsUtils.webSocketSessionMap.containsKey(userId);
    }

    /**
     * 判断链接是否存在
     *
     * @param webSocketSession WebSocketSession对象，包含用户的会话信息
     * @return 如果映射中存在该用户ID对应的链接，返回true；否则返回false
     */
    public static boolean contains(WebSocketSession webSocketSession) {
        return WsUtils.webSocketSessionMap.containsKey(getUserId(webSocketSession));
    }

    /**
     * 获取所有用户连接的用户ID列表。
     *
     * @return List<String> 包含所有已连接用户的用户ID的列表。
     */
    public static List<String> getUserList() {
        return Collections.list(webSocketSessionMap.keys());
    }

    /**
     * 发送消息给指定用户。
     *
     * @param userId  用户ID，用于指定消息接收者。
     * @param message 消息内容，要发送给指定用户的消息。
     * @throws IOException 当发送消息时可能抛出的 IO 异常。
     */
    public static void sendToUser(String userId, String message) {
        WebSocketSession webSocketSession = getWebSocketSession(userId);

        if (webSocketSession == null || !webSocketSession.isOpen()) {
            log.warn("用户【{}】已关闭，无法送消息：{}", userId, message);
        } else {
            try {
                webSocketSession.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败：{}", e.getMessage(), e);
            }
            log.info("sendMessage：向{}发送消息：{}", userId, message);
        }
    }

    /**
     * 发送消息给指定用户。
     *
     * @param userId  目标用户的ID。
     * @param message 要发送的消息对象。
     * @throws IOException 如果发生IO异常。
     */
    public static void sendToUser(String userId, WsMsg message) {
        WebSocketSession webSocketSession = getWebSocketSession(userId);
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            log.warn("用户【{}】已关闭，无法送消息：{}", userId, JSONObject.toJSONString(message));
        } else {
            try {
                webSocketSession.sendMessage(new TextMessage(JSONObject.toJSONString(message)));
            } catch (IOException e) {
                log.error("发送消息失败：{}", e.getMessage(), e);
            }
            log.info("sendMessage：向{}发送消息：{}", userId, JSONObject.toJSONString(message));
        }
    }

    /**
     * 发送消息全部在线用户
     *
     * @param message 发送的消息内容
     * @throws IOException 如果发生IO异常。
     */
    public static void sendToAll(String message) {
        getUserList().forEach(userId -> sendToUser(userId, message));
    }

    /**
     * 发送消息全部在线用户
     *
     * @param message 发送的消息内容
     * @throws IOException 如果发生IO异常。
     */
    public static void sendToAll(WsMsg message) {
        getUserList().forEach(userId -> sendToUser(userId, JSONObject.toJSONString(message)));
    }

    /**
     * 发送通过webSocketSession发送消息
     *
     * @param webSocketSession 对象id
     * @param message          发送的消息内容
     * @throws IOException 如果发生IO异常。
     */
    public static void send(WebSocketSession webSocketSession, String message) {

        if (webSocketSession == null || !webSocketSession.isOpen()) {
            log.warn("连接对象【{}】已关闭，无法送消息：{}", webSocketSession.getId(), message);
        } else {
            try {
                webSocketSession.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败：{}", e.getMessage(), e);
            }
            log.info("sendMessage：向{}发送消息：{}", webSocketSession.getId(), message);
        }
    }

    /**
     * 通过 WebSocketSession 对象发送消息。
     *
     * @param webSocketSession 目标 WebSocketSession 对象。
     * @param wsMsg            要发送的WsMsg消息对象。
     * @throws IOException 如果发生IO异常。
     */
    public static void send(WebSocketSession webSocketSession, WsMsg wsMsg) {
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            log.warn("连接对象【{}】已关闭，无法送消息：{}", webSocketSession.getId(), JSONObject.toJSONString(wsMsg));
        } else {
            try {
                webSocketSession.sendMessage(new TextMessage(JSONObject.toJSONString(wsMsg)));
            } catch (IOException e) {
                log.error("发送消息失败：{}", e.getMessage(), e);
            }
            log.info("sendMessage：向{}发送消息：{}", webSocketSession.getId(), JSONObject.toJSONString(wsMsg));
        }
    }

}
