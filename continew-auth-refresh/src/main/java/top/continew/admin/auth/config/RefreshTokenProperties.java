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

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Refresh Token 配置。
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "auth.refresh-token")
public class RefreshTokenProperties {

    private static final String DNS_LABEL = "[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?";
    private static final java.util.regex.Pattern WILDCARD_COOKIE_ORIGIN =
        java.util.regex.Pattern.compile(
            "^https?://\\*\\.(" + DNS_LABEL + "(?:\\." + DNS_LABEL + ")+)$",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    /**
     * Refresh Token 服务端密钥。
     *
     * <p>用于派生 Token 指纹密钥和轮换快照加密密钥。生产环境必须通过
     * {@code REFRESH_TOKEN_SECRET} 单独配置高熵随机值。</p>
     */
    @NotBlank(message = "Refresh Token 服务端密钥不能为空")
    @Size(min = 32, message = "Refresh Token 服务端密钥长度不能少于 32 个字符")
    private String secret;

    /** 浏览器 Refresh Token Cookie 名称 */
    @NotBlank(message = "Refresh Token Cookie 名称不能为空")
    private String cookieName = "refresh_token";

    /**
     * Cookie 作用路径。
     *
     * <p>前端开发环境通常通过 /api 或 /dev-api 代理访问后端，浏览器判断 Cookie
     * Path 时使用的是代理后的前端 URL，因此不能设置为 /auth，否则刷新请求不会携带
     * Cookie。生产环境如使用固定网关前缀，可通过配置覆盖该值。</p>
     */
    @NotBlank(message = "Refresh Token Cookie Path 不能为空")
    private String cookiePath = "/";

    /** 生产环境必须开启 Secure；开发环境可关闭以支持 HTTP 本地调试 */
    private boolean cookieSecure;

    /** Cookie SameSite 属性 */
    @Pattern(regexp = "(?i)Strict|Lax|None", message = "Cookie SameSite 只能是 Strict、Lax 或 None")
    private String cookieSameSite = "Lax";

    /**
     * 允许携带 Refresh Token Cookie 的跨源前端 Origin。
     *
     * <p>空列表表示只允许请求自身同源。每一项可以是无路径、查询和片段的明确 HTTP(S)
     * Origin，或格式为 {@code http[s]://*.example.com} 的子域通配符。通配符仅匹配最左侧
     * 的一个 DNS 标签，例如 {@code http://*.luoqiz.top} 可匹配
     * {@code http://admin.luoqiz.top}。</p>
     */
    private List<String> cookieAllowedOrigins = new ArrayList<>();

    /** SameSite=None 只有在 HTTPS 下配合 Secure 才能被浏览器接受。 */
    @AssertTrue(message = "Cookie SameSite=None 时必须同时开启 Secure")
    public boolean isCookieSecurityValid() {
        return !"None".equalsIgnoreCase(cookieSameSite) || cookieSecure;
    }

    /** __Host- Cookie 必须满足浏览器规定的 Secure + Path=/ 约束。 */
    @AssertTrue(message = "__Host- Refresh Token Cookie 必须开启 Secure 且 Path=/")
    public boolean isHostCookieValid() {
        return !cookieName.startsWith("__Host-") || cookieSecure && "/".equals(cookiePath);
    }

    /** Cookie 来源白名单必须是严格 HTTP(S) Origin 或受限的子域通配符。 */
    @AssertTrue(
        message = "Refresh Token Cookie 允许来源必须是明确的 HTTP(S) Origin 或 http[s]://*.example.com 格式的子域通配符")
    public boolean isCookieAllowedOriginsValid() {
        return cookieAllowedOrigins != null && cookieAllowedOrigins.stream()
            .allMatch(this::isValidAllowedOrigin);
    }

    /** 同一个旧 Token 的并发请求可重放第一次轮换结果的时间（秒）。 */
    @Min(value = 1, message = "Refresh Token 轮换宽限期不能少于 1 秒")
    @Max(value = 30, message = "Refresh Token 轮换宽限期不能超过 30 秒")
    private int rotationGracePeriod = 5;

    /** 刷新接口单个 IP 在限流窗口内允许的最大请求数，0 表示交由网关限流。 */
    @Min(value = 0, message = "Refresh Token IP 限流次数不能小于 0")
    @Max(value = 10000, message = "Refresh Token IP 限流次数不能超过 10000")
    private int ipRateLimit = 60;

    /** Refresh Token IP 限流窗口（秒）。 */
    @Min(value = 1, message = "Refresh Token IP 限流窗口不能少于 1 秒")
    @Max(value = 3600, message = "Refresh Token IP 限流窗口不能超过 3600 秒")
    private int ipRateLimitPeriod = 60;

    /** 单个登录会话在限流窗口内允许的最大刷新次数。 */
    @Min(value = 1, message = "Refresh Token 会话限流次数不能少于 1")
    @Max(value = 1000, message = "Refresh Token 会话限流次数不能超过 1000")
    private int sessionRateLimit = 10;

    /** 单个登录会话刷新限流窗口（秒）。 */
    @Min(value = 1, message = "Refresh Token 会话限流窗口不能少于 1 秒")
    @Max(value = 3600, message = "Refresh Token 会话限流窗口不能超过 3600 秒")
    private int sessionRateLimitPeriod = 60;

    /** 允许解析转发地址的反向代理地址列表；空列表时始终使用连接对端地址。 */
    private List<String> trustedProxyAddresses = new ArrayList<>();

    /** 可信代理追加到 X-Forwarded-For 的跳数；0 表示不解析转发地址。 */
    @Min(value = 0, message = "可信代理跳数不能小于 0")
    @Max(value = 10, message = "可信代理跳数不能超过 10")
    private int trustedProxyHops;

    /**
     * 热路径会话校验本地缓存开关。
     *
     * <p>开启后，每个已登录请求的会话校验命中本地缓存为 0 次 Redis 往返；会话撤销经
     * 广播子秒级失效，最坏情况由本地缓存 TTL（2 秒）兜底。关闭则恢复逐请求实时校验
     * （每次 2 次 Redis 往返，撤销立即生效）。</p>
     */
    private boolean accessSessionCacheEnabled = true;

    /**
     * 会话失效广播 Topic 名称（显式覆盖）。
     *
     * <p>留空（默认）时使用 {@code auth:access-session-invalid:{spring.application.name}}：
     * 同一服务多副本共用 Topic、不同服务天然隔离。仅当多个服务有意共享同一认证会话域
     * （例如网关与资源服务拆分、共用同一套 Refresh Session）时才显式配置为公共 Topic。
     * 必须与 jetcache 的 broadcastChannel 取值不同，二者消息载荷不兼容。</p>
     */
    private String accessSessionInvalidTopic = "";

    /**
     * 本实例 WebSocket 连接凭证周期校验间隔（毫秒）。
     *
     * <p>兜底 Redis Pub/Sub 撤销消息丢失和 Access Token 自然过期；默认 60 秒，
     * 与撤销广播配合时，未能及时收到通知的连接最迟在该间隔内被关闭。</p>
     */
    @Min(value = 1000, message = "WebSocket 连接校验间隔不能少于 1000 毫秒")
    @Max(value = 3600000, message = "WebSocket 连接校验间隔不能超过 3600000 毫秒")
    private long websocketValidationInterval = 60000;

    private boolean isValidAllowedOrigin(String value) {
        if (value == null || value.isBlank() || !value.equals(value.trim())
            || "*".equals(value)) {
            return false;
        }
        return this.isValidOrigin(value) || WILDCARD_COOKIE_ORIGIN.matcher(value).matches();
    }

    /**
     * 校验字符串是否为严格的无路径/查询/片段的 HTTP(S) Origin。
     *
     * <p>请求守卫的 {@code parseOrigin} 与本方法共用同一份语义，避免两份校验漂移：
     * 端口只允许省略（-1）或 1-65535（拒绝 0），并拒绝 {@code https://host:} 这类
     * 以冒号结尾的空端口写法。</p>
     *
     * @param value 待校验的 Origin
     * @return true：合法 HTTP(S) Origin；false：非法
     */
    public static boolean isValidOrigin(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            int port = uri.getPort();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                && uri.getHost() != null
                && uri.getUserInfo() == null
                && (uri.getRawPath() == null || uri.getRawPath().isEmpty())
                && uri.getRawQuery() == null
                && uri.getRawFragment() == null
                && (port == -1 || port > 0 && port <= 65535)
                && !uri.getRawAuthority().endsWith(":");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
