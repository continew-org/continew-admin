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
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;

import java.util.function.Function;

/**
 * Refresh Token 会话服务。
 *
 * @author luoqiz
 */
public interface RefreshTokenService {

    /** 当前全局及客户端是否开启 Refresh Token。 */
    boolean isEnabled(ClientResp client);

    /** 获取客户端 Refresh Token 的绝对有效期（秒）。 */
    long getRefreshTimeout(ClientResp client);

    /** 获取客户端 Refresh Token 的传输模式。 */
    RefreshTokenModeEnum getMode(ClientResp client);

    /**
     * 创建登录会话的 Refresh Token。
     *
     * @return BODY 模式需要返回给客户端的明文 Token；COOKIE 模式返回值仅供内部使用
     */
    String issue(UserContext userContext, ClientResp client, HttpServletRequest request,
        HttpServletResponse response, String accessToken);

    /**
     * 原子轮换 Refresh Token。
     *
     * @param rawRefreshToken 客户端提交的明文 Refresh Token
     * @param request         当前请求
     * @param response        当前响应
     * @param accessTokenIssuer 根据旧会话重新签发 Access Token 的函数
     * @return 登录响应
     */
    LoginResp rotate(String rawRefreshToken, HttpServletRequest request,
        HttpServletResponse response, Function<RefreshSession, LoginResp> accessTokenIssuer);

    /** 从 Cookie 或未来 BODY 模式中读取 Refresh Token */
    String resolve(String bodyRefreshToken, HttpServletRequest request);

    /** 撤销当前 Access Token 对应的 Refresh Session */
    void revokeCurrent(String accessToken, String refreshToken);

    /** 撤销用户的全部 Refresh Session */
    void revokeByUser(Long userId);

    /** 撤销租户的全部 Refresh Session */
    void revokeByTenant(Long tenantId);

    /** 撤销客户端的全部 Refresh Session */
    void revokeByClient(String clientId);

    /** 撤销指定 Access Token 所属的 Refresh Session */
    void revokeByAccessToken(String accessToken);

    /** 清理浏览器 Refresh Token Cookie */
    void clearCookie(HttpServletResponse response);
}
