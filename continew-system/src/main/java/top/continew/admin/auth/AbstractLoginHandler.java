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

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.model.req.LoginReq;
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
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.util.ServletUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.Validator;
import top.continew.starter.extension.tenant.TenantHandler;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static top.continew.admin.system.enums.PasswordPolicyEnum.PASSWORD_EXPIRATION_DAYS;

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
    private DeptService deptService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    protected static final String CAPTCHA_EXPIRED = "验证码已失效";
    protected static final String CAPTCHA_ERROR = "验证码不正确";
    protected static final String CLIENT_ID = "clientId";

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
     * @param user   用户信息
     * @param client 客户端信息
     * @return token 令牌信息
     */
    protected String authenticate(UserDO user, ClientResp client) {
        // 获取权限、角色、密码过期天数
        Long userId = user.getId();
        Long tenantId = TenantContextHolder.getTenantId();
        CompletableFuture<Set<String>> permissionFuture = CompletableFuture.supplyAsync(() -> {
            Set<String> permissions = new HashSet<>();
            SpringUtil.getBean(TenantHandler.class).execute(tenantId, () -> {
                permissions.addAll(roleService.listPermissionByUserId(userId));
            });
            return permissions;
        }, threadPoolTaskExecutor);
        CompletableFuture<Set<RoleContext>> roleFuture = CompletableFuture.supplyAsync(() -> {
            Set<RoleContext> roles = new HashSet<>();
            SpringUtil.getBean(TenantHandler.class).execute(tenantId, () -> {
                roles.addAll(roleService.listByUserId(userId));
            });
            return roles;
        }, threadPoolTaskExecutor);
        CompletableFuture<Integer> passwordExpirationDaysFuture = CompletableFuture.supplyAsync(() -> optionService
            .getValueByCode2Int(PASSWORD_EXPIRATION_DAYS.name()));
        CompletableFuture.allOf(permissionFuture, roleFuture, passwordExpirationDaysFuture);
        UserContext userContext = new UserContext(permissionFuture.join(), roleFuture
            .join(), passwordExpirationDaysFuture.join());
        BeanUtil.copyProperties(user, userContext);
        // 设置登录配置参数
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setActiveTimeout(client.getActiveTimeout());
        loginParameter.setTimeout(client.getTimeout());
        loginParameter.setDeviceType(client.getClientType());
        userContext.setClientType(client.getClientType());
        loginParameter.setExtra(CLIENT_ID, client.getClientId());
        userContext.setClientId(client.getClientId());
        userContext.setTenantId(tenantId);
        // 登录并缓存用户信息
        StpUtil.login(userContext.getId(), loginParameter.setExtraData(BeanUtil
            .beanToMap(new UserExtraContext(ServletUtils.getRequest()))));
        UserContextHolder.setContext(userContext);
        return StpUtil.getTokenValue();
    }

    /**
     * 检查用户状态
     *
     * @param user 用户信息
     */
    protected void checkUserStatus(UserDO user) {
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, user.getStatus(), "此账号已被禁用，如有疑问，请联系管理员");
        DeptDO dept = deptService.getById(user.getDeptId());
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, dept.getStatus(), "此账号所属部门已被禁用，如有疑问，请联系管理员");
    }
}