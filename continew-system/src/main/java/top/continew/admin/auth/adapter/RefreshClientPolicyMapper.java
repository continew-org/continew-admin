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

package top.continew.admin.auth.adapter;

import top.continew.admin.auth.enums.LogoutReasonEnum;
import top.continew.admin.auth.enums.SessionReplacementScope;
import top.continew.admin.auth.model.RefreshClientPolicy;
import top.continew.admin.system.enums.LogoutModeEnum;
import top.continew.admin.system.enums.ReplacedRangeEnum;
import top.continew.admin.system.model.resp.ClientResp;

/**
 * 系统客户端配置到认证会话策略的边界转换器。
 *
 * @author luoqiz
 */
public final class RefreshClientPolicyMapper {

    private RefreshClientPolicyMapper() {
    }

    /**
     * 将系统客户端响应转换为认证会话模块的最小策略模型。
     *
     * @param client 系统客户端配置
     * @return 认证会话客户端策略
     */
    public static RefreshClientPolicy from(ClientResp client) {
        SessionReplacementScope replacementScope = client.getReplacedRange() == null ? null
            : ReplacedRangeEnum.ALL_DEVICE_TYPE.equals(client.getReplacedRange())
                ? SessionReplacementScope.ALL_CLIENT_TYPES
                : SessionReplacementScope.CURRENT_CLIENT_TYPE;
        return new RefreshClientPolicy(client.getClientId(), client.getClientType(),
            client.getRefreshTokenTimeout(), client.getRefreshTokenMode(),
            Boolean.TRUE.equals(client.getIsConcurrent()), replacementScope,
            client.getMaxLoginCount(), toLogoutReason(client.getOverflowLogoutMode()));
    }

    /** 客户端配置的注销模式决定登录数量超限时被淘汰会话看到的提示。 */
    private static LogoutReasonEnum toLogoutReason(LogoutModeEnum overflowLogoutMode) {
        if (overflowLogoutMode == null) {
            return LogoutReasonEnum.REPLACED;
        }
        try {
            return LogoutReasonEnum.valueOf(overflowLogoutMode.getValue());
        } catch (IllegalArgumentException e) {
            return LogoutReasonEnum.REPLACED;
        }
    }
}
