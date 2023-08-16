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

package top.charles7c.cnadmin.common.util.helper;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.extra.spring.SpringUtil;

import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.model.dto.LogContext;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.service.CommonUserService;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.IpUtils;
import top.charles7c.cnadmin.common.util.ServletUtils;
import top.charles7c.cnadmin.common.util.holder.LogContextHolder;

/**
 * 登录助手
 *
 * @author Lion Li（RuoYi-Vue-Plus）
 * @author Charles7c
 * @since 2022/12/24 12:58
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    /**
     * 用户登录并缓存用户信息
     *
     * @param loginUser
     *            登录用户信息
     */
    public static void login(LoginUser loginUser) {
        if (null == loginUser) {
            return;
        }

        // 记录登录信息
        HttpServletRequest request = ServletUtils.getRequest();
        loginUser.setClientIp(ServletUtil.getClientIP(request));
        loginUser.setLocation(IpUtils.getCityInfo(loginUser.getClientIp()));
        loginUser.setBrowser(ServletUtils.getBrowser(request));
        LogContext logContext = LogContextHolder.get();
        loginUser.setLoginTime(null != logContext ? logContext.getCreateTime() : LocalDateTime.now());

        // 登录保存用户信息
        StpUtil.login(loginUser.getId());
        SaHolder.getStorage().set(CacheConsts.LOGIN_USER_KEY, loginUser);
        loginUser.setToken(StpUtil.getTokenValue());
        StpUtil.getSession().set(CacheConsts.LOGIN_USER_KEY, loginUser);
    }

    /**
     * 获取登录用户信息
     *
     * @return 登录用户信息
     */
    public static LoginUser getLoginUser() {
        LoginUser loginUser = (LoginUser)SaHolder.getStorage().get(CacheConsts.LOGIN_USER_KEY);
        if (null != loginUser) {
            return loginUser;
        }
        SaSession session = StpUtil.getSession();
        if (null == session) {
            return null;
        }
        loginUser = (LoginUser)session.get(CacheConsts.LOGIN_USER_KEY);
        SaHolder.getStorage().set(CacheConsts.LOGIN_USER_KEY, loginUser);
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
        Object loginId = StpUtil.getLoginIdByToken(token);
        SaSession session = StpUtil.getSessionByLoginId(loginId);
        if (null == session) {
            return null;
        }
        return (LoginUser)session.get(CacheConsts.LOGIN_USER_KEY);
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
