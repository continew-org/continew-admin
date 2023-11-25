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

package top.charles7c.cnadmin.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 缓存相关常量
 *
 * @author Charles7c
 * @since 2022/12/22 19:30
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheConstants {

    /**
     * 登录用户键
     */
    public static final String LOGIN_USER_KEY = "LOGIN_USER";

    /**
     * 验证码键前缀
     */
    public static final String CAPTCHA_KEY_PREFIX = "CAPTCHA";

    /**
     * 限流键前缀
     */
    public static final String LIMIT_KEY_PREFIX = "LIMIT";

    /**
     * 用户缓存键前缀
     */
    public static final String USER_KEY_PREFIX = "USER";

    /**
     * 菜单缓存键前缀
     */
    public static final String MENU_KEY_PREFIX = "MENU";

    /**
     * 字典缓存键前缀
     */
    public static final String DICT_KEY_PREFIX = "DICT";

    /**
     * 参数缓存键前缀
     */
    public static final String OPTION_KEY_PREFIX = "OPTION";

    /**
     * 仪表盘缓存键前缀
     */
    public static final String DASHBOARD_KEY_PREFIX = "DASHBOARD";
}
