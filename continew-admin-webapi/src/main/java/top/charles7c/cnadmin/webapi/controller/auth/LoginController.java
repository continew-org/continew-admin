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

import top.charles7c.cnadmin.auth.model.request.LoginRequest;
import top.charles7c.cnadmin.auth.model.vo.LoginVO;
import top.charles7c.cnadmin.auth.model.vo.RouteVO;
import top.charles7c.cnadmin.auth.model.vo.UserInfoVO;
import top.charles7c.cnadmin.auth.service.LoginService;
import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.RedisUtils;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.ValidationUtils;
import top.charles7c.cnadmin.system.model.vo.UserDetailVO;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 登录 API
 *
 * @author Charles7c
 * @since 2022/12/21 20:37
 */
@Tag(name = "登录 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {

    private final LoginService loginService;
    private final UserService userService;

    @SaIgnore
    @Operation(summary = "用户登录", description = "根据用户名和密码进行登录认证")
    @PostMapping("/login")
    public R<LoginVO> login(@Validated @RequestBody LoginRequest loginRequest) {
        // 校验验证码
        String captchaKey = RedisUtils.formatKey(CacheConsts.CAPTCHA_KEY_PREFIX, loginRequest.getUuid());
        String captcha = RedisUtils.getCacheObject(captchaKey);
        ValidationUtils.throwIfBlank(captcha, "验证码已失效");
        RedisUtils.deleteCacheObject(captchaKey);
        ValidationUtils.throwIfNotEqualIgnoreCase(loginRequest.getCaptcha(), captcha, "验证码错误");

        // 用户登录
        String rawPassword =
            ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(loginRequest.getPassword()));
        ValidationUtils.throwIfBlank(rawPassword, "密码解密失败");
        String token = loginService.login(loginRequest.getUsername(), rawPassword);
        return R.ok(new LoginVO().setToken(token));
    }

    @SaIgnore
    @Operation(summary = "用户退出", description = "注销用户的当前登录")
    @Parameter(name = "Authorization", description = "令牌", required = true, example = "Bearer xxxx-xxxx-xxxx-xxxx",
        in = ParameterIn.HEADER)
    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }

    @Operation(summary = "获取用户信息", description = "获取登录用户信息")
    @GetMapping("/user/info")
    public R<UserInfoVO> getUserInfo() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        UserDetailVO userDetailVO = userService.get(loginUser.getId());
        UserInfoVO userInfoVO = BeanUtil.copyProperties(userDetailVO, UserInfoVO.class);
        userInfoVO.setPermissions(loginUser.getPermissions());
        userInfoVO.setRoles(loginUser.getRoles());
        return R.ok(userInfoVO);
    }

    @Operation(summary = "获取路由信息", description = "获取登录用户的路由信息")
    @GetMapping("/route")
    public R<List<RouteVO>> listMenu() {
        Long userId = LoginHelper.getUserId();
        List<RouteVO> routeTree = loginService.buildRouteTree(userId);
        return R.ok(routeTree);
    }
}