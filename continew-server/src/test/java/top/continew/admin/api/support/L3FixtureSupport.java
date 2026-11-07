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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * L3 上下文越权回归夹具层：在 {@link ApiTestSupport} 之上补充多令牌显式请求、
 * 角色/用户/客户端/参数/租户等自建资源与原语，供上下文残留、在线会话同步、租户边界、
 * 闲置超时、密码过期、多端会话等专项用例共享，避免各测试类重复实现。
 *
 * <p>
 * 与基座的分工：基座负责配置读取、登录、通用断言与脱敏；本类只增不改，除内部私有
 * 工具外均为受保护辅助，不放宽基座既有断言语义。多令牌用例显式持有各端 Access Token，经
 * {@link #asToken(String, Object)} 构造请求，避免基座单例登录态互相覆盖。
 * </p>
 * <p>
 * 端点契约来源：客户端管理 {@code ClientController}（@CrudRequestMapping，批量删除为
 * DELETE {base} + IdsReq.ids）；用户分配角色为 {@code UserController#updateRole}
 * （PATCH /system/user/{id}/role，UserRoleUpdateReq.roleIds 标注 @NotEmpty，空集合会被
 * 参数校验拒绝）；参数管理 {@code OptionController}（PUT 请求体为 List&lt;OptionReq&gt;，
 * id/code/value 必传）；闲置阈值字段 {@code sys_client.refresh_token_idle_timeout} 经
 * ClientServiceImpl#checkIdleTimeout 强制「0 或不小于 60 秒」，测试客户端按最小合法值 60 秒。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
public abstract class L3FixtureSupport extends ApiTestSupport {

    /**
     * 角色管理 CRUD 端点。
     */
    protected static final String ROLE_PATH = "/system/role";

    /**
     * 用户管理 CRUD 端点（分页映射根路径，LIST 映射 /list 后缀）。
     */
    protected static final String USER_PATH = "/system/user";

    /**
     * 系统日志端点（受 monitor:log:list 权限保护）。
     */
    protected static final String LOG_PATH = "/system/log";

    /**
     * 客户端管理 CRUD 端点。
     */
    protected static final String CLIENT_PATH = "/system/client";

    /**
     * 参数管理端点。
     */
    protected static final String OPTION_PATH = "/system/option";

    /**
     * 当前登录用户信息端点（自助通道豁免路径）。
     */
    protected static final String USER_INFO_PATH = "/auth/user/info";

    /**
     * 当前登录用户路由端点（自助通道豁免路径）。
     */
    protected static final String USER_ROUTE_PATH = "/auth/user/route";

    /**
     * 在线用户端点（受 monitor:online:list 权限保护）。
     */
    protected static final String ONLINE_PATH = "/monitor/online";

    /**
     * 租户管理 CRUD 端点。
     */
    protected static final String TENANT_PATH = "/tenant/management";

    /**
     * 套餐管理 CRUD 端点。
     */
    protected static final String PACKAGE_PATH = "/tenant/package";

    /**
     * 套餐菜单树端点。
     */
    protected static final String PACKAGE_MENU_TREE_PATH = "/tenant/package/menu/tree";

    /**
     * monitor:log:list 权限按钮的菜单 ID（sys_menu 初始数据）。
     */
    protected static final Long MENU_ID_MONITOR_LOG_LIST = 2031L;

    /**
     * monitor:log:list 所属系统日志目录的菜单 ID（sys_menu 初始数据）。
     */
    protected static final Long MENU_ID_MONITOR_LOG_DIR = 2030L;

    /**
     * monitor:log:list 权限码（用于 user/info 权限快照断言）。
     */
    protected static final String PERM_MONITOR_LOG_LIST = "monitor:log:list";

    /**
     * 闲置拒绝响应消息（SaExtensionInterceptor 闲置短路分支原文）。
     */
    protected static final String IDLE_REJECT_MSG = "长时间未操作，请重新登录";

    /**
     * 租户越权拒绝响应消息（SaExtensionInterceptor 租户比对分支原文）。
     */
    protected static final String TENANT_FORBIDDEN_MSG = "您当前没有访问该租户的权限";

    /**
     * 密码过期业务码（PasswordExpiredException.CODE，HTTP 仍为 200）。
     */
    protected static final String PASSWORD_EXPIRED_CODE = "428";

    /**
     * 密码过期响应消息（PasswordExpiredException 构造入参原文）。
     */
    protected static final String PASSWORD_EXPIRED_MSG = "密码已过期，请先修改密码";

    /**
     * 测试 MySQL 容器名（compose project continew-admin-test 默认命名，可经
     * -Dapi.mysql-container / API_MYSQL_CONTAINER 覆盖）。
     */
    private static final String MYSQL_CONTAINER =
        config("api.mysql-container", "API_MYSQL_CONTAINER",
            "continew-admin-test-mysql-test-1");

    /**
     * 测试客户端闲置阈值最小合法值（秒）：ClientServiceImpl#checkIdleTimeout 拒绝 1-59 秒。
     */
    protected static final long IDLE_TIMEOUT_SECONDS = 60L;

    /**
     * 闲置等待缓冲（秒）：覆盖 ClientPolicyCache 快照 60 秒 TTL 的重载窗口。
     */
    protected static final long IDLE_WAIT_SECONDS = 65L;

    /**
     * 以显式 Access Token 构造请求（默认租户 0），登录态不受基座单例字段影响。
     *
     * @param token Access Token
     * @return 请求规格
     */
    protected RequestSpecification asToken(String token) {
        return asToken(token, DEFAULT_TENANT_ID);
    }

    /**
     * 以显式 Access Token 与显式租户头构造请求（单请求覆盖租户头，用于越权与拒绝面）。
     *
     * @param token
     *            Access Token
     * @param tenantId
     *            X-Tenant-Id 头取值
     * @return 请求规格
     */
    protected RequestSpecification asToken(String token, Object tenantId) {
        return given().header("Authorization", "Bearer " + token)
            .header("X-Tenant-Id", String.valueOf(tenantId));
    }

    /**
     * 以显式令牌读取当前用户信息（默认租户头）。
     *
     * @param token Access Token
     * @return 响应
     */
    protected Response userInfo(String token) {
        return asToken(token).get(USER_INFO_PATH);
    }

    /**
     * 以显式令牌与显式租户头读取当前用户信息。
     *
     * @param token
     *            Access Token
     * @param tenantId
     *            X-Tenant-Id 头取值
     * @return 响应
     */
    protected Response userInfo(String token, Object tenantId) {
        return asToken(token, tenantId).get(USER_INFO_PATH);
    }

    /**
     * 断言 user/info 响应归属指定属主：HTTP 200、业务码 0、id 与 username 匹配。
     *
     * @param response
     *            user/info 响应
     * @param expectedId
     *            属主用户 ID
     * @param expectedUsername
     *            属主用户名
     * @param stage
     *            断言阶段描述（失败定位用）
     */
    protected void assertOwner(Response response, Long expectedId, String expectedUsername,
        String stage) {
        Assertions.assertEquals(200, response.statusCode(),
            stage + "：期望 HTTP 200，实际 " + response.statusCode() + "，响应体：" + sanitize(response
                .getBody().asString()));
        String code = response.jsonPath().getString("code");
        Assertions.assertEquals("0", code,
            stage + "：期望业务码 0，实际 " + code + "，响应体：" + sanitize(response
                .getBody().asString()));
        Long actualId = response.jsonPath().getLong("data.id");
        Assertions.assertEquals(expectedId, actualId, stage + "：data.id 与令牌属主不符（串读判定点）");
        String actualUsername = response.jsonPath().getString("data.username");
        Assertions.assertEquals(expectedUsername, actualUsername, stage + "：data.username 与令牌属主不符");
    }

    /**
     * 提取 user/info 响应中的权限码集合。
     *
     * @param response
     *            user/info 响应
     * @return 权限码列表（无权限时为空列表）
     */
    protected List<String> permissionsOf(Response response) {
        List<String> permissions = response.jsonPath().getList("data.permissions", String.class);
        return permissions == null ? List.of() : permissions;
    }

    /**
     * 创建角色（POST /system/role，name/code/sort 必填形态与 PermissionMatrixApiTest 一致）。
     *
     * @param name
     *            角色名称
     * @param code
     *            角色编码
     * @return 角色 ID
     */
    protected Long createRole(String name, String code) {
        Map<String, Object> body = Map.of("name", name, "code", code, "sort", 1);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(ROLE_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 创建用户并绑定角色集合（密码经 RSA 公钥加密传输）。
     *
     * @param username
     *            用户名（^[a-zA-Z][a-zA-Z0-9_]{3,63}$）
     * @param nickname
     *            昵称
     * @param rawPassword
     *            明文密码（8-32 位，含小写字母与数字）
     * @param roleIds
     *            绑定角色 ID 列表
     * @return 用户 ID
     */
    protected Long createUser(String username, String nickname, String rawPassword,
        List<Long> roleIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("nickname", nickname);
        body.put("password", encryptByRsa(rawPassword));
        body.put("gender", 1);
        body.put("deptId", 1L);
        body.put("roleIds", roleIds);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(USER_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 修改角色功能权限（PUT /system/role/{id}/permission，空集合即清空）。
     *
     * @param roleId
     *            角色 ID
     * @param menuIds
     *            菜单 ID 集合
     */
    protected void updateRolePermission(Long roleId, List<Long> menuIds) {
        Map<String, Object> body = Map.of("menuIds", menuIds);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .put(ROLE_PATH + "/" + roleId + "/permission");
        assertBizOk(response);
    }

    /**
     * 为用户分配角色（PATCH /system/user/{id}/role）。UserController#updateRole 为
     * PatchMapping，且 UserRoleUpdateReq.roleIds 标注 @NotEmpty——撤销不能靠空集合，
     * 用例侧改以「切换到另一空权限角色」实现撤销语义。
     *
     * @param userId
     *            用户 ID
     * @param roleIds
     *            目标角色 ID 列表（非空）
     */
    protected void assignUserRoles(Long userId, List<Long> roleIds) {
        Map<String, Object> body = Map.of("roleIds", roleIds);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .patch(USER_PATH + "/" + userId + "/role");
        assertBizOk(response);
    }

    /**
     * 创建测试客户端（APP 型、ACCOUNT 认证、BODY 传输 Refresh Token、允许并发登录）。
     *
     * @param name
     *            客户端备注名（记录在 clientType 之外的可辨识字段缺省，用 code 语义由时间戳保证）
     * @param idleTimeoutSeconds
     *            Refresh Token 闲置失效秒数（必须为 0 或不小于 60，见 checkIdleTimeout）
     * @return 客户端行 ID（data.id，非 client_id）
     */
    protected Long createClient(String name, long idleTimeoutSeconds) {
        Map<String, Object> body = new HashMap<>();
        body.put("clientType", "APP");
        body.put("authType", List.of("ACCOUNT"));
        body.put("activeTimeout", 1800L);
        body.put("timeout", 86400L);
        body.put("refreshTokenTimeout", 2592000L);
        body.put("refreshTokenMode", "BODY");
        body.put("refreshTokenIdleTimeout", idleTimeoutSeconds);
        body.put("isConcurrent", true);
        body.put("maxLoginCount", -1);
        body.put("status", 1);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(CLIENT_PATH);
        assertBizOk(response);
        log.info("L3 自建客户端 {}: rowId={}", name, response.jsonPath().getLong("data.id"));
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 回读客户端的 client_id（登录请求携带的是 client_id 业务标识而非行 ID）。
     *
     * @param rowId
     *            客户端行 ID
     * @return client_id
     */
    protected String readClientId(Long rowId) {
        return assertBizOk(givenAuth().get(CLIENT_PATH + "/" + rowId), "data.clientId");
    }

    /**
     * 查询参数配置项（GET /system/option?code=xxx，管理端直查库，取首元素）。
     *
     * @param code
     *            参数编码
     * @return 配置项 Map（id/code/value 等）
     */
    protected Map<?, ?> findOption(String code) {
        Response response = givenAuth().queryParam("code", code).get(OPTION_PATH);
        assertBizOk(response);
        List<Map<?, ?>> options = response.jsonPath().getList("data");
        Assertions.assertFalse(options.isEmpty(), "参数配置项应存在: " + code);
        return options.get(0);
    }

    /**
     * 修改参数（PUT /system/option，单元素数组体，更新侧自动清理 OPTION: 缓存）。
     *
     * @param id
     *            配置项 ID
     * @param code
     *            参数编码
     * @param value
     *            新值
     */
    protected void updateOption(Object id, String code, String value) {
        Map<String, Object> option = Map.of("id", id, "code", code, "value", value);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(List.of(option))
            .put(OPTION_PATH);
        assertBizOk(response);
    }

    /**
     * 收集套餐菜单树前 30 个 id（深度优先，与 TenantApiTest 同源契约：data 节点键为 key）。
     *
     * @return 菜单 ID 列表
     */
    protected List<Long> collectFirstThirtyMenuIds() {
        Response response = givenAuth().get(PACKAGE_MENU_TREE_PATH);
        assertBizOk(response);
        List<Long> menuIds = new ArrayList<>(30);
        collectIds(response.jsonPath().getList("data"), menuIds, 30);
        Assertions.assertFalse(menuIds.isEmpty(), "租户套餐菜单树不应为空");
        return menuIds;
    }

    /**
     * 深度优先收集树节点 id，累计到上限即停。
     *
     * @param nodes
     *            当前层节点列表
     * @param ids
     *            收集结果
     * @param limit
     *            上限
     */
    private static void collectIds(List<?> nodes, List<Long> ids, int limit) {
        for (Object node : nodes) {
            if (ids.size() >= limit) {
                return;
            }
            Map<?, ?> nodeMap = (Map<?, ?>) node;
            if (nodeMap.get("key") instanceof Number numberId) {
                ids.add(numberId.longValue());
            }
            if (nodeMap.get("children") instanceof List<?> children) {
                collectIds(children, ids, limit);
            }
        }
    }

    /**
     * 创建套餐（关联菜单树前 30 个 id）。
     *
     * @param name
     *            套餐名称
     * @return 套餐 ID
     */
    protected Long createPackage(String name) {
        Map<String, Object> body = Map.of("name", name, "menuIds", collectFirstThirtyMenuIds());
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(PACKAGE_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 创建租户并指定初始管理员（TenantReq Create 组必填字段，密码 RSA 加密传输）。
     *
     * @param name
     *            租户名称
     * @param domain
     *            绑定域名
     * @param packageId
     *            套餐 ID
     * @param adminUsername
     *            管理员用户名
     * @param adminPassword
     *            管理员明文密码
     * @return 租户 ID
     */
    protected Long createTenant(String name, String domain, Long packageId, String adminUsername,
        String adminPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("domain", domain);
        body.put("expireTime", "2099-12-31 08:08:08");
        body.put("packageId", packageId);
        body.put("adminUsername", adminUsername);
        body.put("adminPassword", encryptByRsa(adminPassword));
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(TENANT_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 在测试实例 MySQL 容器内执行 SQL（仅限隔离 project continew-admin-test）。
     * 密码经容器内 MYSQL_PWD 环境变量注入，不落宿主命令行；执行失败即断言失败并带出 stderr。
     *
     * @param sql
     *            单条 SQL（内部使用单引号，不含双引号）
     */
    protected static void execMysql(String sql) {
        String shellCommand =
            "MYSQL_PWD=\"$MYSQL_ROOT_PASSWORD\" mysql -uroot continew_admin -e \"" + sql + "\"";
        ProcessBuilder builder =
            new ProcessBuilder("docker", "exec", MYSQL_CONTAINER, "sh", "-c", shellCommand);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            String output;
            try (InputStream in = process.getInputStream()) {
                output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            Assertions.assertTrue(finished, "MySQL 命令 60 秒未结束: " + sql);
            Assertions.assertEquals(0, process.exitValue(), "MySQL 命令执行失败: " + output.trim());
            log.info("L3 MySQL 执行成功: {}", sql);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assertions.fail("MySQL 命令等待被中断: " + sql);
        } catch (Exception e) {
            Assertions.fail("MySQL 命令执行异常: " + sql + "，原因：" + e.getMessage());
        }
    }

    /**
     * 显式令牌登出（携带该端登录 Cookie 与同源 Origin，POST 空体走 JSON Content-Type），
     * 失败仅告警：用于清理阶段注销多端会话。
     *
     * @param token
     *            Access Token
     * @param cookies
     *            该端登录响应的 Cookie（可为空 Map）
     */
    protected void logoutQuietly(String token, Map<String, String> cookies) {
        try {
            Response response = asToken(token).cookies(cookies == null ? Map.of() : cookies)
                .header("Origin", BASE_ORIGIN)
                .contentType(ContentType.JSON)
                .body("{}")
                .post("/auth/logout");
            log.info("L3 登出完成: HTTP {}，业务码 {}", response.statusCode(),
                response.jsonPath().getString("code"));
        } catch (Exception e) {
            log.warn("L3 登出失败（忽略）：{}", e.getMessage());
        }
    }

    /**
     * 闲置窗口等待：等待时长必须覆盖客户端闲置阈值与 ClientPolicyCache 快照 60 秒 TTL。
     * 基座 awaitConsistency 面向 5 秒内的最终一致性轮询，不适用于该量级的静默窗口，
     * 此处按「先判定后 touch」的判定语义做确定性静置（期间不发任何请求）。
     *
     * @param seconds
     *            静置秒数
     */
    protected static void sleepSeconds(long seconds) {
        try {
            // 一次性闩锁无外部释放者，await 超时返回 false 即达成指定时长的确定性静置；
            // 期间不发任何请求，避免 touch 把闲置计时刷成本次请求时间
            new CountDownLatch(1).await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assertions.fail("静置等待被中断");
        }
    }

    /**
     * 输出响应体前若干字节的十六进制（证据用：区分服务端写坏的 '?'（0x3F）与测试侧解码问题）。
     *
     * @param response
     *            响应
     * @param maxBytes
     *            截取字节数
     * @return 十六进制字符串
     */
    protected static String hexPrefix(Response response, int maxBytes) {
        byte[] bytes = response.getBody().asByteArray();
        StringBuilder hex = new StringBuilder(Math.min(bytes.length, maxBytes) * 3);
        for (int i = 0; i < Math.min(bytes.length, maxBytes); i++) {
            hex.append(String.format("%02x ", bytes[i]));
        }
        return hex.toString().trim();
    }

    /**
     * 判断字符串是否只含 ASCII '?'（0x3F）——容器 Writer 以 ISO-8859-1 写中文时不可映射字符的形态。
     *
     * @param value
     *            待判文本
     * @return 是否全部为 '?'
     */
    protected static boolean isAllQuestionMarks(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.chars().allMatch(c -> c == '?');
    }
}
