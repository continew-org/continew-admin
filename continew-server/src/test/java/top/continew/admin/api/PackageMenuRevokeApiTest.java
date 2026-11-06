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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import top.continew.admin.api.support.L3FixtureSupport;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 套餐菜单置空撤销链路回归：套餐改 → 租户角色 sys_role_menu 在 DB 层收缩 → 租户管理员重登后
 * 原菜单接口被拒。
 *
 * <p>
 * 回归 PR #250 覆盖的缺陷面：套餐菜单撤销若不同步清理租户角色的关联菜单，撤销仅在会话层兜底
 * （invalidateSessionsByPackageId 强制重新认证），DB 层 sys_role_menu 残留，用户重登后仍按旧
 * 权限放行。PR #251 修复 updateTenantMenu 先删后查时序后，撤销在 DB 与在线刷新两个面均真实
 * 生效，本用例只对修复后行为做断言。
 * </p>
 * <p>
 * 边界说明：PackageReq.menuIds 标注 @NotEmpty（PUT 空集合被参数校验 400 拦截），黑盒层无法以
 * 字面空集合触发置空撤销；本用例以「套餐菜单收缩到单个目录」触达同一条撤销链路
 * （PackageServiceImpl.update → updateTenantMenu → deleteByNotInMenuIds），被撤销菜单的关联
 * 在 DB 层清零即为置空语义的证据。字面空集合的返回语义由 L1 单测
 * PackageMenuServiceImplAddResultTest 覆盖。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class PackageMenuRevokeApiTest extends L3FixtureSupport {

    /**
     * 主流程：建套餐+租户+管理员 → 正向授权锚点 → 收缩撤销套餐菜单 → DB 断言租户角色关联清零
     * → 重登后原菜单接口 401/403。
     */
    @Test
    void packageMenuRevokeCleansTenantRoleMenuAndRejectsAfterRelogin() {
        long timestamp = System.currentTimeMillis();
        String adminUsername = "qa_pmr_admin_" + timestamp;
        // 口令匹配 PASSWORD 正则：8-32 位，含小写字母与数字
        String adminPassword = "QaPmr1" + timestamp;
        Long packageId = null;
        Long tenantId = null;
        try {
            // 步骤 1：建套餐 + 租户 + 租户管理员。撤销目标 2031 必须在套餐菜单内，故在
            // collectFirstThirtyMenuIds 基础上显式并入 2030/2031，不直接用 createPackage 的固定集合
            Set<Long> packageMenuIds = new LinkedHashSet<>(collectFirstThirtyMenuIds());
            packageMenuIds.add(MENU_ID_MONITOR_LOG_DIR);
            packageMenuIds.add(MENU_ID_MONITOR_LOG_LIST);
            packageId = createPackageWithMenus("QA-PMR-" + timestamp + "-PKG", packageMenuIds);
            tenantId = createTenant("qa_pmr_t_" + timestamp, "qa-pmr-" + timestamp + ".test.local",
                packageId, adminUsername, adminPassword);
            Assertions.assertNotNull(tenantId, "步骤 1：创建租户应返回非空 ID");

            // 步骤 2：正向授权锚点——租户管理员持有撤销目标权限，受保护端点 200。
            // 无此锚点，步骤 5 的拒绝无法区分「撤销生效」与「从未授权」
            useTenant(tenantId);
            String adminToken = login(adminUsername, adminPassword);
            Response logPageBefore = asToken(adminToken, tenantId).get(LOG_PATH);
            assertBizOk(logPageBefore);
            List<String> permissionsBefore = permissionsOf(userInfo(adminToken, tenantId));
            Assertions.assertTrue(permissionsBefore.contains(PERM_MONITOR_LOG_LIST), "步骤 2：撤销前"
                + "权限快照应含 " + PERM_MONITOR_LOG_LIST + "，实际 " + permissionsBefore);

            // 步骤 3：收缩撤销——套餐菜单收缩到单个目录，触发 updateTenantMenu 对租户角色关联菜单的同步
            useTenant(DEFAULT_TENANT_ID);
            loginAsDefaultAdmin();
            Long keepMenuId = packageMenuIds.iterator().next();
            Assertions.assertNotEquals(MENU_ID_MONITOR_LOG_LIST, keepMenuId,
                "步骤 3：保留菜单不得与撤销目标相同");
            assertBizOk(givenAuth().contentType(ContentType.JSON)
                .body(
                    Map.of("name", "QA-PMR-" + timestamp + "-PKG", "menuIds", List.of(keepMenuId)))
                .put(PACKAGE_PATH + "/" + packageId));

            // 步骤 4：DB 断言——租户角色 sys_role_menu 中被撤销菜单的关联清零，仅剩套餐保留菜单。
            // 行数读回 Java 侧断言，残留即失败并带出实际计数
            assertTenantRoleMenuShrunk(tenantId, keepMenuId);

            // 步骤 5：重登拒绝——撤销后新建会话的权限快照不含撤销目标，原菜单接口 401/403 而非 200。
            // 重登置于轮询内，覆盖 invalidateSessionsByPackageId 会话失效的传播时序
            awaitReloginRejection(adminUsername, adminPassword, tenantId);
        } finally {
            cleanupPlatformSide();
            if (tenantId != null) {
                deleteQuietly(TENANT_PATH + "/" + tenantId);
            }
            if (packageId != null) {
                deleteQuietly(PACKAGE_PATH + "/" + packageId);
            }
        }
    }

    /**
     * 创建套餐并指定菜单集合：与 {@link L3FixtureSupport#createPackage(String)} 的差异仅在
     * menuIds 来源，本用例需要保证撤销目标菜单在套餐内。
     *
     * @param name
     *            套餐名称
     * @param menuIds
     *            套餐菜单 ID 集合
     * @return 套餐 ID
     */
    private Long createPackageWithMenus(String name, Set<Long> menuIds) {
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(Map.of("name", name, "menuIds", menuIds))
            .post(PACKAGE_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 测试 MySQL 容器名（与 {@link L3FixtureSupport} 同源配置：compose project
     * continew-admin-test 默认命名，可经 -Dapi.mysql-container / API_MYSQL_CONTAINER 覆盖）。
     */
    private static final String MYSQL_CONTAINER =
        config("api.mysql-container", "API_MYSQL_CONTAINER", "continew-admin-test-mysql-test-1");

    /**
     * 断言租户角色 sys_role_menu 已收缩为套餐保留菜单：被撤销菜单的关联数为 0 且总关联数恰为 1。
     *
     * @param tenantId
     *            租户 ID
     * @param keepMenuId
     *            撤销后套餐保留的菜单 ID
     */
    private void assertTenantRoleMenuShrunk(Long tenantId, Long keepMenuId) {
        awaitConsistency(() -> {
            String[] counts = queryMysql("SELECT (SELECT COUNT(*) FROM sys_role_menu "
                + "WHERE tenant_id = " + tenantId + " AND menu_id <> " + keepMenuId
                + "), (SELECT COUNT(*) FROM sys_role_menu WHERE tenant_id = " + tenantId
                + ")").trim().split("\\t");
            Assertions.assertEquals("0", counts[0].trim(), "被撤销菜单在租户角色 sys_role_menu 的"
                + "关联残留数应为 0，实际 " + counts[0].trim());
            Assertions.assertEquals("1", counts[1].trim(), "租户角色 sys_role_menu 应仅剩套餐保留"
                + "菜单 1 条关联，实际 " + counts[1].trim());
        });
    }

    /**
     * 在测试实例 MySQL 容器内执行查询 SQL 并读回 stdout：与 {@link L3FixtureSupport#execMysql}
     * 同一执行通道与认证注入方式，结果读回 Java 侧断言，不依赖 SQL 层的求值语义。
     *
     * @param sql
     *            单条查询 SQL（内部使用单引号，不含双引号）
     * @return 查询输出文本（-N 跳过列名行，仅数据行）
     */
    private static String queryMysql(String sql) {
        String shellCommand =
            "MYSQL_PWD=\"$MYSQL_ROOT_PASSWORD\" mysql -uroot -N continew_admin -e \"" + sql + "\"";
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
            return output;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Assertions.fail("MySQL 命令等待被中断: " + sql);
        } catch (Exception e) {
            return Assertions.fail("MySQL 命令执行异常: " + sql + "，原因：" + e.getMessage());
        }
    }

    /**
     * 轮询断言重登拒绝：新建会话的权限快照不含撤销目标权限，原菜单对应接口 401/403 而非 200。
     *
     * @param adminUsername
     *            租户管理员用户名
     * @param adminPassword
     *            租户管理员明文密码
     * @param tenantId
     *            租户 ID
     */
    private void awaitReloginRejection(String adminUsername, String adminPassword, Long tenantId) {
        useTenant(tenantId);
        awaitConsistency(() -> {
            String reloginToken = login(adminUsername, adminPassword);
            Response logPageAfter = asToken(reloginToken, tenantId).get(LOG_PATH);
            int status = logPageAfter.statusCode();
            Assertions.assertTrue(status == 401 || status == 403, "重登后原菜单接口应 401/403 而非 "
                + "200，实际 HTTP " + status + "，响应体：" + sanitize(logPageAfter.getBody()
                    .asString()));
            Assertions.assertFalse(permissionsOf(userInfo(reloginToken, tenantId))
                .contains(PERM_MONITOR_LOG_LIST), "重登后权限快照不应再含 " + PERM_MONITOR_LOG_LIST);
        });
    }

    /**
     * 清理前统一切回平台侧并重登超管（中途登录态可能停留在租户侧）。
     */
    private void cleanupPlatformSide() {
        useTenant(DEFAULT_TENANT_ID);
        try {
            loginAsDefaultAdmin();
        } catch (Exception e) {
            log.warn("L3 清理前重登超管失败，后续清理可能因无权限跳过：{}", e.getMessage());
        }
    }
}
