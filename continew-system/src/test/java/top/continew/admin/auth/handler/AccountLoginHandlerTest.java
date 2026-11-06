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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.continew.admin.auth.model.req.AccountLoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.auth.service.AuthTokenService;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.Set;

/**
 * 账号登录处理器测试。
 *
 * <p>覆盖验证码开关与失效分支、密码解密与用户名密码校验、连续错误密码的锁定计数递增与放行。
 * 全部为纯单元测试：静态工具（SecureUtils/RedisUtils/SpringUtil）用 Mockito 模拟，
 * 不依赖 Spring 上下文与外部 Redis。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class AccountLoginHandlerTest {

    private static final String USERNAME = "admin";
    private static final String CAPTCHA_UUID = "uuid-1";
    private static final String CAPTCHA_KEY = CacheConstants.CAPTCHA_KEY_PREFIX + CAPTCHA_UUID;
    // handler 先拼 USER_PASSWORD_ERROR_KEY_PREFIX 再拼 formatKey 结果，避免前缀重复
    private static final String LOCK_KEY = CacheConstants.USER_PASSWORD_ERROR_KEY_PREFIX
        + "admin|127.0.0.1";
    private static final String PLAIN_PASSWORD = "plain-pwd";
    private static final String HASHED_PASSWORD = "hashed-pwd";

    private PasswordEncoder passwordEncoder;
    private OptionService optionService;
    private UserService userService;
    private AuthTokenService authTokenService;
    private TestableAccountLoginHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        passwordEncoder = mock(PasswordEncoder.class);
        optionService = mock(OptionService.class);
        userService = mock(UserService.class);
        authTokenService = mock(AuthTokenService.class);
        handler = new TestableAccountLoginHandler(passwordEncoder);
        handler.injectServices(optionService, userService, authTokenService);
        request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        response = new MockHttpServletResponse();
    }

    /**
     * 父类协作字段为 protected，跨包不可直接赋值，经子类中转注入
     */
    private static class TestableAccountLoginHandler extends AccountLoginHandler {

        TestableAccountLoginHandler(PasswordEncoder passwordEncoder) {
            super(passwordEncoder);
        }

        void injectServices(OptionService optionService, UserService userService,
            AuthTokenService authTokenService) {
            this.optionService = optionService;
            this.userService = userService;
            this.authTokenService = authTokenService;
        }
    }

    private static AccountLoginReq req(String captcha, String uuid) {
        AccountLoginReq req = new AccountLoginReq();
        req.setUsername(USERNAME);
        req.setPassword("enc-pwd");
        req.setCaptcha(captcha);
        req.setUuid(uuid);
        return req;
    }

    private static UserDO user(String hashedPassword) {
        UserDO user = new UserDO();
        user.setUsername(USERNAME);
        user.setPassword(hashedPassword);
        return user;
    }

    /**
     * 参数校验器实例经 SpringUtil 从容器获取，纯单测下给一个恒通过的 mock
     *
     * @param springUtil SpringUtil 静态模拟对象
     */
    private void stubJakartaValidator(MockedStatic<SpringUtil> springUtil) {
        Validator jakartaValidator = mock(Validator.class);
        when(jakartaValidator.validate(any(Object.class), ArgumentMatchers.<Class<?>>any()))
            .thenReturn(Set.<ConstraintViolation<Object>>of());
        springUtil.when(() -> SpringUtil.getBean(Validator.class)).thenReturn(jakartaValidator);
    }

    /**
     * RSA 解密成功场景：密文换取固定明文
     *
     * @param secureUtils SecureUtils 静态模拟对象
     */
    private void stubDecryptSuccess(MockedStatic<SecureUtils> secureUtils) {
        secureUtils.when(() -> SecureUtils.decryptPasswordByRsaPrivateKey(anyString(), anyString()))
            .thenReturn(PLAIN_PASSWORD);
    }

    @Test
    void preLoginShouldThrowWhenCaptchaBlank() {
        when(optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED")).thenReturn(1);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            stubJakartaValidator(springUtil);

            AccountLoginReq req = req(null, CAPTCHA_UUID);
            ClientResp client = new ClientResp();
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> handler.preLogin(req, client, request));

            assertEquals("验证码不能为空", exception.getMessage());
        }
    }

    @Test
    void preLoginShouldThrowWhenCaptchaUuidBlank() {
        when(optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED")).thenReturn(1);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            stubJakartaValidator(springUtil);

            AccountLoginReq req = req("aB3d", null);
            ClientResp client = new ClientResp();
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> handler.preLogin(req, client, request));

            assertEquals("验证码标识不能为空", exception.getMessage());
        }
    }

    @Test
    void preLoginShouldThrowWhenCaptchaExpired() {
        when(optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED")).thenReturn(1);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class);
            MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            stubJakartaValidator(springUtil);
            redisUtils.when(() -> RedisUtils.get(CAPTCHA_KEY)).thenReturn(null);

            AccountLoginReq req = req("aB3d", CAPTCHA_UUID);
            ClientResp client = new ClientResp();
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> handler.preLogin(req, client, request));

            assertEquals("验证码已失效", exception.getMessage());
            redisUtils.verify(() -> RedisUtils.delete(CAPTCHA_KEY), never());
        }
    }

    @Test
    void preLoginShouldThrowAndConsumeWhenCaptchaWrong() {
        when(optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED")).thenReturn(1);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class);
            MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            stubJakartaValidator(springUtil);
            redisUtils.when(() -> RedisUtils.get(CAPTCHA_KEY)).thenReturn("aB3d");

            AccountLoginReq req = req("wrong", CAPTCHA_UUID);
            ClientResp client = new ClientResp();
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> handler.preLogin(req, client, request));

            assertEquals("验证码不正确", exception.getMessage());
            // 验证码校验前先删除缓存，无论匹配与否该验证码都不可复用
            redisUtils.verify(() -> RedisUtils.delete(CAPTCHA_KEY));
        }
    }

    @Test
    void preLoginShouldPassAndConsumeWhenCaptchaCorrect() {
        when(optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED")).thenReturn(1);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class);
            MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            stubJakartaValidator(springUtil);
            redisUtils.when(() -> RedisUtils.get(CAPTCHA_KEY)).thenReturn("aB3d");

            assertDoesNotThrow(() -> handler.preLogin(req("aB3d", CAPTCHA_UUID), new ClientResp(),
                request));

            redisUtils.verify(() -> RedisUtils.delete(CAPTCHA_KEY));
        }
    }

    @Test
    void preLoginShouldSkipCaptchaWhenDisabled() {
        when(optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED")).thenReturn(0);
        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class);
            MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            stubJakartaValidator(springUtil);

            assertDoesNotThrow(() -> handler.preLogin(req(null, null), new ClientResp(), request));

            redisUtils.verify(() -> RedisUtils.get(anyString()), never());
        }
    }

    @Test
    void loginShouldThrowWhenPasswordDecryptFailed() {
        try (MockedStatic<SecureUtils> secureUtils = mockStatic(SecureUtils.class)) {
            // 解密校验在 SecureUtils 内部完成，mock 模拟其抛出的解密失败异常
            secureUtils
                .when(() -> SecureUtils.decryptPasswordByRsaPrivateKey(anyString(), anyString()))
                .thenThrow(new BadRequestException("密码解密失败"));

            AccountLoginReq req = req("aB3d", CAPTCHA_UUID);
            ClientResp client = new ClientResp();
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> handler.login(req, client, request, response));

            assertEquals("密码解密失败", exception.getMessage());
            verify(authTokenService, never()).issue(any(), any(), any(), any(), any());
        }
    }

    @Test
    void loginShouldThrowWhenUserNotFound() {
        when(userService.getByUsername(USERNAME)).thenReturn(null);
        when(optionService.getValueByCode2Int("PASSWORD_ERROR_LOCK_COUNT")).thenReturn(0);
        try (MockedStatic<SecureUtils> secureUtils = mockStatic(SecureUtils.class)) {
            stubDecryptSuccess(secureUtils);

            AccountLoginReq req = req(null, null);
            ClientResp client = new ClientResp();
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> handler.login(req, client, request, response));

            assertEquals("用户名或密码不正确", exception.getMessage());
            verify(authTokenService, never()).issue(any(), any(), any(), any(), any());
        }
    }

    @Test
    void loginShouldThrowWhenPasswordMismatch() {
        when(userService.getByUsername(USERNAME)).thenReturn(user(HASHED_PASSWORD));
        when(passwordEncoder.matches(PLAIN_PASSWORD, HASHED_PASSWORD)).thenReturn(false);
        when(optionService.getValueByCode2Int("PASSWORD_ERROR_LOCK_COUNT")).thenReturn(0);
        try (MockedStatic<SecureUtils> secureUtils = mockStatic(SecureUtils.class)) {
            stubDecryptSuccess(secureUtils);

            AccountLoginReq req = req(null, null);
            ClientResp client = new ClientResp();
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> handler.login(req, client, request, response));

            assertEquals("用户名或密码不正确", exception.getMessage());
            verify(authTokenService, never()).issue(any(), any(), any(), any(), any());
        }
    }

    @Test
    void loginShouldIncrementErrorCountAndLockWhenThresholdReached() {
        when(userService.getByUsername(USERNAME)).thenReturn(user(HASHED_PASSWORD));
        when(passwordEncoder.matches(PLAIN_PASSWORD, HASHED_PASSWORD)).thenReturn(false);
        when(optionService.getValueByCode2Int("PASSWORD_ERROR_LOCK_COUNT")).thenReturn(5);
        when(optionService.getValueByCode2Int("PASSWORD_ERROR_LOCK_MINUTES")).thenReturn(15);
        try (MockedStatic<SecureUtils> secureUtils = mockStatic(SecureUtils.class);
            MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            stubDecryptSuccess(secureUtils);
            redisUtils.when(() -> RedisUtils.formatKey(any(String[].class)))
                .thenReturn("admin|127.0.0.1");
            redisUtils.when(() -> RedisUtils.get(LOCK_KEY)).thenReturn(4);

            AccountLoginReq req = req(null, null);
            ClientResp client = new ClientResp();
            BusinessException exception = assertThrows(BusinessException.class,
                () -> handler.login(req, client, request, response));

            assertTrue(exception.getMessage()
                .startsWith("由于您连续 5 次输入错误密码，账号已被锁定 15 分钟"));
            redisUtils.verify(() -> RedisUtils.set(LOCK_KEY, 5, Duration.ofMinutes(15)));
        }
    }

    @Test
    void loginShouldThrowWhenAlreadyLocked() {
        when(userService.getByUsername(USERNAME)).thenReturn(user(HASHED_PASSWORD));
        when(passwordEncoder.matches(PLAIN_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(optionService.getValueByCode2Int("PASSWORD_ERROR_LOCK_COUNT")).thenReturn(5);
        try (MockedStatic<SecureUtils> secureUtils = mockStatic(SecureUtils.class);
            MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            stubDecryptSuccess(secureUtils);
            redisUtils.when(() -> RedisUtils.formatKey(any(String[].class)))
                .thenReturn("admin|127.0.0.1");
            redisUtils.when(() -> RedisUtils.get(LOCK_KEY)).thenReturn(5);

            AccountLoginReq req = req(null, null);
            ClientResp client = new ClientResp();
            BusinessException exception = assertThrows(BusinessException.class,
                () -> handler.login(req, client, request, response));

            // 密码正确但已达锁定阈值同样拒绝，且不再递增计数
            assertTrue(exception.getMessage().startsWith("您的账号已被锁定"));
            redisUtils.verify(() -> RedisUtils.set(anyString(), any(), any()), never());
        }
    }

    @Test
    void loginShouldClearErrorCountAndIssueTokenOnSuccess() {
        when(userService.getByUsername(USERNAME)).thenReturn(user(HASHED_PASSWORD));
        when(passwordEncoder.matches(PLAIN_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(optionService.getValueByCode2Int("PASSWORD_ERROR_LOCK_COUNT")).thenReturn(5);
        LoginResp loginResp = mock(LoginResp.class);
        when(authTokenService.issue(any(), any(), any(), any(), any())).thenReturn(loginResp);
        try (MockedStatic<SecureUtils> secureUtils = mockStatic(SecureUtils.class);
            MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            stubDecryptSuccess(secureUtils);
            redisUtils.when(() -> RedisUtils.formatKey(any(String[].class)))
                .thenReturn("admin|127.0.0.1");
            redisUtils.when(() -> RedisUtils.get(LOCK_KEY)).thenReturn(2);

            LoginResp result = handler.login(req(null, null), new ClientResp(), request, response);

            assertSame(loginResp, result);
            redisUtils.verify(() -> RedisUtils.delete(LOCK_KEY));
        }
    }

    @Test
    void loginShouldIssueTokenWhenLockDisabled() {
        when(userService.getByUsername(USERNAME)).thenReturn(user(HASHED_PASSWORD));
        when(passwordEncoder.matches(PLAIN_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(optionService.getValueByCode2Int("PASSWORD_ERROR_LOCK_COUNT")).thenReturn(0);
        LoginResp loginResp = mock(LoginResp.class);
        when(authTokenService.issue(any(), any(), any(), any(), any())).thenReturn(loginResp);
        try (MockedStatic<SecureUtils> secureUtils = mockStatic(SecureUtils.class)) {
            stubDecryptSuccess(secureUtils);

            LoginResp result = handler.login(req(null, null), new ClientResp(), request, response);

            assertSame(loginResp, result);
        }
    }
}
