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

package top.continew.admin.auth.support;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.config.RefreshTokenProperties;
import top.continew.admin.auth.enums.RefreshTokenModeEnum;
import top.continew.admin.auth.model.RefreshSession;
import top.continew.admin.auth.exception.RefreshTokenException;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Refresh Token HTTP 请求守卫。
 *
 * <p>集中负责 Cookie/BODY 传输边界、Cookie 来源校验、刷新限流和禁止缓存响应，
 * 避免这些安全规则散落在令牌轮换流程中。</p>
 *
 * @author luoqiz
 */
@Component
public class RefreshTokenRequestGuard {

    private static final String RATE_LIMIT_PREFIX = "AUTH:REFRESH:V1:RATE_LIMIT:";

    private final RefreshTokenProperties properties;
    private final RefreshTokenCodec tokenCodec;
    private final RefreshSessionStore sessionStore;
    private final List<Pattern> cookieAllowedOriginMatchers;

    public RefreshTokenRequestGuard(RefreshTokenProperties properties, RefreshTokenCodec tokenCodec,
        RefreshSessionStore sessionStore) {
        this.properties = properties;
        this.tokenCodec = tokenCodec;
        this.sessionStore = sessionStore;
        this.cookieAllowedOriginMatchers = properties.getCookieAllowedOrigins().stream()
            .map(this::compileAllowedOriginMatcher)
            .toList();
    }

    /** 从 Cookie 或 BODY 中读取 Refresh Token；同时出现两种来源时拒绝请求。 */
    public String resolve(String bodyRefreshToken, HttpServletRequest request) {
        String bodyToken = StrUtil.trim(bodyRefreshToken);
        String cookieToken = this.readCookie(request);
        if (StrUtil.isNotBlank(bodyToken) && StrUtil.isNotBlank(cookieToken)) {
            throw RefreshTokenException.forbidden("Refresh Token 来源冲突，请重新登录");
        }
        return StrUtil.isNotBlank(bodyToken) ? bodyToken : cookieToken;
    }

    /** 在解析不可信 Token 之前按客户端地址执行刷新限流。 */
    public void checkRequestRateLimit(HttpServletRequest request) {
        int ipLimit = properties.getIpRateLimit();
        if (ipLimit <= 0) {
            return;
        }
        String clientIp = request == null ? "unknown" : this.resolveClientIp(request);
        clientIp = StrUtil.blankToDefault(clientIp, "unknown");
        this.requireRateLimit("IP:" + tokenCodec.fingerprint(clientIp), ipLimit);
    }

    /** 对真正产生新令牌的一次 Session 轮换执行限流。 */
    public void checkSessionRateLimit(String sessionId) {
        this.requireRateLimit("SESSION:" + sessionId, properties.getSessionRateLimit(),
            Duration.ofSeconds(properties.getSessionRateLimitPeriod()));
    }

    /** 校验已识别 Refresh Session 的令牌传输方式和 Cookie 请求来源。 */
    public void validateRequest(String rawRefreshToken, HttpServletRequest request) {
        if (StrUtil.isBlank(rawRefreshToken) || request == null) {
            return;
        }
        RefreshTokenCodec.ParsedToken token = tokenCodec.parse(rawRefreshToken);
        RefreshSession session = sessionStore.get(token.sessionId());
        // 已轮换多次的旧 Token 可能不再是 current/previous，但仍可在极短宽限期内命中
        // 幂等快照。此类请求同样必须执行 Cookie 来源校验，不能因走重放快照而绕过 CSRF。
        if (session == null || !this.isKnownToken(session, token.fingerprint())
            && sessionStore.getRotation(token.fingerprint()) == null) {
            return;
        }
        String cookieToken = this.readCookie(request);
        if (RefreshTokenModeEnum.COOKIE.equals(session.getMode())) {
            if (!Objects.equals(rawRefreshToken, cookieToken) || !this.isTrustedOrigin(request)) {
                throw RefreshTokenException.forbidden("请求来源不合法，请重新登录");
            }
            return;
        }
        if (StrUtil.isNotBlank(cookieToken)) {
            throw RefreshTokenException.forbidden("Refresh Token 传输方式不合法，请重新登录");
        }
    }

    /** 请求携带 Refresh Token Cookie 时，在解析 Cookie 前校验请求来源。 */
    public void validateCookieOrigin(HttpServletRequest request) {
        if (this.hasRefreshTokenCookie(request) && !this.isTrustedOrigin(request)) {
            throw RefreshTokenException.forbidden("请求来源不合法，请重新登录");
        }
    }

