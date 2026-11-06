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

import java.util.List;
import java.util.Map;

/**
 * 错误码契约回归测试：参数校验、唯一冲突与资源不存在三类响应结构的黑盒断言。
 *
 * <p>
 * 契约基线（与 {@link ApiTestSupport} 类注释一致）：业务失败默认 HTTP 200（application.yml 的
 * default-http-status-code-on-error: 200），错误语义由业务码 code（字符串）与 msg 表达。
 * 本套件聚焦错误码与响应结构，不覆盖权限拒绝（PermissionMatrixApiTest 已覆盖）。
 * </p>
 * <p>
 * 全部用例不会产生落库数据：参数校验失败与唯一冲突均在业务执行前抛出（事务回滚），
 * 其余用例为只读请求，故无需资源清理。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Tag("api")
class ErrorCodeContractApiTest extends ApiTestSupport {

    /**
     * 业务成功响应码。
     */
    private static final String BIZ_OK_CODE = "0";

    /**
     * 客户端错误响应码（参数校验与业务校验类异常）。
     */
    private static final String BAD_REQUEST_CODE = "400";

    /**
     * 业务异常响应码（CheckUtils/ValidationUtils 校验失败等 BusinessException）。
     */
    private static final String BUSINESS_ERROR_CODE = "500";

    /**
     * 全局兜底响应码（未被异常处理器定向接管的异常）。
     */
    private static final String DEFAULT_ERROR_CODE = "1";

    /**
     * 全局兜底失败提示（application.yml default-error-msg）。
     */
    private static final String DEFAULT_ERROR_MSG = "服务器异常，请联系管理员";

    /**
     * 全局成功提示（application.yml default-success-msg）。
     */
    private static final String DEFAULT_SUCCESS_MSG = "操作成功";

    /**
     * 参数校验：POST /system/user 缺失 username，业务码 400 且 msg 为校验注解消息。
     *
     * <p>
     * 依据：AbstractCrudController#create 声明 @Validated(Create) + @Valid（请求体校验失败抛
     * MethodArgumentNotValidException），AdminGlobalExceptionHandler 将其映射为
     * R.fail("400", 第一条字段错误消息)；UserReq#username 为 @NotBlank(message = "用户名不能为空")，
     * username 缺失时 @Pattern 对 null 不生效，错误唯一，msg 可精确断言。
     * </p>
     */
    @Test
    void createUserWithoutUsernameReturnsValidationContract() {
        Map<String, Object> body = Map.of("nickname", "qa_contract_probe", "password",
            "qa-placeholder-pwd", "gender", 1, "deptId", 1, "roleIds", List.of(2));
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post("/system/user");
        assertErrorResponseContract(response, 200, BAD_REQUEST_CODE, "用户名不能为空");
    }

    /**
     * 参数校验：POST /system/role 缺失 code，业务码 400 且 msg 为校验注解消息。
     *
     * <p>
     * 依据：RoleReq#code 为 @NotBlank(message = "编码不能为空")（RoleReq.java:58），
     * 异常映射链与用户用例一致；name 提供合法值，保证仅 code 一条校验错误。
     * </p>
     */
    @Test
    void createRoleWithoutCodeReturnsValidationContract() {
        Map<String, Object> body = Map.of("name", "qa_contract_probe");
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post("/system/role");
        assertErrorResponseContract(response, 200, BAD_REQUEST_CODE, "编码不能为空");
    }

    /**
     * 唯一冲突：POST /system/user 使用内置超管用户名 admin，业务码 500 且 msg 为重复校验文案。
     *
     * <p>
     * 依据：UserServiceImpl#beforeCreate 先解密密码（须传合法 RSA 密文）再执行
     * checkUsernameRepeat（UserServiceImpl.java:765-770，CheckUtils.throwIf 抛 BusinessException，
     * CheckUtils.java:35），AdminGlobalExceptionHandler 将 BusinessException 映射为
     * R.fail("500", e.getMessage())；消息模板「用户名为 [{}] 的用户已存在」可精确断言。
     * </p>
     */
    @Test
    void createDuplicateUsernameReturnsConflictContract() {
        Map<String, Object> body = Map.of("username", "admin", "nickname", "qa_contract_dup_probe",
            "password", encryptByRsa("QaContract#2025"), "gender", 1, "deptId", 1, "roleIds", List
                .of(2));
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post("/system/user");
        assertErrorResponseContract(response, 200, BUSINESS_ERROR_CODE, "用户名为 [admin] 的用户已存在");
    }

    /**
     * 唯一冲突：POST /system/role 使用内置角色编码 super_admin，业务码 500 且 msg 为重复校验文案。
     *
     * <p>
     * 依据：RoleServiceImpl#create 先执行 checkNameRepeat、checkCodeRepeat（RoleServiceImpl.java:81-84），
     * 编码存在性检查先于「编码禁止使用」检查，因此 msg 精确为「编码为 [super_admin] 的角色已存在」
     * （RoleServiceImpl.java:275-281）；super_admin 为 Liquibase 初始超管角色编码（main_data.sql:171）。
     * </p>
     */
    @Test
    void createDuplicateRoleCodeReturnsConflictContract() {
        Map<String, Object> body = Map
            .of("name", "qa_contract_dup_role", "code", "super_admin");
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post("/system/role");
        assertErrorResponseContract(response, 200, BUSINESS_ERROR_CODE, "编码为 [super_admin] 的角色已存在");
    }

    /**
     * 资源不存在：GET /system/user/{不存在的 id}，返回成功响应且 data 为 null。
     *
     * <p>
     * 依据：AbstractCrudController#get 直接返回 baseService.get(id)；CrudServiceImpl#get 以
     * getById(id, false) 关闭存在性检查（ServiceImpl.java:263-270），实体为 null 时
     * BeanUtil.toBean(null, ...) 返回 null、fill 为空操作，控制器返回 null 后由
     * GlobalResponseBodyAdvice 包装为 R.ok()（DefaultGlobalResponseFactory#newSuccess 对 null
     * 返回 R.ok()），即 HTTP 200 + code 0 + data null 的「成功空数据」契约，而非错误响应。
     * </p>
     */
    @Test
    void getNonExistentUserDetailReturnsEmptySuccessContract() {
        Response response = givenAuth().get("/system/user/999999999");
        assertThat(response.statusCode()).as("查询不存在详情的 HTTP 状态码应为 200")
            .isEqualTo(200);
        assertThat(response.jsonPath().getString("code")).as("查询不存在详情的业务码应为 0")
            .isEqualTo(BIZ_OK_CODE);
        assertThat(response.jsonPath().getString("msg")).as("查询不存在详情的 msg 应为全局成功提示")
            .isEqualTo(DEFAULT_SUCCESS_MSG);
        // 显式声明 Object 接收，避免泛型推断在 assertThat 重载间产生二义
        Object data = response.jsonPath().get("data");
        assertThat(data).as("查询不存在详情的 data 应为空")
            .isNull();
    }

    /**
     * 资源不存在：GET 不存在的 URL，业务码 1 且 msg 为全局兜底失败提示。
     *
     * <p>
     * 依据：默认静态资源映射（Spring Boot 4.1.1 WebMvcAutoConfigurationAdapter#addResourceHandlers，
     * addMappings 默认开启）使未匹配控制器的请求落入资源处理器并抛 NoResourceFoundException
     * （Spring 7.0.10 该异常无 @ResponseStatus 注解）；AdminGlobalExceptionHandler 未声明该类型，
     * 由 Starter 全局兜底处理器接管，GlobalExceptionResolver#resolveHttpStatus 无注解与别名时取
     * default-http-status-code-on-error（200），响应码与提示取 default-error-code（1）与
     * default-error-msg（服务器异常，请联系管理员）。
     * </p>
     */
    @Test
    void requestNonExistentUrlReturnsDefaultErrorContract() {
        Response response = givenAuth().get("/system/nonexistent-qa-probe");
        assertErrorResponseContract(response, 200, DEFAULT_ERROR_CODE, DEFAULT_ERROR_MSG);
    }

    /**
     * 断言错误响应契约三要素：HTTP 状态码、业务码与 msg 文案，并校验 data 为空。
     *
     * <p>
     * R.fail(code, msg) 不设置 data，失败响应的 data 始终为 null（R.java fail 工厂方法）。
     * </p>
     *
     * @param response
     *            待断言响应
     * @param expectedStatus
     *            期望 HTTP 状态码
     * @param expectedCode
     *            期望业务码
     * @param expectedMsg
     *            期望 msg 文案
     */
    private void assertErrorResponseContract(Response response, int expectedStatus,
        String expectedCode, String expectedMsg) {
        assertThat(response.statusCode()).as("HTTP 状态码期望 %d", expectedStatus)
            .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getString("code")).as("业务码期望 %s", expectedCode)
            .isEqualTo(expectedCode);
        assertThat(response.jsonPath().getString("msg")).as("msg 期望非空且精确为「%s」", expectedMsg)
            .isNotBlank()
            .isEqualTo(expectedMsg);
        // 显式声明 Object 接收，避免泛型推断在 assertThat 重载间产生二义
        Object data = response.jsonPath().get("data");
        assertThat(data).as("失败响应的 data 应为空")
            .isNull();
    }
}
