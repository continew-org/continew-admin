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

package top.continew.admin.auth.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 三方账号授权认证响应信息
 *
 * @author Charles7c
 * @since 2024/3/6 22:26
 */
@Data
@Builder
@Schema(description = "三方账号授权认证响应信息")
public class SocialAuthAuthorizeResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 授权 URL
     */
    @Schema(description = "授权 URL", example = "https://gitee.com/oauth/authorize?response_type=code&client_id=5d271b7f638941812aaf8bfc2e2f08f06d6235ef934e0e39537e2364eb8452c4&redirect_uri=http://localhost:5173/social/callback?source=gitee&state=d4ea7129e2531050210e9c918cc007d7&scope=user_info")
    private String authorizeUrl;
}
