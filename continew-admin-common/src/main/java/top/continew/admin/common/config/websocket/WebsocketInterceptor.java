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

/**
 * Created by WeiRan on 2024.03.13 16:43
 */

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import top.continew.admin.common.model.dto.LoginUser;
import top.continew.admin.common.util.helper.LoginHelper;
import top.continew.starter.web.util.ServletUtils;

import java.util.Map;

/**
 * @author zhong
 *         用来处理webscocket拦截器
 */
@Component
@Slf4j
public class WebsocketInterceptor extends HttpSessionHandshakeInterceptor {

    /**
     * 建立连接时
     *
     * @param request    the current request
     * @param response   the current response
     * @param wsHandler  the target WebSocket handler
     * @param attributes the attributes from the HTTP handshake to associate with the WebSocket
     *                   session; the provided attributes are copied, the original map is not used.
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest req = (ServletServerHttpRequest)request;
        ServletServerHttpResponse res = (ServletServerHttpResponse)response;

        HttpServletRequest httpServletRequest = ServletUtils.getRequest();
        String ip=JakartaServletUtil.getClientIP(httpServletRequest);
        String token = req.getServletRequest().getParameter("token");
        log.info("开始建立连接....token:{}", token);
        log.info("attributes:{}", attributes);
        if (StrUtil.isBlank(token)) {
            res.setStatusCode(HttpStatus.UNAUTHORIZED);
            res.getServletResponse().setContentType("application/json");
            String errorMessage = "{\"error\": \"Authentication failed. Please provide valid credentials.\"}";
            res.getBody().write(errorMessage.getBytes());
            return false;
        }
        /**
         * 鉴权: return false 不通过
         * response.setStatusCode(HttpStatus.UNAUTHORIZED);
         * return false;
         */
        LoginUser loginUser = LoginHelper.getLoginUser(token);
        if (loginUser == null) {
            res.setStatusCode(HttpStatus.UNAUTHORIZED);
            res.getServletResponse().setContentType("application/json");
            String errorMessage = "{\"error\": \"Authentication failed. Please provide valid credentials.\"}";
            res.getBody().write(errorMessage.getBytes());
            res.close();
            return false;
        }
        attributes.put("userId", String.valueOf(loginUser.getId()));
        attributes.put("ip", ip);
        super.setCreateSession(true);
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    /**
     * 成功建立连接后
     *
     * @param request   the current request
     * @param response  the current response
     * @param wsHandler the target WebSocket handler
     * @param exception an exception raised during the handshake, or {@code null} if none
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        log.info("连接成功....");
        //其他业务代码
        super.afterHandshake(request, response, wsHandler, exception);
    }
}
