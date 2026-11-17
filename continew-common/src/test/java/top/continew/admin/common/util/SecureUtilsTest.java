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

package top.continew.admin.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.config.RsaProperties;
import top.continew.starter.core.exception.BadRequestException;

/**
 * RSA 加解密工具测试。
 *
 * <p>覆盖公钥加密、私钥解密的往返一致性、无密钥配置时的启动失败提示，以及密码解密的
 * 空值报错与格式校验分支。密钥经 SpringUtil 桩供给真实生成的 1024 位 RSA 密钥对，
 * 不依赖 Spring 上下文与配置文件。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class SecureUtilsTest {

    private static final String PLAIN_TEXT = "nightly-secret-3mchx";

    private static String privateKey;

    private static String publicKey;

    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
        if (privateKey == null) {
            java.security.KeyPair keyPair = cn.hutool.crypto.KeyUtil.generateKeyPair("RSA", 1024);
            privateKey = java.util.Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());
            publicKey = java.util.Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());
        }
        springUtilMock
            .when(() -> SpringUtil.getProperty("continew-starter.encrypt.field.private-key"))
            .thenReturn(privateKey);
        springUtilMock
            .when(() -> SpringUtil.getProperty("continew-starter.encrypt.field.public-key"))
            .thenReturn(publicKey);
        // 触发静态块按桩值初始化
        RsaProperties.PUBLIC_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldRoundTripDecryptAfterBase64() {
        // RSA.encrypt 输出是原始密文字节，前端实际传输的是 Base64 后的文本；
        // 解密入口按 Base64 解码，往返以「加密字节 → Base64 文本 → 解密」还原
        byte[] encryptedBytes = SecureUtil.rsa(null, RsaProperties.PUBLIC_KEY)
            .encrypt(PLAIN_TEXT, KeyType.PublicKey);
        String encryptedText = java.util.Base64.getEncoder().encodeToString(encryptedBytes);

        String decrypted = SecureUtils.decryptByRsaPrivateKey(encryptedText);

        assertEquals(PLAIN_TEXT, decrypted);
    }

    @Test
    void shouldRoundTripWithExplicitKeys() {
        byte[] encryptedBytes = SecureUtil.rsa(null, publicKey)
            .encrypt(PLAIN_TEXT, KeyType.PublicKey);
        String encryptedText = java.util.Base64.getEncoder().encodeToString(encryptedBytes);
        String decrypted = SecureUtils.decryptByRsaPrivateKey(encryptedText, privateKey);

        assertEquals(PLAIN_TEXT, decrypted);
    }

    @Test
    void shouldThrowWhenPublicKeyNotConfigured() {
        // 无效公钥在 RSA 初始化或加密时抛出
        assertThrows(Exception.class, () -> SecureUtils.encryptByRsaPublicKey(PLAIN_TEXT, ""));
    }

    @Test
    void shouldThrowWhenPasswordDecryptsBlank() {
        // 密文无法解密出有效内容时按错误信息抛出
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> SecureUtils.decryptPasswordByRsaPrivateKey("not-a-valid-rsa-cipher", "密码解密失败"));
        assertEquals("密码解密失败", exception.getMessage());
    }

    @Test
    void shouldDecryptPasswordWithoutPatternCheck() {
        String decrypted =
            SecureUtils.decryptPasswordByRsaPrivateKey(encryptToBase64Text(PLAIN_TEXT),
                "解密失败", false);

        assertEquals(PLAIN_TEXT, decrypted);
    }

    @Test
    void shouldRejectPasswordNotMatchingPatternWhenVerifyEnabled() {
        // 纯小写字母无数字，PASSWORD 正则要求至少包含字母和数字
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> SecureUtils.decryptPasswordByRsaPrivateKey(encryptToBase64Text("abcdefghij"),
                "解密失败",
                true));
        assertEquals("密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字", exception
            .getMessage());
    }

    /**
     * 公钥加密并将密文编码为 Base64 文本
     *
     * @param plainText 明文
     * @return Base64 编码的密文文本
     */
    private static String encryptToBase64Text(String plainText) {
        byte[] encryptedBytes = SecureUtil.rsa(null, RsaProperties.PUBLIC_KEY)
            .encrypt(plainText, KeyType.PublicKey);
        return java.util.Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
