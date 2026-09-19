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

package top.continew.admin.auth.adapter;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.model.RefreshSession;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.AuthTokenService;
import top.continew.admin.auth.service.RefreshAccessTokenIssuer;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.auth.exception.RefreshTokenException;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.concurrent.atomic.AtomicReference;

/** system 对刷新主体状态和 Access Token 签发的适配实现。 */
@Component
@RequiredArgsConstructor
public class RefreshAccessTokenIssuerImpl implements RefreshAccessTokenIssuer {

    private final AuthTokenService authTokenService;
    private final ClientService clientService;
    private final TenantApi tenantApi;
    private final UserService userService;

    @Override
    public LoginResp issue(RefreshSession session, HttpServletRequest request,
        HttpServletResponse response) {
        ClientResp client = clientService.getByClientId(session.getClientId());
        if (client == null || DisEnableStatusEnum.DISABLE.equals(client.getStatus())) {
            throw RefreshTokenException.unauthorized("登录状态已失效，请重新登录");
        }
        try {
            tenantApi.checkStatus(session.getTenantId());
        } catch (BusinessException e) {
            throw RefreshTokenException.unauthorized(e.getMessage());
        }
        AtomicReference<UserDO> userReference = new AtomicReference<>();
        TenantUtils.execute(session.getTenantId(),
            () -> userReference.set(userService.getById(session.getUserId())));
        if (userReference.get() == null || StrUtil.isBlank(userReference.get().getUsername())) {
            throw RefreshTokenException.unauthorized("登录状态已失效，请重新登录");
        }
        return authTokenService.issueAccessToken(userReference.get(), client, session.getTenantId(),
            session, request, response);
    }
}
