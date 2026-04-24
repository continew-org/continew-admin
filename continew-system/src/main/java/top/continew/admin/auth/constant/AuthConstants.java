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

package top.continew.admin.auth.constant;

/**
 * 认证相关常量
 *
 * @author Charles7c
 * @since 2025/7/26 12:05
 */
public class AuthConstants {

    /**
     * 登录 URI
     */
    public static final String LOGIN_URI = "/auth/login";

    /**
     * 登出 URI
     */
    public static final String LOGOUT_URI = "/auth/logout";

    /**
     * Map 存储登录用户信息时的 key 值
     */
    public static final String LOGIN_USER = "loginUser";

    private AuthConstants() {
    }
}
