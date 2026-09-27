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

package top.continew.admin.auth.model;

import top.continew.admin.auth.enums.LogoutReasonEnum;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;
import top.continew.admin.auth.enums.SessionReplacementScope;

/**
 * 认证会话模块所需的客户端令牌策略。
 *
 * <p>该模型刻意不复用系统管理模块的 {@code ClientResp}，避免认证会话模块反向依赖
 * 用户、客户端等业务实体。</p>
 *
 * @author luoqiz
 */
public record RefreshClientPolicy(String clientId, String clientType, long refreshTokenTimeout,
    RefreshTokenModeEnum refreshTokenMode, boolean concurrent,
    SessionReplacementScope replacementScope, int maxLoginCount,
    LogoutReasonEnum overflowLogoutMode) {
}
