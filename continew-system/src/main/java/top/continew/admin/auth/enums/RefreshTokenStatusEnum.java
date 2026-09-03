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
 * Refresh Token 会话状态。
 *
 * @author luoqiz
 */
public enum RefreshTokenStatusEnum {

    /** 当前 Token 仍可使用 */
    ACTIVE,

    /** 当前 Token 已经被轮换，只允许用于识别重放攻击 */
    USED,

    /** 当前 Token 或所属会话已被撤销 */
    REVOKED
}
