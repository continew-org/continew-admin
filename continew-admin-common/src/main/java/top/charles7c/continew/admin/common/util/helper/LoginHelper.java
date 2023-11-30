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

package top.charles7c.continew.admin.common.util.helper;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;

import top.charles7c.continew.admin.common.constant.CacheConstants;
import top.charles7c.continew.admin.common.model.dto.LogContext;
import top.charles7c.continew.admin.common.model.dto.LoginUser;
import top.charles7c.continew.admin.common.util.ServletUtils;
import top.charles7c.continew.admin.common.util.holder.LogContextHolder;
import top.charles7c.continew.starter.core.util.ExceptionUtils;
import top.charles7c.continew.starter.core.util.IpUtils;
import top.charles7c.continew.starter.extension.crud.base.CommonUserService;

/**
 * 登录助手
 *
 * @author Charles7c
 * @author Lion Li（<a href="https://gitee.com/dromara/RuoYi-Vue-Plus">RuoYi-Vue-Plus</a>）
 * @since 2022/12/24 12:58
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    /**
     * 用户登录并缓存用户信息
     *
     * @param loginUser
     *            登录用户信息
     * @return 令牌
     */
    public static String login(LoginUser loginUser) {
        // 记录登录信息
        HttpServletRequest request = ServletUtils.getRequest();
        loginUser.setClientIp(JakartaServletUtil.getClientIP(request));
        loginUser.setLocation(IpUtils.getCityInfo(loginUser.getClientIp()));
        loginUser.setBrowser(ServletUtils.getBrowser(request));
        LogContext logContext = LogContextHolder.get();
        loginUser.setLoginTime(null != logContext ? logContext.getCreateTime() : LocalDateTime.now());
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
     * @return 登录用户信息（获取 TokenSession 时如未登录，会抛出异常）
     */
    public static LoginUser getLoginUser() {
        LoginUser loginUser = (LoginUser)SaHolder.getStorage().get(CacheConstants.LOGIN_USER_KEY);
        if (null != loginUser) {
            return loginUser;
        }
        SaSession tokenSession = StpUtil.getTokenSession();
        if (null == tokenSession) {
            return null;
        }
        loginUser = (LoginUser)tokenSession.get(CacheConstants.LOGIN_USER_KEY);
        SaHolder.getStorage().set(CacheConstants.LOGIN_USER_KEY, loginUser);
        return loginUser;
    }

    /**
     * 根据 Token 获取登录用户信息
     *
     * @param token
     *            用户 Token
     * @return 登录用户信息
     */
    public static LoginUser getLoginUser(String token) {
        SaSession tokenSession = StpUtil.getTokenSessionByToken(token);
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
        return ExceptionUtils.exToNull(() -> getLoginUser().getId());
    }

    /**
     * 获取登录用户名
     *
     * @return 登录用户名
     */
    public static String getUsername() {
        return ExceptionUtils.exToNull(() -> getLoginUser().getUsername());
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
     * @param userId
     *            登录用户 ID
     * @return 登录用户昵称
     */
    public static String getNickname(Long userId) {
        return ExceptionUtils.exToNull(() -> SpringUtil.getBean(CommonUserService.class).getNicknameById(userId));
    }
}
