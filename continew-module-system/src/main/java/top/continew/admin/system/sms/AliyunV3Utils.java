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

package top.continew.admin.system.sms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SimpleTimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AliyunV3Utils {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * 加密方式
     */
    private static final String ALGORITHM = "ACS3-HMAC-SHA256";

    public static String generateQueryString(AlibabaV3Config alibabaConfig,
                                             String message,
                                             String phone,
                                             String templateId) {
        Map<String, String> paramMap = generateParamMap(alibabaConfig, phone, message, templateId);
        Map<String, String> sortParas = new TreeMap<>(paramMap);

        StringBuilder canonicalQueryString = new StringBuilder();
        sortParas.entrySet()
            .stream()
            .map(entry -> specialUrlEncode(entry.getKey()) + "=" + specialUrlEncode(entry.getValue()))
            .forEach(queryPart -> {
                // 如果canonicalQueryString已经不是空的，则在查询参数前添加"&"
                if (canonicalQueryString.length() > 0) {
                    canonicalQueryString.append("&");
                }
                canonicalQueryString.append(queryPart);
            });
        return canonicalQueryString.toString();
    }

    public static String generateSendSmsRequestUrl(AlibabaV3Config alibabaConfig, String queryString) {

        return "https://" + alibabaConfig.getRequestUrl() + "/?" + queryString;
    }

    /**
     * url编码
     */
    private static String specialUrlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用SHA-256算法计算字符串的哈希值并以十六进制字符串形式返回。
     *
     * @param input 需要进行SHA-256哈希计算的字节数组。
     * @return 计算结果为小写十六进制字符串。
     * @throws Exception 如果在获取SHA-256消息摘要实例时发生错误。
     */
    public static String sha256Hex(byte[] input) throws Exception {
        // 获取SHA-256消息摘要实例
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // 计算字符串s的SHA-256哈希值
        byte[] d = md.digest(input);
        // 将哈希值转换为小写十六进制字符串并返回
        return DatatypeConverter.printHexBinary(d).toLowerCase();
    }

    /**
     * 生成请求body参数
     *
     * @param alibabaConfig 配置数据
     * @param phone         手机号
     * @param message       短信内容
     * @param templateId    模板id
     */
    public static Map<String, String> generateParamMap(AlibabaV3Config alibabaConfig,
                                                       String phone,
                                                       String message,
                                                       String templateId) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("PhoneNumbers", phone);
        paramMap.put("SignName", alibabaConfig.getSignature());
        paramMap.put("TemplateParam", message);
        paramMap.put("TemplateCode", templateId);
        return paramMap;
    }

    /**
     * 生成请求参数body字符串
     *
     * @param alibabaConfig 配置数据
     * @param phone         手机号
     * @param message       短信内容
     * @param templateId    模板id
     */
    public static String generateParamBody(AlibabaV3Config alibabaConfig,
                                           TreeMap<String, Object> body) throws Exception {
        if (Objects.isNull(body))
            return null;
        if (body.isEmpty())
            return null;
        return new ObjectMapper().writeValueAsString(body);
    }

    /**
     * 使用HmacSHA256算法生成消息认证码（MAC）。
     *
     * @param secretKey 密钥，用于生成MAC的密钥，必须保密。
     * @param str       需要进行MAC认证的消息。
     * @return 返回使用HmacSHA256算法计算出的消息认证码。
     * @throws Exception 如果初始化MAC或计算MAC过程中遇到错误，则抛出异常。
     */
    public static byte[] hmac256(byte[] secretKey, String str) throws Exception {
        // 实例化HmacSHA256消息认证码生成器
        Mac mac = Mac.getInstance("HmacSHA256");
        // 创建密钥规范，用于初始化MAC生成器
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, mac.getAlgorithm());
        // 初始化MAC生成器
        mac.init(secretKeySpec);
        // 计算消息认证码并返回
        return mac.doFinal(str.getBytes(StandardCharsets.UTF_8));
    }

    public static Map<String, String> generateHeaders(AlibabaV3Config alibabaV3Config,
                                                      String body,
                                                      String queryString) throws Exception {
        Map<String, String> headers = new TreeMap<>();
        headers.put("host", alibabaV3Config.getRequestUrl());
        headers.put("x-acs-action", alibabaV3Config.getAction());
        headers.put("x-acs-version", alibabaV3Config.getVersion());
        SDF.setTimeZone(new SimpleTimeZone(0, "GMT")); // 设置日期格式化时区为GMT
        headers.put("x-acs-date", SDF.format(new Date()));
        headers.put("x-acs-signature-nonce", UUID.randomUUID().toString());

        String requestPayload = ""; // 请求体，当请求正文为空时，比如GET请求，RequestPayload固定为空字符串
        String hashedRequestPayload = body != null
            ? sha256Hex(body.getBytes(StandardCharsets.UTF_8))
            : sha256Hex(requestPayload.getBytes(StandardCharsets.UTF_8));
        headers.put("x-acs-content-sha256", hashedRequestPayload);

        // 构造请求头，多个规范化消息头，按照消息头名称（小写）的字符代码顺序以升序排列后拼接在一起
        StringBuilder canonicalHeaders = new StringBuilder();
        // 已签名消息头列表，多个请求头名称（小写）按首字母升序排列并以英文分号（;）分隔
        StringBuilder signedHeadersSb = new StringBuilder();
        headers.entrySet()
            .stream()
            .filter(entry -> entry.getKey().toLowerCase().startsWith("x-acs-") || "host".equalsIgnoreCase(entry
                .getKey()) || "content-type".equalsIgnoreCase(entry.getKey()))
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String lowerKey = entry.getKey().toLowerCase();
                String value = String.valueOf(entry.getValue()).trim();
                canonicalHeaders.append(lowerKey).append(":").append(value).append("\n");
                signedHeadersSb.append(lowerKey).append(";");
            });
        String signedHeaders = signedHeadersSb.substring(0, signedHeadersSb.length() - 1);
        String canonicalRequest = "POST" + "\n" + "/" + "\n" + queryString + "\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;
        log.debug("canonicalRequest =========>\n{}", canonicalRequest);

        // 步骤 2：拼接待签名字符串
        String hashedCanonicalRequest = sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8)); // 计算规范化请求的哈希值
        String stringToSign = ALGORITHM + "\n" + hashedCanonicalRequest;
        log.debug("stringToSign =========>\n{}", stringToSign);

        // 步骤 3：计算签名
        String signature = DatatypeConverter.printHexBinary(hmac256(alibabaV3Config.getAccessKeySecret()
            .getBytes(StandardCharsets.UTF_8), stringToSign)).toLowerCase();
        log.debug("signature =========> {}", signature);

        // 步骤 4：拼接 Authorization
        String authorization = ALGORITHM + " " + "Credential=" + alibabaV3Config
            .getAccessKeyId() + ",SignedHeaders=" + signedHeaders + ",Signature=" + signature;
        log.debug("authorization =========> {}", authorization);

        headers.put("Authorization", authorization);

        return headers;

    }

}
