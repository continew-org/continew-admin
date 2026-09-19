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

package top.continew.admin.auth.api;

/**
 * 认证会话常量。
 *
 * @author luoqiz
 */
public final class AuthSessionConstants {

    /** Access Token 中绑定 Refresh Session ID 的声明名称。 */
    public static final String SESSION_ID_CLAIM = "sid";

    /** 会话失效广播 Topic 前缀，完整 Topic 默认追加应用名：{prefix}:{spring.application.name} */
    public static final String ACCESS_SESSION_INVALID_TOPIC_PREFIX = "auth:access-session-invalid";

    private AuthSessionConstants() {
    }
}
