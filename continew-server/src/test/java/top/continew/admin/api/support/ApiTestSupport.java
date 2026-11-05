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

package top.continew.admin.api.support;

import cn.hutool.json.JSONUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Cipher;

/**
 * L3 API 黑盒测试基座：配置读取、登录、请求封装、响应断言与资源清理。
 *
 * <p>
 * 配置读取优先级：系统属性（-Dapi.*）&gt; 环境变量（API_*）&gt; 默认值。Java 测试只读配置，
 * 不执行 docker inspect、不写死宿主路径，环境解析由 scripts/test 下的脚本负责。
 * </p>
 * <p>
 * 响应契约（ContiNew Starter 全局响应）：业务成功为 HTTP 200 且 {@code code} 等于字符串
 * {@code "0"}（{@code R.code} 为 String 类型）；业务错误默认仍为 HTTP 200 但 {@code code}
 * 非 {@code "0"}（application.yml 的 default-http-status-code-on-error: 200）；未认证为
 * HTTP 401 且 {@code code} 为 {@code "401"}（GlobalSaTokenExceptionHandler）。
 * </p>
 * <p>
 * 登录密码按后端约定经 RSA 公钥加密：JCE Cipher 直连 RSA/ECB/PKCS1Padding，输出标准 Base64。
 * 后端解密链路（SecureUtils.decryptByRsaPrivateKey）对入参先做 Base64 解码，加密侧输出必须是
 * Base64 密文字符串（与前端 JSEncrypt 的输出一致）；不可复用 SecureUtils.encryptByRsaPublicKey，
 * 它把原始密文二进制直接转 UTF-8 字符串产生乱码，与解密侧的 Base64 前置约定不兼容。
 * 测试环境已由 up.sh 关闭验证码，登录请求不携带 captcha 与 uuid 字段。
 * </p>
 * <p>
 * 请求 Content-Type 约定：所有 POST 必须显式声明 {@code contentType(JSON)}，无业务字段的
 * POST 用 {@code body("{}")} 占位。rest-assured 对未声明 CT 的 POST 默认发送
 * application/x-www-form-urlencoded，服务端 @RequestBody(required=false) 端点对该 CT 静默
 * 返回 code=1（不走异常处理器、无日志无栈），171 实测教训；登出端点即因此失败。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
public abstract class ApiTestSupport {

    /**
     * application-dev.yml 中随仓库分发的开发环境示例 RSA 公钥（仓库内公开值，非敏感凭据）。
     * 仅适用于开发环境实例，其他环境通过 api.rsa-public-key 或 API_RSA_PUBLIC_KEY 覆盖。
     */
    private static final String DEFAULT_DEV_RSA_PUBLIC_KEY =
        "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAM51dgYtMyF"
            + "+tTQt80sfFOpSV27a7t9uaUVeFrdGiVxscuizE7H8SMntYqfn9lp8a5GH5P1/GGehVjUD2gF/4kcCAwEAAQ==";

    /**
     * 服务基础地址，默认指向 compose 测试环境的 18020 端口。
     */
    protected static final String BASE_URL = config("api.base-url", "API_BASE_URL",
        "http://127.0.0.1:18020");

    /**
     * 服务同源 Origin（BASE_URL 去除尾斜杠）：用于登出等携带 Refresh Token Cookie 的请求。
     * 服务端对带 Cookie 的请求强制 Origin 校验，same-origin 在自比对分支放行，不依赖白名单配置。
     */
    protected static final String BASE_ORIGIN = BASE_URL.replaceAll("/$", "");

    /**
     * 平台超管用户名，仅 dev 环境默认 admin。
     */
    protected static final String USERNAME = config("api.username", "API_USERNAME", "admin");

    /**
     * 平台超管密码，仅 dev 弱默认（计划裁定），非 dev 环境必须显式注入。
     */
    protected static final String PASSWORD = config("api.password", "API_PASSWORD", "admin123");

    /**
     * 默认 PC 客户端 ID，取 Liquibase 初始数据 sys_client 的 client_id。
     */
    protected static final String CLIENT_ID = config("api.client-id", "API_CLIENT_ID",
        "ef51c9a3e9046c4f2ea45142c8a8344a");

    /**
     * RSA 公钥：支持 file: 前缀读 PEM 文件覆盖，默认用仓库内 dev 示例公钥。
     */
    protected static final String RSA_PUBLIC_KEY = normalizePublicKey(config("api.rsa-public-key",
        "API_RSA_PUBLIC_KEY", DEFAULT_DEV_RSA_PUBLIC_KEY));

    /**
     * 默认租户 ID（平台侧）。
     */
    protected static final Long DEFAULT_TENANT_ID = 0L;

    /**
     * 业务成功响应码（全局响应 R.code 为字符串类型）。
     */
    private static final String SUCCESS_CODE = "0";

    /**
     * 未认证 HTTP 状态码。
     */
    protected static final int UNAUTHORIZED_STATUS = 401;

    /**
     * 当前登录用户的 Access Token。
     */
    private String accessToken;

    /**
     * 登录响应携带的 Cookie（COOKIE 模式的 Refresh Token 在其中），登出时需回传。
     */
    private Map<String, String> loginCookies;

    /**
     * 当前租户上下文，默认平台侧租户 0。
     */
    private Long tenantId = DEFAULT_TENANT_ID;

    /**
     * 按优先级读取配置：系统属性 &gt; 环境变量 &gt; 默认值。
     *
     * @param sysPropKey
     *            系统属性键
     * @param envVar
     *            环境变量名
     * @param defaultValue
     *            默认值
     * @return 生效配置值
     */
    protected static String config(String sysPropKey, String envVar, String defaultValue) {
        String value = System.getProperty(sysPropKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(envVar);
        }
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /**
     * 规范化 RSA 公钥：剥除 PEM 头尾标记与空白字符，仅保留 Base64 主体。
     *
     * @param configured
     *            配置的公钥（裸 Base64、PEM 文本或 file: 前缀路径）
     * @return 纯 Base64 公钥
     */
    private static String normalizePublicKey(String configured) {
        String value = configured.trim();
        if (value.startsWith("file:")) {
            Path pemPath = Path.of(value.substring("file:".length()));
            try {
                value = Files.readString(pemPath).trim();
            } catch (Exception e) {
                throw new IllegalStateException("读取 RSA 公钥 PEM 文件失败: " + pemPath, e);
            }
        }
        // 兼容 PEM 文本形态，转换为纯 Base64 后统一交给 JCE 解析
        return value.replaceAll("-----BEGIN [^-]+-----", "")
            .replaceAll("-----END [^-]+-----", "")
            .replaceAll("\\s", "");
    }

    /**
     * 用 RSA 公钥加密明文：JCE Cipher 直连 RSA/ECB/PKCS1Padding，输出标准 Base64。
     * 后端 SecureUtils.decryptByRsaPrivateKey 对入参先 Base64 解码再解密，
     * 加密侧输出必须是 Base64 密文字符串（与前端 JSEncrypt 输出一致）。
     *
     * @param raw
     *            明文
     * @return RSA 加密后的 Base64 密文
     */
    protected static String encryptByRsa(String raw) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(RSA_PUBLIC_KEY);
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder()
                .encodeToString(cipher.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("RSA 公钥加密失败，请检查 api.rsa-public-key 配置", e);
        }
    }

    /**
     * 等待最终一致断言成立：超时 5 秒、轮询间隔 200 毫秒，禁止裸 sleep。
     *
     * @param assertion
     *            反复执行的断言（抛出 AssertionError 即视为未就绪）
     */
    protected static void awaitConsistency(Runnable assertion) {
        Awaitility.await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(200))
            .untilAsserted(assertion::run);
    }

    /**
     * 构造不带认证信息的请求（用于登录、登出后的失效验证等匿名场景）。
     *
     * @return 请求规格
     */
    protected RequestSpecification given() {
        return RestAssured.given().baseUri(BASE_URL);
    }

    /**
     * 构造带当前 Access Token 与租户上下文的请求；未登录时先以默认超管登录。
     *
     * @return 请求规格
     */
    protected RequestSpecification givenAuth() {
        if (accessToken == null) {
            loginAsDefaultAdmin();
        }
        return given().header("Authorization", "Bearer " + accessToken)
            .header("X-Tenant-Id", String.valueOf(tenantId));
    }

    /**
     * 切换后续请求的租户上下文。
     *
     * @param newTenantId
     *            目标租户 ID
     */
    protected void useTenant(Long newTenantId) {
        this.tenantId = newTenantId;
    }

    /**
     * 以配置的默认超管凭据登录。
     *
     * @return Access Token
     */
    protected String loginAsDefaultAdmin() {
        return login(USERNAME, PASSWORD);
    }

    /**
     * 账号密码登录：POST /auth/login，认证类型固定 ACCOUNT，不携带验证码字段。
     * 成功后缓存 Access Token 与登录响应 Cookie，供后续请求与登出复用。
     *
     * @param username
     *            用户名
     * @param rawPassword
     *            明文密码（方法内完成 RSA 加密）
     * @return Access Token
     */
    protected String login(String username, String rawPassword) {
        return login(username, rawPassword, CLIENT_ID);
    }

    /**
     * 指定客户端的账号密码登录：多端并发会话用例（闲置策略归属、双令牌持有）需要以不同
     * clientId 签发各自的 Access Token，登录链路其余逻辑与两参重载完全一致。
     *
     * @param username
     *            用户名
     * @param rawPassword
     *            明文密码（方法内完成 RSA 加密）
     * @param clientId
     *            客户端 ID（sys_client.client_id）
     * @return Access Token
     */
    protected String login(String username, String rawPassword, String clientId) {
        Map<String, Object> body = Map.of("clientId", clientId, "authType", "ACCOUNT",
            "username", username, "password", encryptByRsa(rawPassword));
        Response response = given().header("X-Tenant-Id", String.valueOf(tenantId))
            .contentType(ContentType.JSON)
            .body(body)
            .post("/auth/login");
        if (response.statusCode() != 200 || !SUCCESS_CODE.equals(response.jsonPath()
            .getString("code"))) {
            Assertions.fail("登录失败: HTTP " + response.statusCode() + "，响应体：" + sanitize(response
                .getBody().asString()));
        }
        this.accessToken = response.jsonPath().getString("data.accessToken");
        this.loginCookies = response.cookies();
        return this.accessToken;
    }

    /**
     * 获取登录响应携带的 Cookie。COOKIE 模式客户端（如默认 PC 客户端）的 Refresh Token
     * 仅写入 HttpOnly Cookie，登出时必须回传，否则会话无法完整注销。
     *
     * @return 登录响应的 Cookie 集合
     */
    protected Map<String, String> getLoginCookies() {
        return this.loginCookies;
    }

    /**
     * 获取当前登录会话的 Access Token。供登出失效等用例显式引用原 token，
     * 避免经由 givenAuth 的自动登录掩盖 token 失效事实。
     *
     * @return 当前 Access Token
     */
    protected String getAccessToken() {
        return this.accessToken;
    }

    /**
     * 断言业务成功：HTTP 200 且业务码为 0；失败时输出脱敏后的响应体。
     *
     * @param response
     *            待断言响应
     */
    protected void assertBizOk(Response response) {
        if (response.statusCode() != 200) {
            failWithBody("业务响应 HTTP 状态码期望 200，实际 " + response.statusCode(), response);
        }
        String code = response.jsonPath().getString("code");
        if (!SUCCESS_CODE.equals(code)) {
            failWithBody("业务码期望 " + SUCCESS_CODE + "，实际 " + code, response);
        }
    }

    /**
     * 断言业务成功并提取响应中指定 JsonPath 的值。
     *
     * @param response
     *            待断言响应
     * @param path
     *            JsonPath 表达式（如 data.accessToken）
     * @param <T>
     *            期望类型
     * @return 提取值
     */
    protected <T> T assertBizOk(Response response, String path) {
        assertBizOk(response);
        return response.jsonPath().get(path);
    }

    /**
     * 发起 DELETE 清理请求并吞掉所有异常：清理失败只记 warning，不吞主断言。
     * 供 try/finally 逆序清理使用。
     *
     * @param path
     *            删除端点（如 /tenant/management/1）
     */
    protected void deleteQuietly(String path) {
        try {
            Response response = givenAuth().delete(path);
            if (response.statusCode() == 200 && SUCCESS_CODE.equals(response.jsonPath()
                .getString("code"))) {
                log.info("L3 清理成功: {}", path);
            } else {
                log.warn("L3 清理未成功: {}，HTTP {}，业务码 {}", path, response.statusCode(), response
                    .jsonPath().getString("code"));
            }
        } catch (Exception e) {
            log.warn("L3 清理请求异常: {}，原因：{}", path, e.getMessage());
        }
    }

    /**
     * 发起带 JSON 请求体的 DELETE 清理请求并吞掉所有异常（批量删除端点的形态：
     * DELETE {base}，请求体 IdsReq.ids）。清理失败只记 warning，不吞主断言。
     *
     * @param path 删除端点（如 /system/user）
     * @param body JSON 请求体（如 Map.of("ids", List.of(1L))）
     */
    protected void deleteQuietly(String path, Object body) {
        try {
            Response response = givenAuth().contentType(ContentType.JSON)
                .body(body)
                .delete(path);
            if (response.statusCode() == 200 && SUCCESS_CODE.equals(response.jsonPath()
                .getString("code"))) {
                log.info("L3 清理成功: {}", path);
            } else {
                log.warn("L3 清理未成功: {}，HTTP {}，业务码 {}", path, response.statusCode(), response
                    .jsonPath().getString("code"));
            }
        } catch (Exception e) {
            log.warn("L3 清理请求异常: {}，原因：{}", path, e.getMessage());
        }
    }

    /**
     * 对响应体做脱敏：替换 accessToken、refreshToken 与 password 字段值为 ***。
     * 用于失败信息输出，避免 token 与密码进入测试日志。
     *
     * @param body
     *            原始响应体
     * @return 脱敏后的文本
     */
    protected static String sanitize(String body) {
        if (body == null) {
            return "";
        }
        return body.replaceAll("(\"(?:accessToken|refreshToken|password)\"\\s*:\\s*\")[^\"]*",
            "$1***");
    }

    /**
     * 生成规范化 JSON 文本：对象键按字典序排序，数组元素按其规范化文本排序。
     * 数组排序是为了屏蔽列表接口无稳定排序造成的幂等性误报，比较关注的是数据内容一致性。
     *
     * @param payload
     *            任意可 JSON 化的对象（通常为 JsonPath 提取的 Map/List）
     * @return 键序稳定的 JSON 文本
     */
    protected static String canonicalJson(Object payload) {
        return JSONUtil.toJsonStr(canonicalize(payload));
    }

    /**
     * 递归规范化：对象转 TreeMap（字典序），数组排序元素。
     *
     * @param value
     *            原始值
     * @return 规范化后的值
     */
    private static Object canonicalize(Object value) {
        if (value instanceof Map<?, ?> map) {
            TreeMap<String, Object> sorted = new TreeMap<>();
            map.forEach((key, child) -> sorted.put(String.valueOf(key), canonicalize(child)));
            return sorted;
        }
        if (value instanceof List<?> list) {
            List<Object> items = new ArrayList<>(list.size());
            for (Object item : list) {
                items.add(canonicalize(item));
            }
            items.sort(Comparator.comparing(JSONUtil::toJsonStr));
            return items;
        }
        return value;
    }

    /**
     * 断言失败并输出脱敏后的响应体。
     *
     * @param message
     *            失败描述
     * @param response
     *            待输出响应
     */
    private void failWithBody(String message, Response response) {
        Assertions.fail(message + "，响应体：" + sanitize(response.getBody().asString()));
    }
}
