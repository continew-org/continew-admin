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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import top.continew.admin.common.model.dto.LoginUser;
import top.continew.admin.common.util.helper.LoginHelper;
import top.continew.starter.messaging.websocket.core.CurrentUserProvider;
import top.continew.starter.messaging.websocket.model.CurrentUser;

/**
 * 当前登录用户 Provider
 *
 * @author Charles7c
 * @since 2024/6/4 22:13
 */
@Component
public class CurrentUserProviderImpl implements CurrentUserProvider {

    @Override
    public CurrentUser getCurrentUser(ServletServerHttpRequest request) {
        HttpServletRequest servletRequest = request.getServletRequest();
        String token = servletRequest.getParameter("token");
        LoginUser loginUser = LoginHelper.getLoginUser(token);
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(loginUser.getToken());
        return currentUser;
    }
}
