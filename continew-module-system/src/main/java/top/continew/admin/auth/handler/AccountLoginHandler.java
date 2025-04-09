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

package top.continew.admin.auth.handler;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.AbstractLoginHandler;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.AccountLoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.system.enums.PasswordPolicyEnum;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.validation.CheckUtils;
import top.continew.starter.core.validation.ValidationUtils;

import java.time.Duration;

/**
 * 账号登录处理器
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/22 14:58
 */
@Component
@RequiredArgsConstructor
public class AccountLoginHandler extends AbstractLoginHandler<AccountLoginReq> {

    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResp login(AccountLoginReq req, ClientResp client, HttpServletRequest request) {
        // 解密密码
        String rawPassword = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(req.getPassword()));
        ValidationUtils.throwIfBlank(rawPassword, "密码解密失败");
        // 验证用户名密码
        String username = req.getUsername();
        UserDO user = userService.getByUsername(username);
        boolean isError = ObjectUtil.isNull(user) || !passwordEncoder.matches(rawPassword, user.getPassword());
        // 检查账号锁定状态
        this.checkUserLocked(req.getUsername(), request, isError);
        ValidationUtils.throwIf(isError, "用户名或密码不正确");
        // 检查用户状态
        super.checkUserStatus(user);
        // 执行认证
        String token = this.authenticate(user, client);
        return LoginResp.builder().token(token).build();
    }

    @Override
    public void preLogin(AccountLoginReq req, ClientResp client, HttpServletRequest request) {
        super.preLogin(req, client, request);
        // 校验验证码
        int loginCaptchaEnabled = optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED");
        if (SysConstants.YES.equals(loginCaptchaEnabled)) {
            ValidationUtils.throwIfBlank(req.getCaptcha(), "验证码不能为空");
            ValidationUtils.throwIfBlank(req.getUuid(), "验证码标识不能为空");
            String captchaKey = CacheConstants.CAPTCHA_KEY_PREFIX + req.getUuid();
            String captcha = RedisUtils.get(captchaKey);
            ValidationUtils.throwIfBlank(captcha, CAPTCHA_EXPIRED);
            RedisUtils.delete(captchaKey);
            ValidationUtils.throwIfNotEqualIgnoreCase(req.getCaptcha(), captcha, CAPTCHA_ERROR);
        }
    }

    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.ACCOUNT;
    }

    /**
     * 检测用户是否已被锁定
     *
     * @param username 用户名
     * @param request  请求对象
     * @param isError  是否登录失败
     */
    private void checkUserLocked(String username, HttpServletRequest request, boolean isError) {
        // 不锁定
        int maxErrorCount = optionService.getValueByCode2Int(PasswordPolicyEnum.PASSWORD_ERROR_LOCK_COUNT.name());
        if (maxErrorCount <= SysConstants.NO) {
            return;
        }
        // 检测是否已被锁定
        String key = CacheConstants.USER_PASSWORD_ERROR_KEY_PREFIX + RedisUtils.formatKey(username, JakartaServletUtil
            .getClientIP(request));
        Integer currentErrorCount = ObjectUtil.defaultIfNull(RedisUtils.get(key), 0);
        CheckUtils.throwIf(currentErrorCount >= maxErrorCount, PasswordPolicyEnum.PASSWORD_ERROR_LOCK_MINUTES.getMsg()
            .formatted(this.getUnlockTime(key)));
        // 登录成功清除计数
        if (!isError) {
            RedisUtils.delete(key);
            return;
        }
        // 登录失败递增计数
        currentErrorCount++;
        int lockMinutes = optionService.getValueByCode2Int(PasswordPolicyEnum.PASSWORD_ERROR_LOCK_MINUTES.name());
        RedisUtils.set(key, currentErrorCount, Duration.ofMinutes(lockMinutes));
        CheckUtils.throwIf(currentErrorCount >= maxErrorCount, PasswordPolicyEnum.PASSWORD_ERROR_LOCK_COUNT.getMsg()
            .formatted(maxErrorCount, lockMinutes, this.getUnlockTime(key)));
    }

    /**
     * 获取解锁时间
     *
     * @param key 键
     * @return 解锁时间
     */
    private String getUnlockTime(String key) {
        long timeToLive = RedisUtils.getTimeToLive(key);
        return timeToLive > 0
            ? DateUtil.date()
                .offset(DateField.MILLISECOND, (int)timeToLive)
                .toString(DatePattern.CHINESE_DATE_TIME_FORMAT)
            : "";
    }
}