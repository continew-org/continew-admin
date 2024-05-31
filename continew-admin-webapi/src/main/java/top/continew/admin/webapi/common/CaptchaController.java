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

package top.continew.admin.webapi.common;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.wf.captcha.base.Captcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.redisson.api.RateType;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.continew.admin.common.config.properties.CaptchaProperties;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.model.resp.CaptchaResp;
import top.continew.admin.system.service.OptionService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.captcha.graphic.core.GraphicCaptchaService;
import top.continew.starter.core.autoconfigure.project.ProjectProperties;
import top.continew.starter.core.util.TemplateUtils;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.core.util.validate.ValidationUtils;
import top.continew.starter.log.core.annotation.Log;
import top.continew.starter.messaging.mail.util.MailUtils;
import top.continew.starter.web.model.R;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
@RequestMapping("/captcha")
public class CaptchaController {

    private final ProjectProperties projectProperties;
    private final CaptchaProperties captchaProperties;
    private final CaptchaService behaviorCaptchaService;
    private final GraphicCaptchaService graphicCaptchaService;
    private final OptionService optionService;

    @Log(ignore = true)
    @Operation(summary = "获取行为验证码", description = "获取行为验证码（Base64编码）")
    @GetMapping("/behavior")
    public R<Object> getBehaviorCaptcha(CaptchaVO captchaReq, HttpServletRequest request) {
        captchaReq.setBrowserInfo(JakartaServletUtil.getClientIP(request) + request.getHeader(HttpHeaders.USER_AGENT));
        ResponseModel responseModel = behaviorCaptchaService.get(captchaReq);
        CheckUtils.throwIf(() -> !StrUtil.equals(RepCodeEnum.SUCCESS.getCode(), responseModel
            .getRepCode()), responseModel.getRepMsg());
        return R.ok(responseModel.getRepData());
    }

    @Log(ignore = true)
    @Operation(summary = "校验行为验证码", description = "校验行为验证码")
    @PostMapping("/behavior")
    public R<Object> checkBehaviorCaptcha(@RequestBody CaptchaVO captchaReq, HttpServletRequest request) {
        captchaReq.setBrowserInfo(JakartaServletUtil.getClientIP(request) + request.getHeader(HttpHeaders.USER_AGENT));
        return R.ok(behaviorCaptchaService.check(captchaReq));
    }

