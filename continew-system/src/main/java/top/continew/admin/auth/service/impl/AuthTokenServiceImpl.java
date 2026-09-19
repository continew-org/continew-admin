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

package top.continew.admin.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;
import top.continew.admin.auth.model.AuthSecurityVersion;
import top.continew.admin.auth.model.RefreshSession;
import top.continew.admin.auth.model.RefreshClientPolicy;
import top.continew.admin.auth.adapter.RefreshClientPolicyMapper;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.AuthTokenService;
import top.continew.admin.auth.service.RefreshTokenService;
import top.continew.admin.auth.service.RefreshTokenService.LoginAttempt;
import top.continew.admin.auth.api.AuthSessionConstants;
import top.continew.admin.common.api.tenant.TenantApi;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.context.UserExtraContext;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.auth.exception.RefreshTokenException;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.ClientService;
import top.continew.admin.system.service.DeptService;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static top.continew.admin.system.enums.PasswordPolicyEnum.PASSWORD_EXPIRATION_DAYS;

/**
 * 统一 Access Token 签发实现。
 *
 * <p>Access Token 使用 Sa-Token 管理，Refresh Token 则由独立的 Redis 会话服务管理。
 * 两者职责分离：Access Token 负责短期接口访问，Refresh Token 负责在 Access Token
 * 过期后安全地轮换新令牌。</p>
 *
 * @author luoqiz
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthTokenServiceImpl implements AuthTokenService {

    private static final String CLIENT_ID = "clientId";

    private final RoleService roleService;
    private final OptionService optionService;
    private final DeptService deptService;
    private final UserService userService;
    private final ClientService clientService;
    private final RefreshTokenService refreshTokenService;
    private final TenantApi tenantApi;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public LoginResp issue(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response) {
        return this.issueInternal(user, client, tenantId, request, response, true);
    }

    @Override
    public LoginResp issueAccessToken(UserDO user, ClientResp client, Long tenantId,
        RefreshSession refreshSession, HttpServletRequest request, HttpServletResponse response) {
        // 刷新场景只重新签发短期 Access Token，Refresh Session 的轮换由专门服务完成。
        return this.issueInternal(user, client, tenantId, request, response, false,
            refreshSession);
    }

    private LoginResp issueInternal(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response, boolean issueRefreshToken) {
        return this.issueInternal(user, client, tenantId, request, response, issueRefreshToken,
            null);
    }

    private LoginResp issueInternal(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response, boolean issueRefreshToken,
        RefreshSession refreshSession) {
        if (issueRefreshToken) {
            return this.issueNewSession(user, client, tenantId, request, response);
        }
        AtomicReference<LoginResp> result = new AtomicReference<>();
        // 刷新时租户上下文不会由前端重新提交，必须以 Refresh Session 中的 tenantId 为准。
        TenantUtils.execute(tenantId,
            () -> {
                this.checkUserStatus(user, true);
                result.set(this.issueInTenant(user, client, tenantId, request, response, false,
                    refreshSession, null));
            });
        return result.get();
    }

    /** 新登录的最终数据库复查、安全版本读取、会话策略和签发必须处于同一组作用域锁内。 */
    private LoginResp issueNewSession(UserDO authenticatedUser, ClientResp authenticatedClient,
        Long tenantId, HttpServletRequest request, HttpServletResponse response) {
        return refreshTokenService.executeLoginPolicy(authenticatedUser.getId(),
            authenticatedClient.getClientId(), tenantId, securityVersion -> {
                LoginState loginState = this.reloadLoginState(authenticatedUser,
                    authenticatedClient, tenantId);
                // 登录早期的租户校验可能早于认证过程很久；在租户锁内再次读取数据库，保证
                // 租户禁用、过期或套餐变更与新 Session 创建严格串行。
                tenantApi.checkStatus(tenantId);
                String currentAccessToken = StpUtil.getTokenValue();
                String currentRefreshToken = refreshTokenService.resolve(null, request);
                return new LoginAttempt<>(loginState.user().getId(), RefreshClientPolicyMapper
                    .from(loginState.client()),
                    currentAccessToken, currentRefreshToken,
                    () -> this.executeInTenant(tenantId,
                        () -> this.issueInTenant(loginState.user(), loginState.client(), tenantId,
                            request, response, true, null, securityVersion)));
            });
    }

    private LoginResp issueInTenant(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response, boolean issueRefreshToken,
        RefreshSession refreshSession, AuthSecurityVersion securityVersion) {
        UserContext userContext = this.buildUserContext(user, tenantId);

        String sessionId = issueRefreshToken ? refreshTokenService.newSessionId()
            : refreshSession.getSessionId();

        RefreshClientPolicy refreshClientPolicy = RefreshClientPolicyMapper.from(client);
        long accessTokenTimeout = this.getEffectiveAccessTokenTimeout(client.getTimeout(),
            issueRefreshToken ? refreshTokenService.getRefreshTimeout(refreshClientPolicy)
                : this.remainingSeconds(refreshSession.getExpiresAt()));

        // Sa-Token 只管理短期 Access Token 的生命周期。并发登录、顶人范围和最大登录
        // 数量仅由 Refresh Session 执行，避免两个事实源产生跨端误淘汰。
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setActiveTimeout(client.getActiveTimeout());
        loginParameter.setTimeout(accessTokenTimeout);
        loginParameter.setDeviceType(client.getClientType());
        // sid 同时进入 Access Token 和 Refresh Token，用于服务端核对两类凭证是否
        // 属于同一次登录，客户端不能自行指定或覆盖该值。
        loginParameter.setIsConcurrent(true);
        loginParameter.setMaxLoginCount(-1);

        userContext.setClientType(client.getClientType());
        userContext.setClientId(client.getClientId());
        userContext.setTenantId(tenantId);

        String accessToken = null;
        try {
            // 用户上下文写入 SaSession，后续请求可直接读取权限、角色和租户信息。
            UserExtraContext extraContext = new UserExtraContext(request, tenantId);
            extraContext.setClientId(client.getClientId());
            // SaLoginParameter#setExtraData 会整体替换 extra 数据。必须在同一 Map 中
            // 写入会话声明，否则 sid 会被用户额外上下文覆盖，后续接口无法关联到
            // Refresh Session 而被错误判定为 401。
            Map<String, Object> loginExtraData = new HashMap<>(BeanUtil.beanToMap(extraContext));
            loginExtraData.put(CLIENT_ID, client.getClientId());
            loginExtraData.put(AuthSessionConstants.SESSION_ID_CLAIM, sessionId);
            StpUtil.login(userContext.getId(), loginParameter.setExtraData(loginExtraData));
            // 先保存本次签发的令牌，保证后续任一步骤失败时可以精确清理它。
            accessToken = StpUtil.getTokenValue();
            UserContextHolder.setContext(userContext);

            LoginResp loginResp = LoginResp.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenTimeout)
                .tenantId(TenantContextHolder.isTenantEnabled() ? tenantId : null)
                .build();

            // 新登录创建 Refresh Session。浏览器明文只进入 HttpOnly Cookie；BODY 模式
            // 将明文返回给 App / 微信小程序客户端。
            if (issueRefreshToken) {
                String refreshToken =
                    refreshTokenService.issue(sessionId, userContext, refreshClientPolicy,
                        extraContext, securityVersion, response, accessToken, accessTokenTimeout);
                loginResp.setRefreshExpiresIn(
                    refreshTokenService.getRefreshTimeout(refreshClientPolicy));
                if (RefreshTokenModeEnum.BODY
                    .equals(refreshTokenService.getMode(refreshClientPolicy))) {
                    loginResp.setRefreshToken(refreshToken);
                }
            }
            return loginResp;
        } catch (Exception e) {
            // 登录响应组装、Refresh Session 写入或 Cookie 写入失败时，不能留下孤立的
            // Access Token。这里覆盖 StpUtil.login 后的所有异常路径，而不只是 Redis 失败。
            if (accessToken != null) {
                try {
                    StpUtil.logoutByTokenValue(accessToken);
                } catch (Exception logoutException) {
                    // 记录清理失败，但保留原始异常，便于调用方得到真实失败原因。
                    log.error("Refresh Session 创建失败后，清理用户 [{}] 的 Access Token 失败",
                        user.getId(), logoutException);
                }
            }
            throw e;
        }
    }

    /** 构建刷新时也必须使用的最新权限上下文，避免角色变更后继续沿用旧权限。 */
    private UserContext buildUserContext(UserDO user, Long tenantId) {
        Long userId = user.getId();
        CompletableFuture<Set<String>> permissionFuture = CompletableFuture.supplyAsync(() -> {
            Set<String> permissions = new HashSet<>();
            TenantUtils.execute(tenantId,
                () -> permissions.addAll(roleService.listPermissionByUserId(userId)));
            return permissions;
        }, threadPoolTaskExecutor);
        CompletableFuture<Set<RoleContext>> roleFuture = CompletableFuture.supplyAsync(() -> {
            Set<RoleContext> roles = new HashSet<>();
            TenantUtils.execute(tenantId, () -> roles.addAll(roleService.listByUserId(userId)));
            return roles;
        }, threadPoolTaskExecutor);
        CompletableFuture<Integer> passwordExpirationDaysFuture = CompletableFuture.supplyAsync(
            () -> optionService.getValueByCode2Int(PASSWORD_EXPIRATION_DAYS.name()),
            threadPoolTaskExecutor);
        CompletableFuture.allOf(permissionFuture, roleFuture, passwordExpirationDaysFuture).join();

        UserContext context = new UserContext(permissionFuture.join(), roleFuture.join(),
            passwordExpirationDaysFuture.join());
        BeanUtil.copyProperties(user, context);
        return context;
    }

    /** 刷新不能绕过用户或部门禁用校验。 */
    private void checkUserStatus(UserDO user, boolean refreshRequest) {
        this.requireUserState(user != null, "用户不存在", refreshRequest);
        this.requireUserState(!DisEnableStatusEnum.DISABLE.equals(user.getStatus()),
            "此账号已被禁用，如有疑问，请联系管理员", refreshRequest);
        DeptDO dept = deptService.getById(user.getDeptId());
        this.requireUserState(dept != null, "此账号所属部门不存在", refreshRequest);
        this.requireUserState(!DisEnableStatusEnum.DISABLE.equals(dept.getStatus()),
            "此账号所属部门已被禁用，如有疑问，请联系管理员", refreshRequest);
    }

    private LoginState reloadLoginState(UserDO authenticatedUser, ClientResp authenticatedClient,
        Long tenantId) {
        ClientResp currentClient = clientService.getByClientId(authenticatedClient.getClientId());
        CheckUtils.throwIfNull(currentClient, "客户端不存在");
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, currentClient.getStatus(),
            "客户端已禁用");
        if (!this.isSameSecurityPolicy(authenticatedClient, currentClient)) {
            throw new BusinessException("客户端认证配置已变更，请重新登录");
        }

        AtomicReference<UserDO> currentUser = new AtomicReference<>();
        TenantUtils.execute(tenantId, () -> {
            currentUser.set(userService.getById(authenticatedUser.getId()));
            this.checkUserStatus(currentUser.get(), false);
        });
        if (!Objects.equals(authenticatedUser.getPwdResetTime(),
            currentUser.get().getPwdResetTime())) {
            throw new BusinessException("账号凭证已变更，请重新登录");
        }
        return new LoginState(currentUser.get(), currentClient);
    }

    private boolean isSameSecurityPolicy(ClientResp expected, ClientResp actual) {
        return Objects.equals(expected.getClientType(), actual.getClientType())
            && Objects.equals(expected.getAuthType(), actual.getAuthType())
            && Objects.equals(expected.getActiveTimeout(), actual.getActiveTimeout())
            && Objects.equals(expected.getTimeout(), actual.getTimeout())
            && Objects.equals(expected.getRefreshTokenTimeout(), actual.getRefreshTokenTimeout())
            && Objects.equals(expected.getRefreshTokenMode(), actual.getRefreshTokenMode())
            && Objects.equals(expected.getIsConcurrent(), actual.getIsConcurrent())
            && Objects.equals(expected.getReplacedRange(), actual.getReplacedRange())
            && Objects.equals(expected.getMaxLoginCount(), actual.getMaxLoginCount())
            && Objects.equals(expected.getOverflowLogoutMode(), actual.getOverflowLogoutMode());
    }

    private long getEffectiveAccessTokenTimeout(Long configuredTimeout, long sessionTimeout) {
        if (configuredTimeout == null) {
            throw new BusinessException("Access Token 有效期未配置");
        }
        if (configuredTimeout == -1 || configuredTimeout > sessionTimeout) {
            return sessionTimeout;
        }
        return configuredTimeout;
    }

    private long remainingSeconds(long expiresAt) {
        long remainingMillis = expiresAt - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            throw RefreshTokenException.unauthorized("登录状态已失效，请重新登录");
        }
        return remainingMillis / 1000 + (remainingMillis % 1000 == 0 ? 0 : 1);
    }

    private void requireUserState(boolean valid, String message, boolean refreshRequest) {
        if (valid) {
            return;
        }
        if (refreshRequest) {
            throw RefreshTokenException.unauthorized(message);
        }
        throw new BusinessException(message);
    }

    private <T> T executeInTenant(Long tenantId, Supplier<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        TenantUtils.execute(tenantId, () -> result.set(action.get()));
        return result.get();
    }

    private record LoginState(UserDO user, ClientResp client) {
    }
}
