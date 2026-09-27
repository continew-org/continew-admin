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

/**
 * 非并发登录时替换既有登录会话的范围。
 *
 * @author luoqiz
 */
public enum SessionReplacementScope {

    /** 仅替换相同客户端类型的会话。 */
    CURRENT_CLIENT_TYPE,

    /** 替换该用户的所有客户端类型会话。 */
    ALL_CLIENT_TYPES
}
