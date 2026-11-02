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

package top.continew.admin.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import top.continew.admin.api.support.ApiTestSupport;

import java.util.Map;

/**
 * 认证 API 黑盒测试：登录、用户信息幂等、错误凭据、未认证拦截与登出失效。
 *
 * <p>
 * 端点依据：POST /auth/login 与 GET /auth/user/info（AuthController），POST /auth/logout
 * （SessionController）。错误密码由业务侧抛出业务异常，HTTP 仍为 200 但业务码非 0；
 * 无 token 访问受保护端点由 GlobalSaTokenExceptionHandler 处理为 HTTP 401。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Tag("api")
class AuthApiTest extends ApiTestSupport {

    /**
     * 用户信息端点。
     */
    private static final String USER_INFO_PATH = "/auth/user/info";

    /**
     * 超管登录成功，返回非空 Access Token。
     */
    @Test
    void loginAsDefaultAdminSuccessReturnsToken() {
        String token = loginAsDefaultAdmin();
        assertThat(token).as("登录成功应返回非空 accessToken").isNotBlank();
    }

    /**
     * 用户信息接口连续两次读取的 data 规范化 JSON 一致，验证接口幂等与读取路径稳定。
     */
    @Test
    void userInfoReadTwiceCanonicalJsonStable() {
        loginAsDefaultAdmin();
        Response first = givenAuth().get(USER_INFO_PATH);
        assertBizOk(first);
        Response second = givenAuth().get(USER_INFO_PATH);
        assertBizOk(second);
        assertThat(canonicalJson(first.jsonPath().get("data")))
            .as("连续两次读取 /auth/user/info 的 data 应完全一致")
            .isEqualTo(canonicalJson(second.jsonPath().get("data")));
    }

    /**
     * 错误密码返回业务错误（业务码非 0），响应中不携带 accessToken。
     */
    @Test
    void loginWithWrongPasswordBizErrorWithoutToken() {
        Map<String, Object> body = Map.of("clientId", CLIENT_ID, "authType", "ACCOUNT",
            "username", USERNAME, "password", encryptByRsa("wrong-" + System.nanoTime()));
        Response response = given().contentType(ContentType.JSON)
            .body(body)
            .post("/auth/login");
        assertThat(response.statusCode()).as("错误密码默认仍返回 HTTP 200").isEqualTo(200);
        assertThat(response.jsonPath().getString("code"))
            .as("错误密码业务码不应为 0")
            .isNotEqualTo("0");
        Object accessToken = response.jsonPath().get("data.accessToken");
        assertThat(accessToken).as("错误密码不应返回 accessToken").isNull();
    }

    /**
     * 无 token 访问 /auth/user/info 返回未认证（HTTP 401）。
     */
    @Test
    void userInfoWithoutTokenUnauthorized() {
        Response response = given().get(USER_INFO_PATH);
        assertThat(response.statusCode()).as("无 token 访问受保护端点应返回 401")
            .isEqualTo(UNAUTHORIZED_STATUS);
    }

    /**
     * 登出后原 token 失效。默认 PC 客户端为 COOKIE 模式，Refresh Token 在登录响应的
     * HttpOnly Cookie 中，登出时按浏览器行为原样回传。
     */
    @Test
    void logoutThenOriginalTokenInvalid() {
        loginAsDefaultAdmin();
        Map<String, String> cookies = getLoginCookies();
        Response logoutResponse = givenAuth().cookies(cookies).post("/auth/logout");
        assertBizOk(logoutResponse);
        // 登出后带原 token 再访问，应被判定为未认证
        Response response = given().header("Authorization", "Bearer " + getAccessToken())
            .get(USER_INFO_PATH);
        assertThat(response.statusCode()).as("登出后原 token 应失效并返回 401")
            .isEqualTo(UNAUTHORIZED_STATUS);
    }
}
