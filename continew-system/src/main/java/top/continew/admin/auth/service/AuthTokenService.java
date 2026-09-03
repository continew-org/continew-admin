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
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;

/**
 * 统一访问令牌签发服务。
 *
 * <p>账号、手机、邮箱、第三方登录以及 Refresh Token 刷新都必须经过此服务，确保
 * Sa-Token 登录参数、用户权限上下文和 Refresh Token 的创建逻辑保持一致。</p>
 *
 * @author luoqiz
 */
public interface AuthTokenService {

    /**
     * 为一次登录或刷新请求签发 Access Token。
     *
     * @param user     用户信息
     * @param client   客户端配置
     * @param tenantId 当前登录确定的租户 ID
     * @param request  HTTP 请求
     * @param response HTTP 响应，用于写入浏览器 Refresh Token Cookie
     * @return 登录令牌响应
     */
    LoginResp issue(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response);

    /**
     * 仅签发 Access Token，不创建新的 Refresh Session。
     *
     * <p>Refresh Token 轮换时必须复用原有 familyId，由 RefreshTokenService 负责生成并保存
     * 新的 Refresh Token；如果这里再次创建登录会话，会产生脱离原 Token 链的孤立会话。</p>
     *
     * @param user     用户信息
     * @param client   客户端配置
     * @param tenantId 当前登录确定的租户 ID
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 登录令牌响应（不包含新的 Refresh Token）
     */
    LoginResp issueAccessToken(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response);

}
