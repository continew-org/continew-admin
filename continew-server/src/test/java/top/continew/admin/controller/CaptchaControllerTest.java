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

package top.continew.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.wf.captcha.base.Captcha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import top.continew.admin.auth.model.resp.CaptchaResp;
import top.continew.admin.common.config.CaptchaProperties;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.system.service.OptionService;
import top.continew.admin.system.service.SmsConfigService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.captcha.graphic.core.GraphicCaptchaService;
import top.continew.starter.core.autoconfigure.application.ApplicationProperties;
import top.continew.starter.core.exception.BusinessException;

import java.time.Duration;

/**
 * 验证码 API 测试。
 *
 * <p>覆盖图片验证码的启用/停用分支与验证码落缓存，行为验证码获取的成功与失败分支。
 * 全部为纯单元测试：OptionService/GraphicCaptchaService 等依赖用 Mockito 模拟，
 * RedisUtils 静态工具用 Mockito 模拟，不依赖 Spring 上下文与外部 Redis。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class CaptchaControllerTest {

    private static final String CAPTCHA_TEXT = "3mchx";

    private ApplicationProperties applicationProperties;
    private CaptchaProperties captchaProperties;
    private GraphicCaptchaService graphicCaptchaService;
    private OptionService optionService;
    private SmsConfigService smsConfigService;
    private CaptchaController controller;

    @BeforeEach
    void setUp() {
        applicationProperties = mock(ApplicationProperties.class);
        captchaProperties = new CaptchaProperties();
        graphicCaptchaService = mock(GraphicCaptchaService.class);
        optionService = mock(OptionService.class);
        smsConfigService = mock(SmsConfigService.class);
        controller = new CaptchaController(applicationProperties, captchaProperties,
            graphicCaptchaService, optionService, smsConfigService);
    }

    @Test
    void shouldReturnDisabledWhenLoginCaptchaDisabled() {
        when(optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED"))
            .thenReturn(GlobalConstants.Boolean.NO);

        CaptchaResp resp = controller.getImageCaptcha();

        assertFalse(resp.getIsEnabled());
        Mockito.verifyNoInteractions(graphicCaptchaService);
    }

    @Test
    void shouldSaveCaptchaToRedisWhenLoginCaptchaEnabled() {
        when(optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED"))
            .thenReturn(GlobalConstants.Boolean.YES);
        Captcha captcha = mock(Captcha.class);
        when(captcha.text()).thenReturn(CAPTCHA_TEXT);
        when(captcha.toBase64()).thenReturn("data:image/gif;base64,xxx");
        when(graphicCaptchaService.getCaptcha()).thenReturn(captcha);
        captchaProperties.setExpirationInMinutes(5);

        CaptchaResp resp;
        // RedisUtils 的静态块经 SpringUtil 取 RedissonClient，纯单测下先桩 SpringUtil 再模拟 RedisUtils
        try (
            MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class);
            MockedStatic<RedisUtils> redisUtils = mockStatic(RedisUtils.class)) {
            RedissonClient redissonClient = mock(RedissonClient.class);
            springUtil.when(() -> SpringUtil.getBean(RedissonClient.class))
                .thenReturn(redissonClient);
            resp = controller.getImageCaptcha();

            assertNotNull(resp.getUuid());
            assertEquals("data:image/gif;base64,xxx", resp.getImg());
            assertTrue(resp.getIsEnabled());
            assertTrue(resp.getExpireTime() > System.currentTimeMillis());
            redisUtils.verify(() -> RedisUtils
                .set(eq(CacheConstants.CAPTCHA_KEY_PREFIX + resp.getUuid()), eq(CAPTCHA_TEXT),
                    eq(Duration
                        .ofMinutes(5))));
        }
    }

    @Test
    void shouldReturnBehaviorCaptchaDataOnSuccess() {
        org.springframework.mock.web.MockHttpServletRequest request =
            new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("User-Agent", "JUnit");
        request.setRemoteAddr("127.0.0.1");
        com.anji.captcha.model.vo.CaptchaVO vo = new com.anji.captcha.model.vo.CaptchaVO();
        com.anji.captcha.model.common.ResponseModel model =
            new com.anji.captcha.model.common.ResponseModel();
        model.setRepCode(com.anji.captcha.model.common.RepCodeEnum.SUCCESS.getCode());
        model.setRepData("behavior-data");
        com.anji.captcha.service.CaptchaService behaviorCaptchaService =
            mock(com.anji.captcha.service.CaptchaService.class);
        when(behaviorCaptchaService.get(any())).thenReturn(model);

        Object result;
        try (MockedStatic<cn.hutool.extra.spring.SpringUtil> springUtil =
            mockStatic(cn.hutool.extra.spring.SpringUtil.class)) {
            springUtil.when(() -> cn.hutool.extra.spring.SpringUtil
                .getBean(com.anji.captcha.service.CaptchaService.class))
                .thenReturn(behaviorCaptchaService);
            result = controller.getBehaviorCaptcha(vo, request);
        }

        assertEquals("behavior-data", result);
        // browserInfo 由客户端 IP 与 UA 拼出，供行为验证码风控使用
        assertEquals("127.0.0.1JUnit", vo.getBrowserInfo());
    }

    @Test
    void shouldThrowWhenBehaviorCaptchaFails() {
        org.springframework.mock.web.MockHttpServletRequest request =
            new org.springframework.mock.web.MockHttpServletRequest();
        com.anji.captcha.model.vo.CaptchaVO vo = new com.anji.captcha.model.vo.CaptchaVO();
        com.anji.captcha.model.common.ResponseModel model =
            new com.anji.captcha.model.common.ResponseModel();
        model.setRepCode(com.anji.captcha.model.common.RepCodeEnum.API_CAPTCHA_ERROR.getCode());
        model.setRepMsg("验证码错误");
        com.anji.captcha.service.CaptchaService behaviorCaptchaService =
            mock(com.anji.captcha.service.CaptchaService.class);
        when(behaviorCaptchaService.get(any())).thenReturn(model);

        try (MockedStatic<cn.hutool.extra.spring.SpringUtil> springUtil =
            mockStatic(cn.hutool.extra.spring.SpringUtil.class)) {
            springUtil.when(() -> cn.hutool.extra.spring.SpringUtil
                .getBean(com.anji.captcha.service.CaptchaService.class))
                .thenReturn(behaviorCaptchaService);

            org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class,
                () -> controller.getBehaviorCaptcha(vo, request));
        }
    }

    @Test
    void shouldCheckBehaviorCaptchaAndReturnModel() {
        org.springframework.mock.web.MockHttpServletRequest request =
            new org.springframework.mock.web.MockHttpServletRequest();
        com.anji.captcha.model.vo.CaptchaVO vo = new com.anji.captcha.model.vo.CaptchaVO();
        com.anji.captcha.model.common.ResponseModel model =
            new com.anji.captcha.model.common.ResponseModel();
        model.setRepCode(com.anji.captcha.model.common.RepCodeEnum.SUCCESS.getCode());
        com.anji.captcha.service.CaptchaService behaviorCaptchaService =
            mock(com.anji.captcha.service.CaptchaService.class);
        when(behaviorCaptchaService.check(any())).thenReturn(model);

        Object result;
        try (MockedStatic<cn.hutool.extra.spring.SpringUtil> springUtil =
            mockStatic(cn.hutool.extra.spring.SpringUtil.class)) {
            springUtil.when(() -> cn.hutool.extra.spring.SpringUtil
                .getBean(com.anji.captcha.service.CaptchaService.class))
                .thenReturn(behaviorCaptchaService);
            result = controller.checkBehaviorCaptcha(vo, request);
        }

        assertEquals(model, result);
        verify(behaviorCaptchaService).check(vo);
    }

}
