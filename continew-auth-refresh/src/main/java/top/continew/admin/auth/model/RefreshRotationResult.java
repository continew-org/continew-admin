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
import java.io.Serial;
import java.io.Serializable;

/**
 * Refresh Token 短时轮换结果。
 *
 * <p>Redis 中的 Access Token 和 Refresh Token 均为 AES-GCM 密文，记录只在并发宽限期内
 * 存活，用于让浏览器多标签页、App 和小程序弱网重试得到完全相同的结果。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Data
@NoArgsConstructor
public class RefreshRotationResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String encryptedAccessToken;
    private String encryptedRefreshToken;
    private Long expiresIn;
    private Long refreshExpiresIn;
    private Long tenantId;

    /** 是否已经完成 Access Token 签发，可以直接幂等返回。 */
    public boolean isComplete() {
        return encryptedAccessToken != null;
    }
}
