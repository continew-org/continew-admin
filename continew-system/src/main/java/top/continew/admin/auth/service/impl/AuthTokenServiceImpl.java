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
import cn.dev33.satoken.stp.parameter.enums.SaLogoutMode;
import cn.dev33.satoken.stp.parameter.enums.SaReplacedRange;
import cn.hutool.core.bean.BeanUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.AuthTokenService;
import top.continew.admin.auth.service.RefreshTokenService;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.context.UserExtraContext;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.DeptService;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.RoleService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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
public class AuthTokenServiceImpl implements AuthTokenService {

    private static final String CLIENT_ID = "clientId";

    private final RoleService roleService;
    private final OptionService optionService;
    private final DeptService deptService;
    private final RefreshTokenService refreshTokenService;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public LoginResp issue(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response) {
        // 新登录会覆盖浏览器 Cookie，先撤销当前浏览器原有的 Refresh Session，避免旧账号会话
        // 失去 Cookie 后仍可在服务端继续存活；刷新场景走 issueAccessToken，不会执行此逻辑。
        // 登录前可能尚未建立 Sa-Token 会话，使用允许无会话读取的模式，避免误抛未登录异常。
        refreshTokenService.revokeCurrent(StpUtil.getStpLogic().getTokenValue(true),
            refreshTokenService.resolve(null, request));
        return this.issueInternal(user, client, tenantId, request, response, true);
    }

    @Override
    public LoginResp issueAccessToken(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response) {
        // 刷新场景只重新签发短期 Access Token，Refresh Session 的轮换由专门服务完成。
        return this.issueInternal(user, client, tenantId, request, response, false);
    }

    private LoginResp issueInternal(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response, boolean issueRefreshToken) {
        AtomicReference<LoginResp> result = new AtomicReference<>();
        // 刷新时租户上下文不会由前端重新提交，必须以 Refresh Session 中的 tenantId 为准。
        TenantUtils.execute(tenantId,
            () -> result.set(this.issueInTenant(user, client, tenantId, request, response,
                issueRefreshToken)));
        return result.get();
    }

    private LoginResp issueInTenant(UserDO user, ClientResp client, Long tenantId,
        HttpServletRequest request, HttpServletResponse response, boolean issueRefreshToken) {
        this.checkUserStatus(user);
        UserContext userContext = this.buildUserContext(user, tenantId);

        // Sa-Token 的登录参数决定 Access Token 的生命周期及并发登录行为。
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setActiveTimeout(client.getActiveTimeout());
        loginParameter.setTimeout(client.getTimeout());
        loginParameter.setDeviceType(client.getClientType());
        loginParameter.setExtra(CLIENT_ID, client.getClientId());
        loginParameter.setIsConcurrent(client.getIsConcurrent());
        if (Boolean.FALSE.equals(client.getIsConcurrent())) {
            loginParameter.setReplacedRange(
                SaReplacedRange.valueOf(client.getReplacedRange().getValue()));
        }
        loginParameter.setMaxLoginCount(client.getMaxLoginCount());
        if (client.getMaxLoginCount() != -1) {
            loginParameter.setOverflowLogoutMode(
                SaLogoutMode.valueOf(client.getOverflowLogoutMode().getValue()));
        }

        userContext.setClientType(client.getClientType());
        userContext.setClientId(client.getClientId());
        userContext.setTenantId(tenantId);

        // 用户上下文写入 SaSession，后续请求可直接读取权限、角色和租户信息。
        StpUtil.login(userContext.getId(), loginParameter.setExtraData(BeanUtil
            .beanToMap(new UserExtraContext(request))));
        UserContextHolder.setContext(userContext);
        String accessToken = StpUtil.getTokenValue();

        // 将客户端能力返回给前端。HttpOnly Cookie 对脚本不可见，前端不能靠读取 Cookie
        // 判断是否启用刷新，只能使用登录响应中的明确标识。
        boolean refreshTokenEnabled = refreshTokenService.isEnabled(client);
        LoginResp loginResp = LoginResp.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(client.getTimeout())
            .refreshTokenEnabled(refreshTokenEnabled)
            .tenantId(TenantContextHolder.isTenantEnabled() ? tenantId : null)
            .build();

        // 仅在客户端和全局配置均开启时创建 Refresh Session。浏览器明文只进入 HttpOnly
        // Cookie；BODY 模式才会把明文返回给未来的 App / 微信小程序客户端。
        if (issueRefreshToken && refreshTokenEnabled) {
            String refreshToken = refreshTokenService.issue(userContext, client, request, response,
                accessToken);
            loginResp.setRefreshExpiresIn(refreshTokenService.getRefreshTimeout(client));
            if (RefreshTokenModeEnum.BODY.equals(refreshTokenService.getMode(client))) {
                loginResp.setRefreshToken(refreshToken);
            }
        } else if (issueRefreshToken) {
            // 客户端关闭 Refresh Token 时清理浏览器之前遗留的长期凭证，防止旧 Cookie
            // 在前端状态丢失或旧版本前端仍发起刷新请求时造成误判。
            refreshTokenService.clearCookie(response);
        }
        return loginResp;
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
    private void checkUserStatus(UserDO user) {
        CheckUtils.throwIfNull(user, "用户不存在");
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, user.getStatus(),
            "此账号已被禁用，如有疑问，请联系管理员");
        DeptDO dept = deptService.getById(user.getDeptId());
        CheckUtils.throwIfNull(dept, "此账号所属部门不存在");
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, dept.getStatus(),
            "此账号所属部门已被禁用，如有疑问，请联系管理员");
    }
}
