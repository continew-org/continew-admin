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

package top.charles7c.cnadmin.webapi.controller.auth;

import java.util.List;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;

import top.charles7c.cnadmin.auth.model.req.AccountLoginReq;
import top.charles7c.cnadmin.auth.model.req.EmailLoginReq;
import top.charles7c.cnadmin.auth.model.req.PhoneLoginReq;
import top.charles7c.cnadmin.auth.model.resp.LoginResp;
import top.charles7c.cnadmin.auth.model.resp.RouteResp;
import top.charles7c.cnadmin.auth.model.resp.UserInfoResp;
import top.charles7c.cnadmin.auth.service.LoginService;
import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.model.resp.R;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.RedisUtils;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.ValidationUtils;
import top.charles7c.cnadmin.monitor.annotation.Log;
import top.charles7c.cnadmin.system.model.resp.UserDetailResp;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 认证 API
 *
 * @author Charles7c
 * @since 2022/12/21 20:37
 */
@Log(module = "登录")
@Tag(name = "认证 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final LoginService loginService;
    private final UserService userService;

    @SaIgnore
    @Operation(summary = "账号登录", description = "根据账号和密码进行登录认证")
    @PostMapping("/account")
    public R<LoginResp> accountLogin(@Validated @RequestBody AccountLoginReq loginReq) {
        String captchaKey = RedisUtils.formatKey(CacheConsts.CAPTCHA_KEY_PREFIX, loginReq.getUuid());
        String captcha = RedisUtils.getCacheObject(captchaKey);
        ValidationUtils.throwIfBlank(captcha, "验证码已失效");
        RedisUtils.deleteCacheObject(captchaKey);
        ValidationUtils.throwIfNotEqualIgnoreCase(loginReq.getCaptcha(), captcha, "验证码错误");
        // 用户登录
        String rawPassword = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(loginReq.getPassword()));
        ValidationUtils.throwIfBlank(rawPassword, "密码解密失败");
        String token = loginService.accountLogin(loginReq.getUsername(), rawPassword);
        return R.ok(LoginResp.builder().token(token).build());
    }

    @SaIgnore
    @Operation(summary = "邮箱登录", description = "根据邮箱和验证码进行登录认证")
    @PostMapping("/email")
    public R<LoginResp> emailLogin(@Validated @RequestBody EmailLoginReq loginReq) {
        String email = loginReq.getEmail();
        String captchaKey = RedisUtils.formatKey(CacheConsts.CAPTCHA_KEY_PREFIX, email);
        String captcha = RedisUtils.getCacheObject(captchaKey);
        ValidationUtils.throwIfBlank(captcha, "验证码已失效");
        ValidationUtils.throwIfNotEqualIgnoreCase(loginReq.getCaptcha(), captcha, "验证码错误");
        RedisUtils.deleteCacheObject(captchaKey);
        String token = loginService.emailLogin(email);
        return R.ok(LoginResp.builder().token(token).build());
    }

    @SaIgnore
    @Operation(summary = "手机号登录", description = "根据手机号和验证码进行登录认证")
    @PostMapping("/phone")
    public R<LoginResp> phoneLogin(@Validated @RequestBody PhoneLoginReq loginReq) {
        String phone = loginReq.getPhone();
        String captchaKey = RedisUtils.formatKey(CacheConsts.CAPTCHA_KEY_PREFIX, phone);
        String captcha = RedisUtils.getCacheObject(captchaKey);
        ValidationUtils.throwIfBlank(captcha, "验证码已失效");
        ValidationUtils.throwIfNotEqualIgnoreCase(loginReq.getCaptcha(), captcha, "验证码错误");
        RedisUtils.deleteCacheObject(captchaKey);
        String token = loginService.phoneLogin(phone);
        return R.ok(LoginResp.builder().token(token).build());
    }

    @Operation(summary = "用户退出", description = "注销用户的当前登录")
    @Parameter(name = "Authorization", description = "令牌", required = true, example = "Bearer xxxx-xxxx-xxxx-xxxx",
        in = ParameterIn.HEADER)
    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }

    @Log(ignore = true)
    @Operation(summary = "获取用户信息", description = "获取登录用户信息")
    @GetMapping("/user/info")
    public R<UserInfoResp> getUserInfo() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        UserDetailResp userDetailResp = userService.get(loginUser.getId());
        UserInfoResp userInfoResp = BeanUtil.copyProperties(userDetailResp, UserInfoResp.class);
        userInfoResp.setPermissions(loginUser.getPermissions());
        userInfoResp.setRoles(loginUser.getRoleCodes());
        return R.ok(userInfoResp);
    }

    @Log(ignore = true)
    @Operation(summary = "获取路由信息", description = "获取登录用户的路由信息")
    @GetMapping("/route")
    public R<List<RouteResp>> listRoute() {
        Long userId = LoginHelper.getUserId();
        return R.ok(loginService.buildRouteTree(userId));
    }
}