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
 * 登录会话失效原因。
 *
 * <p>Sa-Token 不再管理并发登录与顶人下线，客户端配置的注销模式只能由 Refresh Session
 * 在撤销会话时落到这里，才能把“被踢下线”“被顶下线”“已注销”区分给客户端。原因只用于
 * 改善提示文案：标记过期后请求会回落到 {@link #LOGOUT} 的默认提示。</p>
 *
 * @author luoqiz
 */
public enum LogoutReasonEnum {

    /** 主动注销或会话自然失效。 */
    LOGOUT("登录状态已失效，请重新登录"),

    /** 被管理员强退，或因用户、租户、客户端变更被强制下线。 */
    KICKOUT("您已被管理员强制下线，请重新登录"),

    /** 登录数量超限或不允许多地登录，被新登录顶下线。 */
    REPLACED("您的账号已在其他设备登录，请重新登录");

    private final String message;

    LogoutReasonEnum(String message) {
        this.message = message;
    }

    /** 返回给客户端的失效提示。 */
    public String getMessage() {
        return this.message;
    }
}
