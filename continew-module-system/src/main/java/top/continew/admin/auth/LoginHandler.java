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

package top.continew.admin.auth;

import jakarta.servlet.http.HttpServletRequest;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.system.model.resp.ClientResp;

/**
 * 登录处理器
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/22 14:52
 */
public interface LoginHandler<T extends LoginReq> {

    /**
     * 登录
     *
     * @param req     登录请求参数
     * @param client  客户端信息
     * @param request 请求对象
     * @return 登录响应参数
     */
    LoginResp login(T req, ClientResp client, HttpServletRequest request);

    /**
     * 登录前置处理
     *
     * @param req     登录请求参数
     * @param client  客户端信息
     * @param request 请求对象
     */
    void preLogin(T req, ClientResp client, HttpServletRequest request);

    /**
     * 登录后置处理
     *
     * @param req     登录请求参数
     * @param client  客户端信息
     * @param request 请求对象
     */
    void postLogin(T req, ClientResp client, HttpServletRequest request);

    /**
     * 获取认证类型
     *
     * @return 认证类型
     */
    AuthTypeEnum getAuthType();
}