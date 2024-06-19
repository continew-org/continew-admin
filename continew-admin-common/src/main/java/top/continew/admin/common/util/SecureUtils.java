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

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.extra.spring.SpringUtil;
import top.continew.admin.common.config.properties.RsaProperties;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.core.util.validate.ValidationUtils;
import top.continew.starter.security.crypto.autoconfigure.CryptoProperties;
import top.continew.starter.security.crypto.encryptor.AesEncryptor;
import top.continew.starter.security.crypto.encryptor.IEncryptor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 加密/解密工具类
 *
 * @author Charles7c
 * @since 2022/12/21 21:41
 */
public class SecureUtils {

    private SecureUtils() {
    }

    /**
     * 私钥解密
     *
     * @param data 要解密的内容（Base64 加密过）
     * @return 解密后的内容
     */
    public static String decryptByRsaPrivateKey(String data) {
        String privateKey = RsaProperties.PRIVATE_KEY;
        ValidationUtils.throwIfBlank(privateKey, "请配置 RSA 私钥");
        return decryptByRsaPrivateKey(data, privateKey);
    }

    /**
     * 私钥解密
     *
     * @param data       要解密的内容（Base64 加密过）
     * @param privateKey 私钥
     * @return 解密后的内容
     */
    public static String decryptByRsaPrivateKey(String data, String privateKey) {
        return new String(SecureUtil.rsa(privateKey, null).decrypt(Base64.decode(data), KeyType.PrivateKey));
    }

    /**
     * 对普通加密字段列表进行AES加密，优化starter加密模块后优化这个方法
     *
     * @param values 待加密内容
     * @return 加密后内容
     */
    public static List<String> encryptFieldByAes(List<String> values) {
        IEncryptor encryptor = new AesEncryptor();
        CryptoProperties properties = SpringUtil.getBean(CryptoProperties.class);
        return values.stream().map(value -> {
            try {
                return encryptor.encrypt(value, properties.getPassword(), properties.getPublicKey());
            } catch (Exception e) {
                throw new BusinessException("字段加密异常");
            }
        }).collect(Collectors.toList());
    }
}
