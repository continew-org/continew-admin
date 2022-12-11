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

package top.charles7c.cnadmin.auth.config.properties;

import java.awt.*;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.wf.captcha.*;
import com.wf.captcha.base.Captcha;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 验证码配置属性
 *
 * @author Charles7c
 * @since 2022/12/11 13:35
 */
@Data
@Component
@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    /**
     * 类型
     */
    private CaptchaTypeEnum type;

    /**
     * 缓存键的前缀
     */
    private String keyPrefix;

    /**
     * 过期时间
     */
    private Long expirationInMinutes = 2L;

    /**
     * 内容长度
     */
    private int length = 4;

    /**
     * 宽度
     */
    private int width = 111;

    /**
     * 高度
     */
    private int height = 36;

    /**
     * 字体
     */
    private String fontName;

    /**
     * 字体大小
     */
    private int fontSize = 25;

    /**
     * 获取验证码对象
     *
     * @return 验证码对象
     */
    public Captcha getCaptcha() {
        Captcha captcha = ReflectUtil.newInstance(type.getClazz(), this.width, this.height);
        captcha.setLen(length);
        if (StrUtil.isNotBlank(this.fontName)) {
            captcha.setFont(new Font(this.fontName, Font.PLAIN, this.fontSize));
        }
        return captcha;
    }

    /**
     * 验证码类型枚举
     */
    @Getter
    @RequiredArgsConstructor
    public enum CaptchaTypeEnum {

        /**
         * 算术
         */
        ARITHMETIC(ArithmeticCaptcha.class),

        /**
         * 中文
         */
        CHINESE(ChineseCaptcha.class),

        /**
         * 中文闪图
         */
        CHINESE_GIF(ChineseGifCaptcha.class),

        /**
         * 闪图
         */
        GIF(GifCaptcha.class),

        /**
         * 特殊类型
         */
        SPEC(SpecCaptcha.class),;

        /**
         * 验证码字节码类型
         */
        private final Class<? extends Captcha> clazz;
    }
}
