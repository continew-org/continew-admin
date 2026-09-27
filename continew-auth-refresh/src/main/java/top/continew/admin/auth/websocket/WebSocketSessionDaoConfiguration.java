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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.continew.starter.messaging.websocket.dao.WebSocketSessionDao;

/**
 * WebSocket 会话 DAO 配置。
 *
 * <p>用多标签页实现替换 Starter 的 {@code @ConditionalOnMissingBean} 默认 DAO：
 * 同一 Access Token 的多条连接互不覆盖，撤销时可全量关闭。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Configuration
public class WebSocketSessionDaoConfiguration {

    @Bean
    public WebSocketSessionDao webSocketSessionDao() {
        return new ConcurrentWebSocketSessionDao();
    }
}
