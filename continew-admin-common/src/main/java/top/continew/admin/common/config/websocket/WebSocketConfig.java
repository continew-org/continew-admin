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

package top.continew.admin.common.config.websocket;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import top.continew.admin.common.handler.MyWebSocketHandler;

/**
 * WebSocketConfig配置
 *
 * @author WeiRan
 * @since 2024.03.13 16:45
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private WebsocketInterceptor customWebsocketInterceptor;

    @Resource
    private MyWebSocketHandler myWebSocketHandler;

    /**
     * 注册WebSocket处理程序并设置必要的配置。
     *
     * @param registry 用于注册处理程序的WebSocketHandlerRegistry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            // 设置处理器处理/custom/**
            .addHandler(myWebSocketHandler, "/ws")
            // 允许跨越
            .setAllowedOrigins("*")
            // 设置监听器
            .addInterceptors(customWebsocketInterceptor);
    }

}
