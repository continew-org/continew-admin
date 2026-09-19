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

package top.continew.admin.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.auth.model.req.RefreshTokenReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.RefreshAccessTokenIssuer;
import top.continew.admin.auth.service.RefreshTokenService;
import top.continew.admin.auth.exception.RefreshTokenException;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;
import top.continew.starter.log.annotation.Log;
import top.continew.starter.log.enums.Include;

/** Refresh Token 会话 API。 */
@Tag(name = "认证会话 API")
@Log(module = "认证会话")
@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class SessionController {

    private final RefreshAccessTokenIssuer refreshAccessTokenIssuer;
    private final RefreshTokenService refreshTokenService;

    /** 使用 Refresh Token 轮换 Access Token。 */
    @SaIgnore
    @TenantIgnore
    @Operation(summary = "刷新令牌", description = "使用 Refresh Token 轮换 Access Token")
    @Log(excludes = {Include.REQUEST_HEADERS, Include.REQUEST_BODY, Include.RESPONSE_HEADERS,
        Include.RESPONSE_BODY})
    @PostMapping("/refresh")
    public LoginResp refresh(@RequestBody(required = false) @Valid RefreshTokenReq req,
        HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = refreshTokenService.resolve(req == null ? null : req
            .getRefreshToken(), request);
        refreshTokenService.checkRequestRateLimit(request);
        refreshTokenService.validateRequest(rawRefreshToken, request);
        return refreshTokenService.rotate(rawRefreshToken, response,
            session -> refreshAccessTokenIssuer.issue(session, request, response));
    }

    /** 注销当前认证会话。 */
    @SaIgnore
    @TenantIgnore
    @Operation(summary = "登出", description = "注销用户的当前登录")
    @Log(excludes = {Include.REQUEST_HEADERS, Include.REQUEST_BODY, Include.RESPONSE_HEADERS})
    @Parameter(name = "Authorization", description = "令牌", required = true,
        example = "Bearer xxxx-xxxx-xxxx-xxxx", in = ParameterIn.HEADER)
    @PostMapping("/logout")
    public Object logout(@RequestBody(required = false) RefreshTokenReq req,
        HttpServletRequest request, HttpServletResponse response) {
        Object loginId = StpUtil.getLoginId(-1L);
        String accessToken = StpUtil.getTokenValue();
        refreshTokenService.validateCookieOrigin(request);
        String refreshToken;
        try {
            refreshToken = refreshTokenService.resolve(req == null ? null : req.getRefreshToken(),
                request);
            refreshTokenService.validateRequest(refreshToken, request);
        } catch (RefreshTokenException e) {
            refreshTokenService.clearCookie(response);
            throw e;
        }
        refreshTokenService.revokeCurrent(accessToken, refreshToken);
        try {
            if (accessToken != null) {
                StpUtil.logoutByTokenValue(accessToken);
            }
        } catch (Exception e) {
            log.debug("当前 Access Token 已无需注销", e);
        }
        refreshTokenService.clearCookie(response);
        return loginId;
    }
}
