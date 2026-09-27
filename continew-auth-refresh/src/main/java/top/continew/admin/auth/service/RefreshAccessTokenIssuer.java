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

package top.continew.admin.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import top.continew.admin.auth.model.RefreshSession;
import top.continew.admin.auth.model.resp.LoginResp;

/**
 * 刷新时重新装载主体状态并签发 Access Token 的业务适配点。
 *
 * <p>认证会话模块不依赖用户、客户端和租户实体；system 模块实现该接口，确保刷新时
 * 使用最新权限和业务状态。</p>
 *
 * @author luoqiz
 */
public interface RefreshAccessTokenIssuer {

    LoginResp issue(RefreshSession session, HttpServletRequest request,
        HttpServletResponse response);
}
