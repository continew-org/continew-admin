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
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.model.resp.RouteResp;
import top.continew.admin.auth.model.resp.SocialAuthAuthorizeResp;
import top.continew.admin.auth.model.resp.UserInfoResp;
import top.continew.admin.auth.service.AuthService;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.enums.SocialSourceEnum;
import top.continew.admin.system.model.resp.user.UserDetailResp;
import top.continew.admin.system.service.UserService;
import top.continew.starter.auth.justauth.AuthRequestFactory;
import top.continew.starter.auth.refresh.token.api.AuthSessionConstants;
import top.continew.starter.auth.refresh.token.model.SessionView;
import top.continew.starter.auth.refresh.token.service.SessionQueryService;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.log.annotation.Log;
import top.continew.starter.log.enums.Include;
import top.continew.starter.validation.constraints.EnumValue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证 API
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Tag(name = "认证 API")
@Log(module = "登录")
@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final ObjectProvider<SessionQueryService> sessionQueryServiceProvider;
    // 第三方登录为可选能力，未启用时 Starter 不注册 AuthRequestFactory，故惰性获取
    private final ObjectProvider<AuthRequestFactory> authRequestFactoryProvider;

    /**
     * 用户登录
     *
     * @param req  登录信息
     * @param request  请求对象
     * @param response 响应对象
     * @return 登录结果
     */
    @SaIgnore
    @Operation(summary = "登录", description = "用户登录")
    @Log(excludes = {Include.REQUEST_HEADERS, Include.RESPONSE_HEADERS, Include.RESPONSE_BODY})
    @PostMapping("/login")
    public LoginResp login(@RequestBody @Valid LoginReq req, HttpServletRequest request,
        HttpServletResponse response) {
        return authService.login(req, request, response);
    }

    /**
     * 三方账号登录授权
     *
     * @param source 第三方平台来源
     * @return 授权地址响应
     */
    @SaIgnore
    @Operation(summary = "三方账号登录授权", description = "三方账号登录授权")
    @Parameter(name = "source", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @GetMapping("/{source}")
    public SocialAuthAuthorizeResp authorize(@PathVariable @EnumValue(
        value = SocialSourceEnum.class, message = "第三方平台无效") String source) {
        AuthRequestFactory authRequestFactory = authRequestFactoryProvider.getIfAvailable();
        ValidationUtils.throwIf(authRequestFactory == null, "暂未启用第三方登录");
        AuthRequest authRequest = authRequestFactory.getAuthRequest(source);
        return SocialAuthAuthorizeResp.builder()
            .authorizeUrl(authRequest.authorize(AuthStateUtils.createState()))
            .build();
    }

    /**
     * 获取登录用户信息
     *
     * @return 用户信息（含权限列表）
     */
    @Log(ignore = true)
    @Operation(summary = "获取用户信息", description = "获取登录用户信息")
    @GetMapping("/user/info")
    public UserInfoResp getUserInfo() {
        UserContext userContext = UserContextHolder.getContext();
        UserDetailResp userDetailResp = userService.get(userContext.getId());
        UserInfoResp userInfoResp =
            BeanUtil.copyProperties(userDetailResp, UserInfoResp.class);
        userInfoResp.setPermissions(userContext.getPermissions());
        userInfoResp.setRoles(userContext.getRoleCodes());
        // 两个字段同源推导，避免各自取 now 落在到期点两侧时出现「未过期 + 剩余 0 天」
        Integer pwdExpireDays = userContext.getPwdExpireDays();
        userInfoResp.setPwdExpired(pwdExpireDays != null && pwdExpireDays == 0);
        userInfoResp.setPwdExpireDays(pwdExpireDays);
        userInfoResp.setPwdWarningDays(userContext.getPasswordExpirationWarningDays());
        this.fillCurrentSession(userInfoResp);
        return userInfoResp;
    }

    @Log(ignore = true)
    @Operation(summary = "获取路由信息", description = "获取登录用户的路由信息")
    @GetMapping("/user/route")
    public List<RouteResp> listRoute() {
        return authService.buildRouteTree(UserContextHolder.getUserId());
    }

    /**
     * 填充本次会话的登录 IP 与时间，只用于欢迎区展示。
     *
     * <p>会话查询的每个环节都降级：服务未注册、SID 缺失或查询失败时保留已读取的用户资料与权限，
     * 两个展示字段留空。日志不记录 SID 与异常原文，避免把可关联登录会话的标识或底层敏感信息写入。</p>
     *
     * @param userInfoResp 待填充的用户信息
     */
    private void fillCurrentSession(UserInfoResp userInfoResp) {
        try {
            SessionQueryService sessionQueryService = sessionQueryServiceProvider.getIfAvailable();
            if (sessionQueryService == null) {
                return;
            }
            String sessionId = Convert.toStr(StpUtil.getExtra(StpUtil.getTokenValue(),
                AuthSessionConstants.SESSION_ID_CLAIM));
            if (StrUtil.isBlank(sessionId)) {
                return;
            }
            SessionView session = sessionQueryService.getSession(sessionId);
            if (session != null) {
                userInfoResp.setLoginIp(session.getIp());
                if (session.getCreatedAt() > 0) {
                    // 与其余业务时间保持同一时区，UTC 容器下才不会比登录时间早八小时。
                    userInfoResp.setLoginTime(
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getCreatedAt()),
                            GlobalConstants.DEFAULT_ZONE_ID));
                }
            }
        } catch (Exception e) {
            // 异常原文由令牌解析器或会话存储提供，可能含令牌或带 SID 的存储键，只记录类型。
            log.warn("读取当前会话登录信息失败：{}", e.getClass().getSimpleName());
        }
    }
}
