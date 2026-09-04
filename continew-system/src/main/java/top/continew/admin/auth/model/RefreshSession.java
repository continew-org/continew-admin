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
import top.continew.admin.auth.enums.RefreshTokenStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * Redis 中保存的 Refresh Token 会话。
 *
 * <p>Refresh Token 本身只在客户端持有，Redis 仅以哈希值作为 Key。此对象保存的是
 * Token 的业务元数据，用于轮换、租户校验、设备会话管理和撤销。</p>
 *
 * @author luoqiz
 */
@Data
@NoArgsConstructor
public class RefreshSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前 Refresh Token 的唯一编号；每次轮换都会生成新的 jti */
    private String jti;

    /**
     * 登录会话家族编号。
     *
     * <p>同一次登录产生的所有 Refresh Token 共用 familyId。它不是密钥，不能用于刷新，
     * 仅用于在 Token 重放、单设备退出或强制下线时一次性撤销整条 Token 链。</p>
     */
    private String familyId;

    /** 用户 ID */
    private Long userId;

    /** 客户端 ID */
    private String clientId;

    /** 登录客户端类型 */
    private String clientType;

    /** 登录时确定的租户 ID，避免刷新时跨租户使用 */
    private Long tenantId;

    /** 当前客户端使用的 Refresh Token 传输模式 */
    private RefreshTokenModeEnum mode;

    /** 创建时间（毫秒时间戳） */
    private long issuedAt;

    /** 整个登录会话的绝对过期时间（毫秒时间戳），轮换不会无限延长会话寿命 */
    private long expiresAt;

    /** 最近一次成功刷新时间（毫秒时间戳） */
    private long lastUsedAt;

    /** 当前 Token 状态 */
    private RefreshTokenStatusEnum status;

    /** 轮换后新 Token 的 jti，用于审计和重放检测 */
    private String replacedBy;

    /** 客户端安装或设备标识，仅用于设备会话管理，不作为唯一安全凭证 */
    private String deviceId;

    /** 登录 IP */
    private String ip;

    /** 登录 User-Agent */
    private String userAgent;
}
