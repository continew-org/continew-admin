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

package top.continew.admin.common.handler;

/**
 * Created by WeiRan on 2024.03.13 16:41
 */

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import top.continew.admin.common.util.WsUtils;

import java.io.IOException;

/**
 * @author zhong
 *         webscoket 处理器
 */
@Component
@Slf4j
public class MyWebSocketHandler extends TextWebSocketHandler {

    /**
     * 收到客户端消息时触发的回调
     *
     * @param session 连接对象
     * @param message 消息体
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        log.info("接受到会话【{}】的消息：{}", session.getId(), message.getPayload());
        String jsonPayload = message.getPayload();
        if (StrUtil.isBlank(jsonPayload)) {
            log.error("接收到空消息");
            return;
        }
        try {
            //业务逻辑处理
            // WsUtils.send(session, "我收到了你的消息");
        } catch (Exception e) {
            log.error("WebSocket消息解析失败：{}", e.getMessage(), e);
            WsUtils.close(session, "消息解析失败：" + e.getMessage());
        }
    }

    /**
     * 建立连接后触发的回调
     *
     * @param session 连接对象
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = String.valueOf(session.getAttributes().get("userId"));
        // 将新连接添加
        WsUtils.bindUser(userId, session);
        //在线数加1
        WsUtils.onlineCount.incrementAndGet();
        log.info("与用户【{}】建立了连接 当前在线人数【{}】", userId, WsUtils.onlineCount.get());
        log.info("attributes:{}", session.getAttributes());

    }

    /**
     * 断开连接后触发的回调
     *
     * @param session 连接对象
     * @param status  状态
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 关闭连接
        WsUtils.close(session, "断开连接后触发的回调");
        log.info("用户【{}】断开连接,status:{},当前剩余在线人数【{}】", WsUtils.getUserId(session), status.getCode(), WsUtils.onlineCount
            .get());
    }

    /**
     * 传输消息出错时触发的回调
     *
     * @param session   连接对象
     * @param exception 异常
     * @throws Exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("用户【{}】发生错误,exception:{}", session.getId(), exception.getMessage());
        // 如果发送异常，则断开连接
        if (session.isOpen()) {
            WsUtils.close(session, "传输消息出错时触发的回调");
        }
    }
}
