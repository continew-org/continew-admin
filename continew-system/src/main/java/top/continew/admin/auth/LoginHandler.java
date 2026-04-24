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

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.temp.SaTempUtil;
import jakarta.servlet.http.HttpServletRequest;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.resp.DoubleTokenLoginResp;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.model.resp.SingleTokenLoginResp;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

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

    /**
     * 构建登录信息
     *
     * @param loginParameter 登录的参数
     * @param userContext    用户上下文信息
     * @param client         客户端信息
     * @return
     */
    static LoginResp buildLoginResp(SaLoginParameter loginParameter, UserContext userContext, ClientResp client) {
        StpUtil.login(userContext.getId(), loginParameter);
        if (Boolean.TRUE.equals(client.getIsEnableRefreshToken())) {
            // 刷新令牌设置的有效时长
            long refreshExpiresIn = (client.getRefreshTokenTimeout() != null && client.getRefreshTokenTimeout() > 0L)
                    ? client.getRefreshTokenTimeout() : client.getTimeout();
            String refreshToken = SaTempUtil.createToken(userContext.getId(), refreshExpiresIn, false);
            // 将生成的token保存一份，方便刷新token时删除先前的token
            loginParameter.setToken(StpUtil.getTokenValue());
            SaTempUtil.saveToken(refreshToken, loginParameter, refreshExpiresIn);
            return DoubleTokenLoginResp.builder()
                    .accessToken(StpUtil.getTokenValue())
                    .accessExpiresIn(StpUtil.getTokenTimeout())
                    .refreshToken(refreshToken)
                    .refreshExpiresIn(refreshExpiresIn)
                    .tenantId(TenantContextHolder.isTenantEnabled() ? TenantContextHolder.getTenantId() : null)
                    .build();
        } else {
            return SingleTokenLoginResp.builder()
                    .token(StpUtil.getTokenValue())
                    .expiresIn(StpUtil.getTokenTimeout())
                    .tenantId(TenantContextHolder.isTenantEnabled() ? TenantContextHolder.getTenantId() : null)
                    .build();
        }
    }
}