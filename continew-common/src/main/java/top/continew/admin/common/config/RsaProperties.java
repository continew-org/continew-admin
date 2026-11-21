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

package top.continew.admin.common.config;

import cn.hutool.extra.spring.SpringUtil;

/**
 * RSA 配置属性
 *
 * @author Zheng Jie（ELADMIN）
 * @author Charles7c
 * @since 1.0.0
 */
public class RsaProperties {

    /**
     * 私钥
     */
    public static final String PRIVATE_KEY;
    public static final String PUBLIC_KEY;

    static {
        PRIVATE_KEY = SpringUtil.getProperty("continew-starter.encrypt.field.private-key");
        PUBLIC_KEY = SpringUtil.getProperty("continew-starter.encrypt.field.public-key");
    }

    /**
     * 首次调用时才读 Spring 环境：RsaProperties 可能先于 SpringUtil 注入 ApplicationContext 被加载，
     * 静态块会把 null 固化为终值（登录密码解密失败即此原因），故调用侧改用懒读取。
     */
    public static String privateKey() {
        return PRIVATE_KEY != null
            ? PRIVATE_KEY
            : SpringUtil.getProperty("continew-starter.encrypt.field.private-key");
    }

    public static String publicKey() {
        return PUBLIC_KEY != null
            ? PUBLIC_KEY
            : SpringUtil.getProperty("continew-starter.encrypt.field.public-key");
    }

    private RsaProperties() {
    }
}
