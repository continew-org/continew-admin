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

package top.continew.admin.config.satoken;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import top.continew.admin.auth.constant.AuthConstants;
import top.continew.admin.auth.service.SessionActivityService;
import top.continew.admin.auth.support.ClientPolicyCache;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.context.UserExtraContext;
import top.continew.admin.open.util.OpenApiUtils;
import top.continew.starter.auth.refresh.token.api.AccessSessionValidator;
import top.continew.starter.auth.refresh.token.support.AccessTokenClaimsReader;
import top.continew.starter.core.util.ServletUtils;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.json.jackson.util.JsonUtils;
import top.continew.starter.web.model.R;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * Sa-Token 扩展拦截器
 *
 * @author Charles7c
 * @since 3.4.0
 */
@Slf4j
public class SaExtensionInterceptor extends SaInterceptor {

    /**
     * 客户端 ID 令牌声明名，与登录时写入的声明保持一致
     */
    private static final String CLIENT_ID_CLAIM = "clientId";

    private final AccessSessionValidator accessSessionValidator;
    private final SessionActivityService sessionActivityService;
    private final ClientPolicyCache clientPolicyCache;
    private final LoginPasswordProperties loginPasswordProperties;

    public SaExtensionInterceptor(SaParamFunction<Object> auth,
        AccessSessionValidator accessSessionValidator,
        SessionActivityService sessionActivityService,
        ClientPolicyCache clientPolicyCache,
        LoginPasswordProperties loginPasswordProperties) {
        super(auth);
        this.accessSessionValidator = accessSessionValidator;
        this.sessionActivityService = sessionActivityService;
        this.clientPolicyCache = clientPolicyCache;
        this.loginPasswordProperties = loginPasswordProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
        HttpServletResponse response,
        Object handler) throws Exception {
        boolean flag = super.preHandle(request, response, handler);
        // AK/SK 请求已经由签名认证完成，不属于交互式登录 Session。
        if (!flag || OpenApiUtils.isSignParamExists() || !StpUtil.isLogin()) {
            return flag;
        }
        // 设置上下文
        UserContext userContext = UserContextHolder.getContext();
        // Sa-Token 只校验 Access Token 自身有效性；这里补充 Session 状态校验，确保用户、
        // 租户或客户端强制下线后，尚未自然过期的 Access Token 也不能继续访问。
        // 登录、刷新和退出都由认证服务依据请求体、Cookie 或令牌映射自行确定租户，不能
        // 使用当前请求残留的租户上下文做普通业务接口的跨租户校验。
        boolean authRequest = this.isAuthRequest(request);
        if (!authRequest) {
            String tokenValue = StpUtil.getTokenValue();
            // 失效原因区分被强退、被顶下线和普通失效，让客户端能给出准确的重新登录提示。
            String invalidReason = accessSessionValidator.getInvalidReason(tokenValue);
            if (invalidReason != null) {
                // preHandle 返回 false 时 afterCompletion 不再回调（Spring 只对已通过的
                // 拦截器回调），若不在此清除，上一请求写入的 UserContext 线程本地会残留
                // 在 Tomcat 池化线程上，被下一请求复用造成跨用户上下文串用。
                UserContextHolder.clearContext();
                R r = R.fail(String.valueOf(HttpStatus.UNAUTHORIZED.value()), invalidReason);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                ServletUtils.writeJson(response, JsonUtils.toJsonStr(r));
                return false;
            }
            // 闲置判定必须放在记录活跃之前：先记录会把最后活跃时间刷成当前时间，闲置永远判不出来。
            String sessionId = AccessTokenClaimsReader.resolveSessionIdQuietly(tokenValue);
            String clientId = this.resolveClientId(tokenValue, userContext);
            long idleTimeoutSeconds = clientPolicyCache.get(clientId).idleTimeoutSeconds();
            // 密码过期放行的路径（改密、拉路由、取用户信息）同时也是自助通道，不参与闲置判定：
            // 密码过期和闲置超时同时成立时，若这里再拦一次，用户只能重新登录才能改密。
            // 这些请求本身要求完整凭据，放行不会放大风险，活跃时间仍照常记录。
            boolean selfServicePath =
                SaRouter.isMatchCurrURI(loginPasswordProperties.getExcludes());
            if (!selfServicePath && sessionActivityService
                .isIdleExpired(sessionId, idleTimeoutSeconds)) {
                // 这里只拒绝本次请求，不撤销会话：会话撤销统一由刷新时的兜底判定完成，
                // 否则一个后台轮询请求就可能把还能恢复的会话直接清掉。
                UserContextHolder.clearContext();
                R r = R.fail(String.valueOf(HttpStatus.UNAUTHORIZED.value()), "长时间未操作，请重新登录");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                ServletUtils.writeJson(response, JsonUtils.toJsonStr(r));
                // 闲置用户可能停在某个页面持续轮询，这里只记 DEBUG，避免刷满日志。
                log.debug("会话已闲置超时，拒绝访问：sessionId={}，闲置阈值={}秒", sessionId, idleTimeoutSeconds);
                return false;
            }
            sessionActivityService.touch(sessionId, clientId);
        }
        if (authRequest) {
            return true;
        }
        if (userContext == null) {
            return true;
        }
        // 检查用户租户权限
        if (TenantContextHolder.isTenantEnabled()) {
            // UserContext 保存在用户级 SaSession，同一用户多租户并发登录时会被后一次
            // 登录覆盖；租户边界必须使用当前 Access Token 自身的额外上下文。
            UserExtraContext extraContext = UserContextHolder.getExtraContext();
            Long userTenantId = extraContext.getTenantId();
            Long tenantId = TenantContextHolder.getTenantId();
            if (!Objects.equals(userTenantId, tenantId)) {
                // 见上方 401 短路的注释：必须在此清除线程本地，避免残留上下文复用。
                UserContextHolder.clearContext();
                R r = R.fail(String.valueOf(HttpStatus.FORBIDDEN.value()), "您当前没有访问该租户的权限");
                response.setStatus(HttpStatus.FORBIDDEN.value());
                ServletUtils.writeJson(response, JsonUtils.toJsonStr(r));
                return false;
            }
        }
        return true;
    }

