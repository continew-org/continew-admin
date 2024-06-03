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

package top.continew.admin.common.util.helper;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.model.dto.LoginUser;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.util.IpUtils;
import top.continew.starter.extension.crud.service.CommonUserService;
import top.continew.starter.web.util.ServletUtils;

import java.time.LocalDateTime;

/**
 * 登录助手
 *
 * @author Charles7c
 * @author Lion Li（<a href="https://gitee.com/dromara/RuoYi-Vue-Plus">RuoYi-Vue-Plus</a>）
 * @since 2022/12/24 12:58
 */
public class LoginHelper {

    private LoginHelper() {
    }

    /**
     * 用户登录并缓存用户信息
     *
     * @param loginUser 登录用户信息
     * @return 令牌
     */
    public static String login(LoginUser loginUser) {
        // 记录登录信息
        HttpServletRequest request = ServletUtils.getRequest();
        loginUser.setIp(JakartaServletUtil.getClientIP(request));
        loginUser.setAddress(IpUtils.getAddress(loginUser.getIp()));
        loginUser.setBrowser(ServletUtils.getBrowser(request));
        loginUser.setLoginTime(LocalDateTime.now());
        loginUser.setOs(StrUtil.subBefore(ServletUtils.getOs(request), " or", false));
        // 登录并缓存用户信息
        StpUtil.login(loginUser.getId());
        SaHolder.getStorage().set(CacheConstants.LOGIN_USER_KEY, loginUser);
        String tokenValue = StpUtil.getTokenValue();
        loginUser.setToken(tokenValue);
        StpUtil.getTokenSession().set(CacheConstants.LOGIN_USER_KEY, loginUser);
        return tokenValue;
    }

    /**
     * 获取登录用户信息
     *
     * @return 登录用户信息
     * @throws NotLoginException 未登录异常
     */
    public static LoginUser getLoginUser() throws NotLoginException {
        StpUtil.checkLogin();
        LoginUser loginUser = (LoginUser)SaHolder.getStorage().get(CacheConstants.LOGIN_USER_KEY);
        if (null != loginUser) {
            return loginUser;
        }
        SaSession tokenSession = StpUtil.getTokenSession();
        loginUser = (LoginUser)tokenSession.get(CacheConstants.LOGIN_USER_KEY);
        SaHolder.getStorage().set(CacheConstants.LOGIN_USER_KEY, loginUser);
        return loginUser;
    }

    /**
     * 根据 Token 获取登录用户信息
     *
     * @param token 用户 Token
     * @return 登录用户信息
     */
    public static LoginUser getLoginUser(String token) {
        SaSession tokenSession = StpUtil.getStpLogic().getTokenSessionByToken(token, false);
        if (null == tokenSession) {
            return null;
        }
        return (LoginUser)tokenSession.get(CacheConstants.LOGIN_USER_KEY);
    }

    /**
     * 获取登录用户 ID
     *
     * @return 登录用户 ID
     */
    public static Long getUserId() {
        return getLoginUser().getId();
    }

    /**
     * 获取登录用户名
     *
     * @return 登录用户名
     */
    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    /**
     * 获取登录用户昵称
     *
     * @return 登录用户昵称
     */
    public static String getNickname() {
        return getNickname(getUserId());
    }

    /**
     * 获取登录用户昵称
     *
     * @param userId 登录用户 ID
     * @return 登录用户昵称
     */
    public static String getNickname(Long userId) {
        return ExceptionUtils.exToNull(() -> SpringUtil.getBean(CommonUserService.class).getNicknameById(userId));
    }
}
