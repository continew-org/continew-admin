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

import javax.mail.MessagingException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

import top.charles7c.cnadmin.common.config.properties.CaptchaProperties;
import top.charles7c.cnadmin.common.config.properties.ProjectProperties;
import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.constant.RegexConsts;
import top.charles7c.cnadmin.common.model.vo.CaptchaVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.MailUtils;
import top.charles7c.cnadmin.common.util.RedisUtils;
import top.charles7c.cnadmin.common.util.TemplateUtils;
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
    private final ProjectProperties projectProperties;

    @Operation(summary = "获取图片验证码", description = "获取图片验证码（Base64编码，带图片格式：data:image/gif;base64）")
    @GetMapping("/img")
    public R<CaptchaVO> getImageCaptcha() {
        // 生成验证码
        CaptchaProperties.CaptchaImage captchaImage = captchaProperties.getImage();
        Captcha captcha = captchaImage.getCaptcha();

        // 保存验证码
        String uuid = IdUtil.fastUUID();
        String captchaKey = RedisUtils.formatKey(CacheConsts.CAPTCHA_KEY_PREFIX, uuid);
        RedisUtils.setCacheObject(captchaKey, captcha.text(),
            Duration.ofMinutes(captchaImage.getExpirationInMinutes()));
        return R.ok(CaptchaVO.builder().uuid(uuid).img(captcha.toBase64()).build());
    }

    @Operation(summary = "获取邮箱验证码", description = "发送验证码到指定邮箱")
    @GetMapping("/mail")
    public R getMailCaptcha(
        @NotBlank(message = "邮箱不能为空") @Pattern(regexp = RegexConsts.EMAIL, message = "邮箱格式错误") String email)
        throws MessagingException {
        String limitKeyPrefix = CacheConsts.LIMIT_KEY_PREFIX;
        String captchaKeyPrefix = CacheConsts.CAPTCHA_KEY_PREFIX;
        String limitCaptchaKey = RedisUtils.formatKey(limitKeyPrefix, captchaKeyPrefix, email);
        long limitTimeInMillisecond = RedisUtils.getTimeToLive(limitCaptchaKey);
        CheckUtils.throwIf(limitTimeInMillisecond > 0, "发送邮箱验证码过于频繁，请您 {}s 后再试", limitTimeInMillisecond / 1000);

        // 生成验证码
        CaptchaProperties.CaptchaMail captchaMail = captchaProperties.getMail();
        String captcha = RandomUtil.randomNumbers(captchaMail.getLength());

        // 发送验证码
        Long expirationInMinutes = captchaMail.getExpirationInMinutes();
        String content = TemplateUtils.render(captchaMail.getTemplatePath(),
            Dict.create().set("captcha", captcha).set("expiration", expirationInMinutes));
        MailUtils.sendHtml(email, String.format("【%s】邮箱验证码", projectProperties.getName()), content);

        // 保存验证码
        String captchaKey = RedisUtils.formatKey(captchaKeyPrefix, email);
        RedisUtils.setCacheObject(captchaKey, captcha, Duration.ofMinutes(expirationInMinutes));
        RedisUtils.setCacheObject(limitCaptchaKey, captcha, Duration.ofSeconds(captchaMail.getLimitInSeconds()));
        return R.ok(String.format("发送成功，验证码有效期 %s 分钟", expirationInMinutes));
    }
}
