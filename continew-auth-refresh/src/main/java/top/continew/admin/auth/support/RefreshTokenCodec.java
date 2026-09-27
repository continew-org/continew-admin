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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.config.RefreshTokenProperties;
import top.continew.admin.auth.exception.RefreshTokenException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Refresh Token 编解码器。
 *
 * <p>令牌格式固定为 {@code sessionId.secret}。sessionId 只负责定位服务端会话，
 * secret 才是凭证；服务端只保存 secret 的 HMAC-SHA256 指纹。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCodec {

    private static final int SESSION_ID_BYTES = 16;
    private static final int SECRET_BYTES = 32;
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int MAX_TOKEN_LENGTH = 96;
    private static final byte[] ENCRYPTION_AAD =
        "continew-refresh-rotation-v1".getBytes(StandardCharsets.UTF_8);
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]{22}");
    private static final Pattern SECRET_PATTERN = Pattern.compile("[A-Za-z0-9_-]{43}");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenProperties properties;

    /** 生成 128 bit 随机会话 ID。 */
    public String newSessionId() {
        return this.randomBase64Url(SESSION_ID_BYTES);
    }

    /** 为指定 Session 生成 256 bit Refresh Token。 */
    public IssuedToken issue(String sessionId) {
        if (!SESSION_ID_PATTERN.matcher(sessionId).matches()) {
            throw new IllegalArgumentException("Refresh Session ID 格式无效");
        }
        String secret = this.randomBase64Url(SECRET_BYTES);
        return new IssuedToken(sessionId + "." + secret, sessionId,
            this.fingerprint(secret));
    }

    /** 严格解析 Refresh Token，并计算 secret 指纹。 */
    public ParsedToken parse(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > MAX_TOKEN_LENGTH) {
            throw this.invalidToken();
        }
        int separator = rawToken.indexOf('.');
        if (separator <= 0 || separator != rawToken.lastIndexOf('.')) {
            throw this.invalidToken();
        }
        String sessionId = rawToken.substring(0, separator);
        String secret = rawToken.substring(separator + 1);
        if (!SESSION_ID_PATTERN.matcher(sessionId).matches()
            || !SECRET_PATTERN.matcher(secret).matches()) {
            throw this.invalidToken();
        }
        return new ParsedToken(rawToken, sessionId, this.fingerprint(secret));
    }

    /** 对任意 bearer 凭证生成不可逆、带服务端密钥的稳定指纹。 */
    public String fingerprint(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new IllegalArgumentException("凭证不能为空");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(this.deriveKey("fingerprint"), "HmacSHA256"));
            byte[] digest = mac.doFinal(credential.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("无法计算 Refresh Token 指纹", e);
        }
    }

    /** 使用常量时间比较两个指纹。 */
    public boolean matches(String actualFingerprint, String expectedFingerprint) {
        if (actualFingerprint == null || expectedFingerprint == null) {
            return false;
        }
        return MessageDigest.isEqual(actualFingerprint.getBytes(StandardCharsets.US_ASCII),
            expectedFingerprint.getBytes(StandardCharsets.US_ASCII));
    }

    /** 使用 AES-256-GCM 加密短时轮换快照。 */
    public String encrypt(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("待加密凭证不能为空");
        }
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(this.deriveKey("encryption"), "AES"),
                new GCMParameterSpec(GCM_TAG_BITS, iv));
            cipher.updateAAD(ENCRYPTION_AAD);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("无法加密 Refresh Token 轮换快照", e);
        }
    }

    /** 解密并校验 AES-GCM 轮换快照。 */
    public String decrypt(String value) {
        if (value == null || value.isBlank()) {
            throw this.invalidToken();
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(value);
            if (payload.length <= GCM_IV_BYTES) {
                throw this.invalidToken();
            }
            byte[] iv = new byte[GCM_IV_BYTES];
            byte[] encrypted = new byte[payload.length - GCM_IV_BYTES];
            System.arraycopy(payload, 0, iv, 0, iv.length);
            System.arraycopy(payload, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(this.deriveKey("encryption"), "AES"),
                new GCMParameterSpec(GCM_TAG_BITS, iv));
            cipher.updateAAD(ENCRYPTION_AAD);
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw this.invalidToken();
        }
    }

    private byte[] deriveKey(String purpose) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(("continew-refresh-" + purpose + "\0")
                .getBytes(StandardCharsets.UTF_8));
            return digest.digest(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("无法派生 Refresh Token 密钥", e);
        }
    }

    private String randomBase64Url(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private RefreshTokenException invalidToken() {
        return RefreshTokenException.unauthorized("登录状态已失效，请重新登录");
    }

    /** 新签发的 Refresh Token。 */
    public record IssuedToken(String rawToken, String sessionId, String fingerprint) {
    }

    /** 已解析的 Refresh Token；不向调用方暴露 secret。 */
    public record ParsedToken(String rawToken, String sessionId, String fingerprint) {
    }
}
