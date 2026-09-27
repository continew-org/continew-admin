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

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.model.req.LoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.AuthTokenService;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.util.validation.Validator;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

/**
 * 登录处理器基类
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/22 14:52
 */
@Component
public abstract class AbstractLoginHandler<T extends LoginReq> implements LoginHandler<T> {

    @Resource
    protected OptionService optionService;
    @Resource
    protected UserService userService;
    @Resource
    protected RoleService roleService;
    @Resource
    protected AuthTokenService authTokenService;

    protected static final String CAPTCHA_EXPIRED = "验证码已失效";
    protected static final String CAPTCHA_ERROR = "验证码不正确";

    @Override
    public void preLogin(T req, ClientResp client, HttpServletRequest request) {
        // 参数校验
        Validator.validate(req);
    }

    @Override
    public void postLogin(T req, ClientResp client, HttpServletRequest request) {
    }

    /**
     * 认证
     *
     * @param user     用户信息
     * @param client   客户端信息
     * @param request  请求对象
     * @param response 响应对象
     * @return 登录响应参数
     */
    protected LoginResp authenticate(UserDO user, ClientResp client, HttpServletRequest request,
        HttpServletResponse response) {
        // 所有登录方式共用一套令牌签发流程，避免某种登录方式遗漏 Refresh Token。
        return authTokenService.issue(user, client, TenantContextHolder.getTenantId(), request,
            response);
    }

}
