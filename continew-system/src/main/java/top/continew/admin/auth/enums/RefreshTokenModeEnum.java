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

package top.continew.admin.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

/**
 * Refresh Token 传输模式。
 *
 * <p>COOKIE 适用于浏览器：长期凭证由 HttpOnly Cookie 承载，前端 JavaScript 无法读取。
 * BODY 为 App、小程序等后续客户端预留，长期凭证由客户端安全存储后通过请求体提交。</p>
 *
 * @author luoqiz
 */
@Getter
@RequiredArgsConstructor
public enum RefreshTokenModeEnum implements BaseEnum<String> {

    /** 浏览器 Cookie 模式 */
    COOKIE("COOKIE", "Cookie"),

    /** 原生 App / 小程序请求体模式 */
    BODY("BODY", "请求体");

    private final String value;
    private final String description;
}
