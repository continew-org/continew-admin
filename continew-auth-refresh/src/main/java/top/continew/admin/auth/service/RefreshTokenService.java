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

package top.continew.admin.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import top.continew.admin.auth.model.AuthSecurityVersion;
import top.continew.admin.auth.model.RefreshClientPolicy;
import top.continew.admin.auth.model.RefreshSession;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserExtraContext;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.List;

/**
 * Refresh Token 会话服务。
 *
 * @author luoqiz
 */
public interface RefreshTokenService {

    /** 获取客户端 Refresh Token 的绝对有效期（秒）。 */
    long getRefreshTimeout(RefreshClientPolicy clientPolicy);

    /** 获取客户端 Refresh Token 的传输模式。 */
    RefreshTokenModeEnum getMode(RefreshClientPolicy clientPolicy);

    /** 为一次新登录生成 Session ID，供 Access Token 和 Refresh Token 共同绑定。 */
    String newSessionId();

    /**
     * 在固定作用域锁内复查登录状态、执行 Session 数量策略并创建会话。
     *
     * <p>并发登录和最大登录数以 Refresh Session 为唯一事实源，不能依赖生命周期更短的
     * Access Token。当前浏览器中将被新 Cookie 覆盖的旧会话也在同一临界区内撤销。
     * 锁顺序固定为用户、租户、客户端，与安全配置失效共用同一组锁；状态复查函数也在
     * 锁内执行，调用方无法绕过临界区单独调用登录策略。</p>
     *
     * @param userId         初步认证得到的用户 ID，仅用于确定锁范围
     * @param clientId       初步认证得到的客户端 ID，仅用于确定锁范围
     * @param tenantId       租户 ID
     * @param attemptFactory 锁内最终状态复查函数；接收需要固化到新 Session 的安全版本
     * @return 签发结果
     */
    <T> T executeLoginPolicy(Long userId, String clientId, Long tenantId,
        Function<AuthSecurityVersion, LoginAttempt<T>> attemptFactory);

    /**
     * 创建登录会话的 Refresh Token。
     *
     * @return BODY 模式需要返回给客户端的明文 Token；COOKIE 模式返回值仅供内部使用
     */
    String issue(String sessionId, UserContext userContext, RefreshClientPolicy clientPolicy,
        UserExtraContext extraContext, AuthSecurityVersion securityVersion,
        HttpServletResponse response, String accessToken, long accessTokenTimeout);

    /**
     * 原子轮换 Refresh Token。
     *
     * @param rawRefreshToken 客户端提交的明文 Refresh Token
     * @param response        当前响应
     * @param accessTokenIssuer 根据旧会话重新签发 Access Token 的函数
     * @return 登录响应
     */
    LoginResp rotate(String rawRefreshToken, HttpServletResponse response,
        Function<RefreshSession, LoginResp> accessTokenIssuer);

    /** 从 Cookie 或 BODY 中读取 Refresh Token；同时出现两种来源时拒绝请求。 */
    String resolve(String bodyRefreshToken, HttpServletRequest request);

    /** 在解析不可信 Token 之前按可信客户端地址执行刷新限流。 */
    void checkRequestRateLimit(HttpServletRequest request);

    /** 校验 Cookie 模式请求来源，防止跨站请求伪造刷新或退出当前登录。 */
    void validateRequest(String rawRefreshToken, HttpServletRequest request);

    /** 请求携带 Refresh Token Cookie 时，在解析 Cookie 前校验请求来源。 */
    void validateCookieOrigin(HttpServletRequest request);

    /** 撤销当前 Access Token 对应的 Refresh Session */
    void revokeCurrent(String accessToken, String refreshToken);

    /** 撤销用户的全部 Refresh Session */
    void revokeByUser(Long userId);

    /** 撤销租户的全部 Refresh Session */
    void revokeByTenant(Long tenantId);

    /** 撤销客户端的全部 Refresh Session */
    void revokeByClient(String clientId);

    /** 查询有效登录会话；tenantId 为空时查询全部租户。 */
    List<RefreshSession> listSessions(Long tenantId);

    /** 查询指定有效登录会话。 */
    RefreshSession getSession(String sessionId);

    /** 撤销指定 Refresh Session。 */
    void revokeBySessionId(String sessionId);

    /** 清理浏览器 Refresh Token Cookie */
    void clearCookie(HttpServletResponse response);

    /**
     * 已在策略锁内完成最终状态复查的一次登录尝试。
     *
     * @param userId              最终确认的用户 ID
     * @param client              最终确认的客户端配置
     * @param currentAccessToken  当前请求携带的 Access Token
     * @param currentRefreshToken 当前请求携带的 Refresh Token
     * @param issuer              新令牌签发函数
     */
    record LoginAttempt<T>(Long userId, RefreshClientPolicy clientPolicy, String currentAccessToken,
        String currentRefreshToken, Supplier<T> issuer) {
    }
}
