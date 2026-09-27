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
import lombok.NoArgsConstructor;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * Redis 中保存的 Refresh Token 会话。
 *
 * <p>一条记录对应一次登录会话。Refresh Token 使用 {@code sessionId.secret} 格式，
 * Redis 只保存 secret 的指纹，轮换只需要原子更新本对象。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Data
@NoArgsConstructor
public class RefreshSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 登录会话 ID，同时是 Refresh Token 的公开定位部分。 */
    private String sessionId;

    /** 用户 ID */
    private Long userId;

    /** 登录时的用户名快照，用于登录会话管理。 */
    private String username;

    /** 登录时的用户昵称快照，用于登录会话管理。 */
    private String nickname;

    /** 客户端 ID */
    private String clientId;

    /** 客户端类型，用于执行同类型设备互斥登录策略。 */
    private String clientType;

    /** 登录时确定的租户 ID，避免刷新时跨租户使用 */
    private Long tenantId;

    /** 当前客户端使用的 Refresh Token 传输模式 */
    private RefreshTokenModeEnum mode;

    /** 登录会话创建时间（毫秒时间戳），用于最大登录数量的稳定淘汰顺序。 */
    private long createdAt;

    /** 最近一次成功发起令牌轮换的时间（毫秒时间戳）。 */
    private long lastRefreshAt;

    /** 初次登录 IP。 */
    private String ip;

    /** 初次登录 IP 归属地。 */
    private String address;

    /** 初次登录浏览器或客户端。 */
    private String browser;

    /** 初次登录操作系统。 */
    private String os;

    /** 整个登录会话的绝对过期时间（毫秒时间戳），轮换不会无限延长会话寿命 */
    private long expiresAt;

    /** 创建会话时的用户安全版本。 */
    private long userSecurityVersion;

    /** 创建会话时的客户端安全版本。 */
    private long clientSecurityVersion;

    /** 创建会话时的租户安全版本。 */
    private long tenantSecurityVersion;

    /** 当前 Refresh Token secret 的 HMAC-SHA256 指纹。 */
    private String currentTokenFingerprint;

    /** 上一个 Refresh Token secret 的指纹，仅用于识别最近一次重放。 */
    private String previousTokenFingerprint;

}