    @Log(ignore = true)
    @Operation(summary = "获取图片验证码", description = "获取图片验证码（Base64编码，带图片格式：data:image/gif;base64）")
    @GetMapping("/image")
    public R<CaptchaResp> getImageCaptcha() {
        String uuid = IdUtil.fastUUID();
        String captchaKey = CacheConstants.CAPTCHA_KEY_PREFIX + uuid;
        Captcha captcha = graphicCaptchaService.getCaptcha();
        long expireTime = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()
            .plusMinutes(captchaProperties.getExpirationInMinutes()));
        RedisUtils.set(captchaKey, captcha.text(), Duration.ofMinutes(captchaProperties.getExpirationInMinutes()));
        return R.ok(CaptchaResp.builder().uuid(uuid).img(captcha.toBase64()).expireTime(expireTime).build());
    }

    @Operation(summary = "获取邮箱验证码", description = "发送验证码到指定邮箱")
    @GetMapping("/mail")
    public R<Void> getMailCaptcha(@NotBlank(message = "邮箱不能为空") @Pattern(regexp = RegexPool.EMAIL, message = "邮箱格式错误") String email) throws MessagingException {
        String limitKeyPrefix = CacheConstants.LIMIT_KEY_PREFIX;
        String captchaKeyPrefix = CacheConstants.CAPTCHA_KEY_PREFIX;
        String limitCaptchaKey = limitKeyPrefix + captchaKeyPrefix + email;
        long limitTimeInMillisecond = RedisUtils.getTimeToLive(limitCaptchaKey);
        CheckUtils.throwIf(limitTimeInMillisecond > 0, "发送验证码过于频繁，请您 {}s 后再试", limitTimeInMillisecond / 1000);
        // 生成验证码
        CaptchaProperties.CaptchaMail captchaMail = captchaProperties.getMail();
        String captcha = RandomUtil.randomNumbers(captchaMail.getLength());
        // 发送验证码
        Long expirationInMinutes = captchaMail.getExpirationInMinutes();
        Map<String, String> siteConfig = optionService.getByCategory("SITE");
        String content = TemplateUtils.render(captchaMail.getTemplatePath(), Dict.create()
            .set("siteUrl", projectProperties.getUrl())
            .set("siteTitle", siteConfig.get("SITE_TITLE"))
            .set("siteCopyright", siteConfig.get("SITE_COPYRIGHT"))
            .set("captcha", captcha)
            .set("expiration", expirationInMinutes));
        MailUtils.sendHtml(email, "【%s】邮箱验证码".formatted(projectProperties.getName()), content);
        // 保存验证码
        String captchaKey = captchaKeyPrefix + email;
        RedisUtils.set(captchaKey, captcha, Duration.ofMinutes(expirationInMinutes));
        RedisUtils.set(limitCaptchaKey, captcha, Duration.ofSeconds(captchaMail.getLimitInSeconds()));
        return R.ok("发送成功，验证码有效期 %s 分钟".formatted(expirationInMinutes));
    }

    @Operation(summary = "获取短信验证码", description = "发送验证码到指定手机号")
    @GetMapping("/sms")
    public R<Void> getSmsCaptcha(@NotBlank(message = "手机号不能为空") @Pattern(regexp = RegexPool.MOBILE, message = "手机号格式错误") String phone,
                                 CaptchaVO captchaReq,
                                 HttpServletRequest request) {
        // 行为验证码校验
        ResponseModel verificationRes = behaviorCaptchaService.verification(captchaReq);
        ValidationUtils.throwIfNotEqual(verificationRes.getRepCode(), RepCodeEnum.SUCCESS.getCode(), verificationRes
            .getRepMsg());
        CaptchaProperties.CaptchaSms captchaSms = captchaProperties.getSms();
        String templateId = captchaSms.getTemplateId();
        String limitKeyPrefix = CacheConstants.LIMIT_KEY_PREFIX;
        String captchaKeyPrefix = CacheConstants.CAPTCHA_KEY_PREFIX;
        String limitTemplateKeyPrefix = limitKeyPrefix + captchaKeyPrefix;
        // 限制短信发送频率
        // 1.同一号码同一短信模板，1分钟2条，1小时8条，24小时20条，e.g. LIMIT:CAPTCHA:XXX:188xxxxx:1
        final String errorMsg = "获取验证码操作太频繁，请稍后再试";
        CheckUtils.throwIf(!RedisUtils.rateLimit(RedisUtils
            .formatKey(limitTemplateKeyPrefix + "MIN", phone, templateId), RateType.OVERALL, 2, 60), errorMsg);
        CheckUtils.throwIf(!RedisUtils.rateLimit(RedisUtils
            .formatKey(limitTemplateKeyPrefix + "HOUR", phone, templateId), RateType.OVERALL, 8, 60 * 60), errorMsg);
        CheckUtils.throwIf(!RedisUtils.rateLimit(RedisUtils
            .formatKey(limitTemplateKeyPrefix + "DAY", phone, templateId), RateType.OVERALL, 20, 60 * 60 * 24), errorMsg);
        // 2.同一号码所有短信模板 24 小时 100 条，e.g. LIMIT:CAPTCHA:188xxxxx
        String limitPhoneKey = limitKeyPrefix + captchaKeyPrefix + phone;
        CheckUtils.throwIf(!RedisUtils.rateLimit(limitPhoneKey, RateType.OVERALL, 100, 60 * 60 * 24), errorMsg);
        // 3.同一 IP 每分钟限制发送 30 条，e.g. LIMIT:CAPTCHA:PHONE:1xx.1xx.1xx.1xx
        String limitIpKey = RedisUtils.formatKey(limitKeyPrefix + captchaKeyPrefix + "PHONE", JakartaServletUtil
            .getClientIP(request));
        CheckUtils.throwIf(!RedisUtils.rateLimit(limitIpKey, RateType.OVERALL, 30, 60), errorMsg);
        // 生成验证码
        String captcha = RandomUtil.randomNumbers(captchaSms.getLength());
        // 发送验证码
        Long expirationInMinutes = captchaSms.getExpirationInMinutes();
        SmsBlend smsBlend = SmsFactory.getBySupplier(SupplierConstant.CLOOPEN);
        Map<String, String> messageMap = MapUtil.newHashMap(2, true);
        messageMap.put("captcha", captcha);
        messageMap.put("expirationInMinutes", String.valueOf(expirationInMinutes));
        SmsResponse smsResponse = smsBlend.sendMessage(phone, captchaSms
            .getTemplateId(), (LinkedHashMap<String, String>)messageMap);
        CheckUtils.throwIf(!smsResponse.isSuccess(), "验证码发送失败");
        // 保存验证码
        String captchaKey = captchaKeyPrefix + phone;
        RedisUtils.set(captchaKey, captcha, Duration.ofMinutes(expirationInMinutes));
        return R.ok("发送成功，验证码有效期 %s 分钟".formatted(expirationInMinutes));
    }
}
