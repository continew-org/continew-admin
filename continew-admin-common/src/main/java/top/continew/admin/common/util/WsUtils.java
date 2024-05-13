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
import top.continew.admin.common.model.dto.Msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by WeiRan on 2024.04.24 18:26
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
     * @param userId
     * @param session
     */
    public static void bindUser(String userId, WebSocketSession session) {
        webSocketSessionMap.put(userId, session);
    }

    /**
     * 删除连接
     *
     * @param session
     */
    public static void remove(WebSocketSession session) {
        //在线数减1
        onlineCount.decrementAndGet();
        webSocketSessionMap.remove(getUserId(session));
    }

    /**
     * 关闭连接
     *
     * @param webSocketSession
     */
    public static void close(WebSocketSession webSocketSession, String msg) throws IOException {
        String userId = getUserId(webSocketSession);
        if (!webSocketSession.isOpen()) {
            log.warn("连接对象【{}】已关闭：{}", userId, msg);
        } else {
            webSocketSession.close(CloseStatus.SERVER_ERROR);
            WsUtils.remove(webSocketSession);
            log.error("服务端主动关闭连接：用户{}信息：{}", userId, msg);
        }
    }


    /**
     * 通过userId获取WebSocketSession
     *
     * @param userId
     * @return
     */
    public static WebSocketSession getWebSocketSession(String userId) {
        return webSocketSessionMap.get(userId);
    }

    /**
     * 获取用户的session
     *
     * @param session
     * @return
     */
    public static String getUserId(WebSocketSession session) {
        return (String) session.getAttributes().get("userId");
    }

    /**
     * 获取用户的ip
     *
     * @param session
     * @return
     */
    public static String getIp(WebSocketSession session) {
        return (String) session.getAttributes().get("ip");
    }


    /**
     * 判断链接是否存在
     *
     * @param userId
     * @return
     */
    public static boolean contains(String userId) {
        return WsUtils.webSocketSessionMap.containsKey(userId);
    }


    /**
     * 判断链接是否存在
     *
     * @param webSocketSession
     * @return
     */
    public static boolean contains(WebSocketSession webSocketSession) {
        return WsUtils.webSocketSessionMap.containsKey(getUserId(webSocketSession));
    }


    /**
     * 获取所有的用户连接的用户id
     *
     * @return List<String>
     */
    public static List<String> getUserList() {
        return Collections.list(webSocketSessionMap.keys());
    }


    /**
     * 发送消息给指定用户
     *
     * @param userId  对象id
     * @param message 消息
     * @throws IOException IO
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
     * 发送消息给指定用户
     *
     * @param userId  对象id
     * @param message 消息
     * @throws IOException IO
     */
    public static void sendToUser(String userId, Msg message) {
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
     * @param message 消息
     * @throws IOException IO
     */
    public static void sendToAll(String message) {
        getUserList().forEach(userId -> sendToUser(userId, message));
    }

    /**
     * 发送消息全部在线用户
     *
     * @param message 消息
     * @throws IOException IO
     */
    public static void sendToAll(Msg message) {
        getUserList().forEach(userId -> sendToUser(userId, JSONObject.toJSONString(message)));
    }

    /**
     * 发送通过webSocketSession发送消息
     *
     * @param webSocketSession 对象id
     * @param message          消息
     * @throws IOException IO
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
     * 发送通过webSocketSession发送消息
     *
     * @param webSocketSession 对象id
     * @param message          消息
     * @throws IOException IO
     */
    public static void send(WebSocketSession webSocketSession, Msg message) {

        if (webSocketSession == null || !webSocketSession.isOpen()) {
            log.warn("连接对象【{}】已关闭，无法送消息：{}", webSocketSession.getId(), JSONObject.toJSONString(message));
        } else {
            try {
                webSocketSession.sendMessage(new TextMessage(JSONObject.toJSONString(message)));
            } catch (IOException e) {
                log.error("发送消息失败：{}", e.getMessage(), e);
            }
            log.info("sendMessage：向{}发送消息：{}", webSocketSession.getId(), JSONObject.toJSONString(message));
        }
    }


}