    private boolean isAuthRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());
        return AuthConstants.LOGIN_URI.equals(path)
            || AuthConstants.REFRESH_URI.equals(path)
            || AuthConstants.LOGOUT_URI.equals(path);
    }

    /**
     * 解析当前令牌对应的客户端 ID
     *
     * <p>必须优先取令牌自身的声明：{@link UserContext} 存在用户级 SaSession 里，同一账号多端并发
     * 登录时会被后一次登录覆盖（与上方租户校验同理），用它会让 A 端会话按 B 端的闲置策略判定，
     * 轻则该闲置的不闲置，重则正常的会话被误踢。</p>
     *
     * @param tokenValue  当前 Access Token
     * @param userContext 用户上下文，令牌缺少客户端声明时回落使用
     * @return 客户端 ID，无法确定时返回 {@code null}
     */
    private String resolveClientId(String tokenValue, UserContext userContext) {
        try {
            Object claim = StpUtil.getExtra(tokenValue, CLIENT_ID_CLAIM);
            if (claim != null && !claim.toString().isBlank()) {
                return claim.toString();
            }
        } catch (Exception e) {
            // 非 JWT 令牌（如开放接口签名请求）读不到声明，回落到用户上下文。
            log.debug("读取令牌中的客户端声明失败，回落到用户上下文：{}", e.getMessage());
        }
        return userContext == null ? null : userContext.getClientId();
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable Exception e) throws Exception {
        // 清除上下文
        try {
            super.afterCompletion(request, response, handler, e);
        } finally {
            UserContextHolder.clearContext();
        }
    }
}
