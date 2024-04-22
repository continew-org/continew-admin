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

package top.continew.admin.webapi.auth;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.xkcoding.justauth.AuthRequestFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.web.bind.annotation.*;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.model.resp.SocialAuthAuthorizeResp;
import top.continew.admin.auth.service.LoginService;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.util.validate.ValidationUtils;
import top.continew.starter.log.core.annotation.Log;
import top.continew.starter.web.model.R;

/**
 * 三方账号认证 API
 *
 * @author Charles7c
 * @since 2023/10/8 22:52
 */
@Log(module = "登录")
@Tag(name = "三方账号认证 API")
@SaIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class SocialAuthController {

    private final LoginService loginService;
    private final AuthRequestFactory authRequestFactory;

    @Operation(summary = "三方账号登录授权", description = "三方账号登录授权")
    @Parameter(name = "source", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @GetMapping("/{source}")
    public R<SocialAuthAuthorizeResp> authorize(@PathVariable String source) {
        AuthRequest authRequest = this.getAuthRequest(source);
        return R.ok(SocialAuthAuthorizeResp.builder()
            .authorizeUrl(authRequest.authorize(AuthStateUtils.createState()))
            .build());
    }

    @Operation(summary = "三方账号登录", description = "三方账号登录")
    @Parameter(name = "source", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @PostMapping("/{source}")
    public R<LoginResp> login(@PathVariable String source, @RequestBody AuthCallback callback) {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        AuthRequest authRequest = this.getAuthRequest(source);
        AuthResponse<AuthUser> response = authRequest.login(callback);
        ValidationUtils.throwIf(!response.ok(), response.getMsg());
        AuthUser authUser = response.getData();
        String token = loginService.socialLogin(authUser);
        return R.ok(LoginResp.builder().token(token).build());
    }

    private AuthRequest getAuthRequest(String source) {
        try {
            return authRequestFactory.get(source);
        } catch (Exception e) {
            throw new BadRequestException("暂不支持 [%s] 平台账号登录".formatted(source));
        }
    }
}