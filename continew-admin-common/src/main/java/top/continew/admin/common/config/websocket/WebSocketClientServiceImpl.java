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

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.messaging.websocket.core.WebSocketClientService;

/**
 * 当前登录用户 Provider
 *
 * @author Charles7c
 * @since 2024/6/4 22:13
 */
@Component
public class WebSocketClientServiceImpl implements WebSocketClientService {

    @Override
    public String getClientId(ServletServerHttpRequest request) {
        HttpServletRequest servletRequest = request.getServletRequest();
        String token = servletRequest.getParameter("token");
        if (null == StpUtil.getLoginIdByToken(token)) {
            throw new BusinessException("登录已过期，请重新登录");
        }
        return token;
    }
}
