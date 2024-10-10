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

package top.continew.admin.common.context;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.extension.crud.service.CommonUserService;

import java.util.Optional;

/**
 * 用户上下文 Holder
 *
 * @author Charles7c
 * @since 2022/12/24 12:58
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<UserExtraContext> EXTRA_CONTEXT_HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    /**
     * 设置上下文
     *
     * @param context 上下文
     */
    public static void setContext(UserContext context) {
        setContext(context, true);
    }

    /**
     * 设置上下文
     *
     * @param context  上下文
     * @param isUpdate 是否更新
     */
    public static void setContext(UserContext context, boolean isUpdate) {
        CONTEXT_HOLDER.set(context);
        if (isUpdate) {
            StpUtil.getSessionByLoginId(context.getId()).set(SaSession.USER, context);
        }
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public static UserContext getContext() {
        UserContext context = CONTEXT_HOLDER.get();
        if (null == context) {
            context = StpUtil.getSession().getModel(SaSession.USER, UserContext.class);
            CONTEXT_HOLDER.set(context);
        }
        return context;
    }

    /**
     * 获取指定用户的上下文
     *
     * @param userId 用户 ID
     * @return 上下文
     */
    public static UserContext getContext(Long userId) {
        SaSession session = StpUtil.getSessionByLoginId(userId, false);
        if (null == session) {
            return null;
        }
        return session.getModel(SaSession.USER, UserContext.class);
    }

    /**
     * 设置额外上下文
     *
     * @param context 额外上下文
     */
    public static void setExtraContext(UserExtraContext context) {
        EXTRA_CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取额外上下文
     *
     * @return 额外上下文
     */
    public static UserExtraContext getExtraContext() {
        UserExtraContext context = EXTRA_CONTEXT_HOLDER.get();
        if (null == context) {
            context = getExtraContext(StpUtil.getTokenValue());
            EXTRA_CONTEXT_HOLDER.set(context);
        }
        return context;
    }

    /**
     * 获取额外上下文
     *
     * @param token 令牌
     * @return 额外上下文
     */
    public static UserExtraContext getExtraContext(String token) {
        UserExtraContext context = new UserExtraContext();
        context.setIp(Convert.toStr(StpUtil.getExtra(token, "ip")));
        context.setAddress(Convert.toStr(StpUtil.getExtra(token, "address")));
        context.setBrowser(Convert.toStr(StpUtil.getExtra(token, "browser")));
        context.setOs(Convert.toStr(StpUtil.getExtra(token, "os")));
        context.setLoginTime(Convert.toLocalDateTime(StpUtil.getExtra(token, "loginTime")));
        return context;
    }

    /**
     * 清除上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
        EXTRA_CONTEXT_HOLDER.remove();
    }

    /**
     * 获取用户 ID
     *
     * @return 用户 ID
     */
    public static Long getUserId() {
        return Optional.ofNullable(getContext()).map(UserContext::getId).orElse(null);
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        return Optional.ofNullable(getContext()).map(UserContext::getUsername).orElse(null);
    }

    /**
     * 获取用户昵称
     *
     * @return 用户昵称
     */
    public static String getNickname() {
        return getNickname(getUserId());
    }

    /**
     * 获取用户昵称
     *
     * @param userId 登录用户 ID
     * @return 用户昵称
     */
    public static String getNickname(Long userId) {
        return ExceptionUtils.exToNull(() -> SpringUtil.getBean(CommonUserService.class).getNicknameById(userId));
    }

    /**
     * 是否为管理员
     *
     * @return 是否为管理员
     */
    public static Boolean isAdmin() {
        StpUtil.checkLogin();
        return getContext().isAdmin();
    }
}
