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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
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
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.enums.SocialSourceEnum;
import top.continew.admin.system.model.resp.user.UserDetailResp;
import top.continew.admin.system.service.UserService;
import top.continew.starter.auth.justauth.AuthRequestFactory;
import top.continew.starter.log.annotation.Log;
import top.continew.starter.validation.constraints.EnumValue;

import java.util.List;

/**
 * 认证 API
 *
 * @author Charles7c
 * @since 2022/12/21 20:37
 */
@Tag(name = "认证 API")
@Log(module = "登录")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthRequestFactory authRequestFactory;

    /**
     * 用户登录
     *
     * @param req 登录信息
     * @param request 请求对象
     * @return 登录结果
     */
    @SaIgnore
    @Operation(summary = "登录", description = "用户登录")
    @PostMapping("/login")
    public LoginResp login(@RequestBody @Valid LoginReq req, HttpServletRequest request) {
        return authService.login(req, request);
    }

    /**
     * 注销用户的当前登录
     *
     * @return 被登出的用户 ID
     */
    @Operation(summary = "登出", description = "注销用户的当前登录")
    @Parameter(name = "Authorization", description = "令牌", required = true,
        example = "Bearer xxxx-xxxx-xxxx-xxxx", in = ParameterIn.HEADER)
    @PostMapping("/logout")
    public Object logout() {
        Object loginId = StpUtil.getLoginId(-1L);
        StpUtil.logout();
        return loginId;
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
        UserInfoResp userInfoResp = BeanUtil.copyProperties(userDetailResp, UserInfoResp.class);
        userInfoResp.setPermissions(userContext.getPermissions());
        userInfoResp.setRoles(userContext.getRoleCodes());
        userInfoResp.setPwdExpired(userContext.isPasswordExpired());
        return userInfoResp;
    }

    @Log(ignore = true)
    @Operation(summary = "获取路由信息", description = "获取登录用户的路由信息")
    @GetMapping("/user/route")
    public List<RouteResp> listRoute() {
        return authService.buildRouteTree(UserContextHolder.getUserId());
    }
}
