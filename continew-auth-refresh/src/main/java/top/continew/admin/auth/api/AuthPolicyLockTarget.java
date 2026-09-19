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
 * 认证会话安全策略锁的目标。
 *
 * @author luoqiz
 */
public record AuthPolicyLockTarget(Type type, String key) {

    public static AuthPolicyLockTarget client(String clientId) {
        return new AuthPolicyLockTarget(Type.CLIENT, clientId);
    }

    public static AuthPolicyLockTarget tenant(Long tenantId) {
        return new AuthPolicyLockTarget(Type.TENANT, String.valueOf(tenantId));
    }

    public static AuthPolicyLockTarget user(Long userId) {
        return new AuthPolicyLockTarget(Type.USER, String.valueOf(userId));
    }

    public enum Type {
        USER,
        TENANT,
        CLIENT
    }
}
