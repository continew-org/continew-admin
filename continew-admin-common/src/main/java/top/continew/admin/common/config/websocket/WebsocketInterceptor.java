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
 * 用来处理webscocket拦截器
 *
 * @author WeiRan
 * @since 2024.03.13 16:45
 */
@Component
@Slf4j
public class WebsocketInterceptor extends HttpSessionHandshakeInterceptor {

    /**
     * 在建立 WebSocket 连接之前处理握手过程。
     *
     * @param request    HTTP 请求对象
     * @param response   HTTP 响应对象
     * @param wsHandler  WebSocket 处理程序
     * @param attributes 用于存储 WebSocket 会话的自定义属性的映射
     * @return 如果握手成功则返回 true，否则返回 false
     * @throws Exception 如果在握手过程中发生错误
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest req = (ServletServerHttpRequest)request;
        ServletServerHttpResponse res = (ServletServerHttpResponse)response;

        HttpServletRequest httpServletRequest = ServletUtils.getRequest();
        String ip = JakartaServletUtil.getClientIP(httpServletRequest);
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

        // 鉴权: 如果返回 false 则表示未通过
        // response.setStatusCode(HttpStatus.UNAUTHORIZED);
        //返回 false;
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
     * WebSocket 握手成功完成后调用此方法。
     * 它记录一条消息指示连接已建立，然后调用
     * 方法的超类实现。
     *
     * @param request   表示传入 HTTP 请求的 ServerHttpRequest 对象
     * @param response  表示传出 HTTP 响应的 ServerHttpResponse 对象
     * @param wsHandler 将处理 WebSocket 会话的 WebSocketHandler 对象
     * @param exception 在握手过程中发生的异常，如果没有异常则为 null
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