    /** 写入浏览器 HttpOnly Refresh Token Cookie。 */
    public void writeCookie(HttpServletResponse response, String rawToken, long ttlSeconds) {
        ResponseCookie cookie = ResponseCookie.from(properties.getCookieName(), rawToken)
            .httpOnly(true)
            .secure(properties.isCookieSecure())
            .sameSite(properties.getCookieSameSite())
            .path(properties.getCookiePath())
            .maxAge(Duration.ofSeconds(ttlSeconds))
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** 清理浏览器 Refresh Token Cookie。 */
    public void clearCookie(HttpServletResponse response) {
        this.disableCaching(response);
        ResponseCookie cookie = ResponseCookie.from(properties.getCookieName(), "")
            .httpOnly(true)
            .secure(properties.isCookieSecure())
            .sameSite(properties.getCookieSameSite())
            .path(properties.getCookiePath())
            .maxAge(Duration.ZERO)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** 禁止浏览器和中间代理缓存认证响应。 */
    public void disableCaching(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }

    private String readCookie(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return null;
        }
        String value = null;
        for (Cookie cookie : request.getCookies()) {
            if (!properties.getCookieName().equals(cookie.getName())) {
                continue;
            }
            if (value != null) {
                throw RefreshTokenException.forbidden("Refresh Token Cookie 重复，请重新登录");
            }
            value = cookie.getValue();
        }
        return value;
    }

    private boolean hasRefreshTokenCookie(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return false;
        }
        for (Cookie cookie : request.getCookies()) {
            if (properties.getCookieName().equals(cookie.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isKnownToken(RefreshSession session, String fingerprint) {
        return tokenCodec.matches(fingerprint, session.getCurrentTokenFingerprint())
            || tokenCodec.matches(fingerprint, session.getPreviousTokenFingerprint());
    }

    private void requireRateLimit(String keySuffix, int limit) {
        this.requireRateLimit(keySuffix, limit,
            Duration.ofSeconds(properties.getIpRateLimitPeriod()));
    }

    private void requireRateLimit(String keySuffix, int limit, Duration period) {
        boolean allowed = sessionStore.tryAcquireRateLimit(RATE_LIMIT_PREFIX + keySuffix, limit,
            period);
        if (!allowed) {
            throw RefreshTokenException.tooManyRequests("刷新请求过于频繁，请稍后重试");
        }
    }

    private boolean isTrustedOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (StrUtil.isNotBlank(origin)) {
            return this.isAllowedOrigin(origin, request);
        }
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (StrUtil.isBlank(referer)) {
            return false;
        }
        try {
            URI refererUri = URI.create(referer);
            String scheme = refererUri.getScheme();
            String host = refererUri.getHost();
            if (StrUtil.isBlank(scheme) || StrUtil.isBlank(host)
                || !"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }
            int port = refererUri.getPort();
            String refererOrigin = scheme + "://" + host + (port < 0 ? "" : ":" + port);
            return this.isAllowedOrigin(refererOrigin, request);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddress = StrUtil.blankToDefault(request.getRemoteAddr(), "unknown");
        int trustedProxyHops = properties.getTrustedProxyHops();
        if (trustedProxyHops <= 0
            || !properties.getTrustedProxyAddresses().contains(remoteAddress)) {
            return remoteAddress;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(forwardedFor)) {
            return remoteAddress;
        }
        List<String> addresses = new ArrayList<>();
        for (String address : StrUtil.splitTrim(forwardedFor, ',')) {
            if (StrUtil.isNotBlank(address)) {
                addresses.add(address);
            }
        }
        int clientIndex = addresses.size() - trustedProxyHops;
        return clientIndex < 0 ? remoteAddress : addresses.get(clientIndex);
    }

    private boolean isAllowedOrigin(String origin, HttpServletRequest request) {
        URI originUri = this.parseOrigin(origin);
        if (originUri == null) {
            return false;
        }
        String normalizedOrigin = this.normalizeOrigin(originUri);
        if (cookieAllowedOriginMatchers.stream()
            .anyMatch(pattern -> pattern.matcher(normalizedOrigin).matches())) {
            return true;
        }
        return request.getScheme().equalsIgnoreCase(originUri.getScheme())
            && request.getServerName().equalsIgnoreCase(originUri.getHost())
            && this.normalizePort(request.getScheme(), request.getServerPort()) == this
                .normalizePort(originUri.getScheme(), originUri.getPort());
    }

    private URI parseOrigin(String value) {
        // 与 RefreshTokenProperties.isValidOrigin 共用同一份语义，避免两份校验漂移。
        if (StrUtil.isBlank(value) || !RefreshTokenProperties.isValidOrigin(value)) {
            return null;
        }
        try {
            return URI.create(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Pattern compileAllowedOriginMatcher(String allowedOrigin) {
        URI allowedUri = this.parseOrigin(allowedOrigin);
        if (allowedUri != null) {
            return Pattern.compile(Pattern.quote(this.normalizeOrigin(allowedUri)),
                Pattern.CASE_INSENSITIVE);
        }
        int wildcardIndex = allowedOrigin.indexOf('*');
        String expression = Pattern.quote(allowedOrigin.substring(0, wildcardIndex))
            + "[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?"
            + Pattern.quote(allowedOrigin.substring(wildcardIndex + 1));
        return Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
    }

    private String normalizeOrigin(URI uri) {
        String scheme = uri.getScheme();
        int port = this.normalizePort(scheme, uri.getPort());
        boolean defaultPort = "https".equalsIgnoreCase(scheme) ? port == 443 : port == 80;
        return scheme + "://" + uri.getHost() + (defaultPort ? "" : ":" + port);
    }

    private int normalizePort(String scheme, int port) {
        if (port >= 0) {
            return port;
        }
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }
}
