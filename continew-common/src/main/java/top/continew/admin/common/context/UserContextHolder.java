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

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import top.continew.admin.common.api.system.UserApi;
import top.continew.starter.core.util.ExceptionUtils;

/**
 * 用户上下文 Holder
 *
 * @author Charles7c
 * @since 1.0.0
 */
public class UserContextHolder {

    private static final TransmittableThreadLocal<UserContext> CONTEXT_HOLDER =
        new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<UserExtraContext> EXTRA_CONTEXT_HOLDER =
        new TransmittableThreadLocal<>();

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
            updateSessionContext(context);
        }
    }

    /**
     * 更新用户会话中的上下文，不改动当前线程的上下文缓存
     *
     * <p>用于变更后同步在线用户的会话数据（如管理员调整他人权限）。此类场景只需写会话，
     * 写线程本地会把他人上下文留在当前线程上，被同一请求后续步骤当作自己的上下文读取。</p>
     *
     * @param context 上下文
     * @since 5.0.0
     */
    public static void updateSessionContext(UserContext context) {
        StpUtil.getSessionByLoginId(context.getId()).set(SaSession.USER, context);
    }

    /**
     * 获取上下文
     *
     * <p>线程本地缓存须先确认归属：容器线程会被复用，上一请求若在其清理逻辑执行前抛出异常，
     * 缓存里留下的就是他人的上下文，直接返回即造成跨请求串用与越权。</p>
     *
     * @return 上下文
     */
    public static UserContext getContext() {
        UserContext context = CONTEXT_HOLDER.get();
        if (context != null && !isOwnedByCurrentLogin(context)) {
            // 归属不符时连同额外上下文一并清除，避免同一次请求后续步骤仍读到这批残留数据
            clearContext();
            context = null;
        }
        if (context == null) {
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
        if (session == null) {
            return null;
        }
        return session.getModel(SaSession.USER, UserContext.class);
    }

    /**
     * 判断线程本地上下文是否属于当前登录用户
     *
     * @param context 线程本地上下文
     * @return true：属于当前登录用户，或当前线程无登录上下文可比对；false：归属不符，或当前请求未登录
     */
    private static boolean isOwnedByCurrentLogin(UserContext context) {
        Object loginId;
        try {
            loginId = StpUtil.getLoginIdDefaultNull();
        } catch (SaTokenException e) {
            // 取不到 Sa-Token 请求上下文的线程（异步任务等）没有归属可比对，其上下文只能由代码经
            // TransmittableThreadLocal 显式传递而来，不属于跨请求残留，保持返回缓存
            return true;
        }
        if (loginId == null || context.getId() == null) {
            return false;
        }
        // 按字符串比对：JWT 载荷解析出的登录标识数值类型可能是 Integer 或 String，与 Long 型 userId 不同源
        return loginId.toString().equals(context.getId().toString());
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
        if (context == null) {
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
        context.setTenantId(Convert.toLong(StpUtil.getExtra(token, "tenantId")));
        context.setClientId(Convert.toStr(StpUtil.getExtra(token, "clientId")));
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
        return ExceptionUtils.exToNull(() -> getContext().getId());
    }

    /**
     * 获取租户 ID
     *
     * @return 租户 ID
     */
    public static Long getTenantId() {
        return ExceptionUtils.exToNull(() -> getContext().getTenantId());
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        return ExceptionUtils.exToNull(() -> getContext().getUsername());
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
        return ExceptionUtils
            .exToNull(() -> SpringUtil.getBean(UserApi.class).getNicknameById(userId));
    }

    /**
     * 是否为超级管理员
     *
     * @return true：是；false：否
     */
    public static boolean isSuperAdmin() {
        StpUtil.checkLogin();
        return getContext().isSuperAdmin();
    }

    /**
     * 是否为租户管理员
     *
     * @return true：是；false：否
     */
    public static boolean isTenantAdmin() {
        StpUtil.checkLogin();
        return getContext().isTenantAdmin();
    }
}
