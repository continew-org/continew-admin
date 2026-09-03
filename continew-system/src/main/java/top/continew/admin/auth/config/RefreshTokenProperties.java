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

package top.continew.admin.auth.config;

import lombok.Data;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Refresh Token 配置。
 *
 * @author luoqiz
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "auth.refresh-token")
public class RefreshTokenProperties {

    /** 全局开关；具体客户端还需通过 sys_client.is_enable_refresh_token 开启 */
    private boolean enabled = true;

    /** 浏览器 Refresh Token Cookie 名称 */
    private String cookieName = "refresh_token";

    /**
     * Cookie 作用路径。
     *
     * <p>前端开发环境通常通过 /api 或 /dev-api 代理访问后端，浏览器判断 Cookie
     * Path 时使用的是代理后的前端 URL，因此不能设置为 /auth，否则刷新请求不会携带
     * Cookie。生产环境如使用固定网关前缀，可通过配置覆盖该值。</p>
     */
    private String cookiePath = "/";

    /** 生产环境必须开启 Secure；开发环境可关闭以支持 HTTP 本地调试 */
    private boolean cookieSecure;

    /** Cookie SameSite 属性 */
    @Pattern(regexp = "(?i)Strict|Lax|None", message = "Cookie SameSite 只能是 Strict、Lax 或 None")
    private String cookieSameSite = "Lax";

    /** 未配置客户端刷新时长时的默认值（30 天） */
    private long defaultTimeout = 2_592_000L;
}
