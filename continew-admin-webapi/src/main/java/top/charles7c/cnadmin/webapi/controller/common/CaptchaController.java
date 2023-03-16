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

package top.charles7c.cnadmin.webapi.controller.common;

import java.time.Duration;

import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wf.captcha.base.Captcha;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

import top.charles7c.cnadmin.common.config.properties.CaptchaProperties;
import top.charles7c.cnadmin.common.config.properties.ContiNewAdminProperties;
import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.model.vo.CaptchaVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.*;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;

/**
 * 验证码 API
 *
 * @author Charles7c
 * @since 2022/12/11 14:00
 */
@Tag(name = "验证码 API")
@SaIgnore
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/common/captcha")
public class CaptchaController {

    private final CaptchaProperties captchaProperties;
    private final ContiNewAdminProperties properties;

    @Operation(summary = "获取图片验证码", description = "获取图片验证码（Base64编码，带图片格式：data:image/gif;base64）")
    @GetMapping("/img")
    public R<CaptchaVO> getImageCaptcha() {
        // 生成验证码
        CaptchaProperties.CaptchaImage captchaImage = captchaProperties.getImage();
        Captcha captcha = captchaImage.getCaptcha();

        // 保存验证码
        String uuid = IdUtil.fastSimpleUUID();
        String captchaKey = RedisUtils.formatKey(CacheConsts.CAPTCHA_CACHE_KEY, uuid);
        RedisUtils.setCacheObject(captchaKey, captcha.text(),
            Duration.ofMinutes(captchaImage.getExpirationInMinutes()));

        // 返回验证码
        CaptchaVO captchaVO = new CaptchaVO().setUuid(uuid).setImg(captcha.toBase64());
        return R.ok(captchaVO);
    }

    @Operation(summary = "获取邮箱验证码", description = "发送验证码到指定邮箱")
    @GetMapping("/mail")
    public R getMailCaptcha(
        @NotBlank(message = "邮箱不能为空") @Pattern(regexp = RegexPool.EMAIL, message = "邮箱格式错误") String email)
        throws MessagingException {
        String limitCacheKey = CacheConsts.LIMIT_CACHE_KEY;
        String captchaCacheKey = CacheConsts.CAPTCHA_CACHE_KEY;
        String limitCaptchaKey = RedisUtils.formatKey(limitCacheKey, captchaCacheKey, email);
        long limitTimeInMillisecond = RedisUtils.getTimeToLive(limitCaptchaKey);
        CheckUtils.throwIf(() -> limitTimeInMillisecond > 0,
            String.format("发送邮箱验证码过于频繁，请您 %ds 后再试", limitTimeInMillisecond / 1000));

        // 生成验证码
        CaptchaProperties.CaptchaMail captchaMail = captchaProperties.getMail();
        String captcha = RandomUtil.randomNumbers(captchaMail.getLength());

        // 发送验证码
        Long expirationInMinutes = captchaMail.getExpirationInMinutes();
        String content = TemplateUtils.render(captchaMail.getTemplatePath(),
            Dict.create().set("captcha", captcha).set("expiration", expirationInMinutes));
        MailUtils.sendHtml(email, String.format("【%s】邮箱验证码", properties.getName()), content);

        // 保存验证码
        String captchaKey = RedisUtils.formatKey(CacheConsts.CAPTCHA_CACHE_KEY, email);
        RedisUtils.setCacheObject(captchaKey, captcha, Duration.ofMinutes(expirationInMinutes));
        RedisUtils.setCacheObject(limitCaptchaKey, captcha, Duration.ofSeconds(captchaMail.getLimitInSeconds()));
        return R.ok(String.format("发送成功，验证码有效期 %s 分钟", expirationInMinutes));
    }
}
