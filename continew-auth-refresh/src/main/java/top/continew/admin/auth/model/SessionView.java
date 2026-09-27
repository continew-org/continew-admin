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

import lombok.Data;

/**
 * 可供系统管理模块使用的认证会话安全视图。
 *
 * <p>不包含 Refresh Token、指纹、安全版本和轮换快照。</p>
 *
 * @author luoqiz
 */
@Data
public class SessionView {

    private String sessionId;
    private Long userId;
    private String username;
    private String nickname;
    private String clientId;
    private String clientType;
    private Long tenantId;
    private long createdAt;
    private long lastRefreshAt;
    private String ip;
    private String address;
    private String browser;
    private String os;
    private long expiresAt;
}
