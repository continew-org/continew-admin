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

package top.continew.admin.it.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.service.UserService;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户删除数据清理边界集成测试
 *
 * <p>
 * 验证删除租户仅清理其自身数据：clear() 中每条删除语句的 WHERE 均为恒真条件，行级拦截器必须
 * 对全部语句追加目标租户条件，否则全表清空会连带抹除其他租户数据。探针行经 JdbcTemplate 直插
 * （绕过 ORM 与租户拦截），默认租户 0 与其他租户 888 的有效行以插入完成后的实际快照为基线断言，
 * 清理效果只能来自删除链路的真实 SQL 范围。
 * </p>
 * <p>
 * 7 张逻辑删除表（deleted 列）的删除被改写为 UPDATE，按 deleted = 0 统计有效行；其余 6 张表
 * 为物理删除，直接统计行数。探针行按唯一后缀命名并在用例后物理清理，容器销毁亦不残留。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class TenantClearBoundaryIntegrationTest extends BaseIntegrationTest {

    private static final Long TARGET_TENANT_ID = 999L;
    private static final Long OTHER_TENANT_ID = 888L;
    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long PROBE_PACKAGE_ID = 990001L;
    private static final List<Long> PROBE_TENANT_IDS = List.of(TARGET_TENANT_ID, OTHER_TENANT_ID,
        DEFAULT_TENANT_ID);

    /**
     * clear() 涉及的 13 张业务表（与 TenantDataApiForSystemImpl#clear 的删除面一致）
     */
    private static final List<String> CLEAR_INVOLVED_TABLES =
        List.of("sys_dept", "sys_file", "sys_log",
            "sys_message", "sys_message_log", "sys_notice", "sys_role", "sys_role_dept",
            "sys_role_menu",
            "sys_user", "sys_user_password_history", "sys_user_role", "sys_user_social");

    /**
     * 实体含 deleted 列的表：clear() 的删除被改写为 UPDATE deleted = id，行仍在表中，
     * 有效行计数须追加 deleted = 0 过滤
     */
    private static final List<String> LOGICAL_DELETE_TABLES =
        List.of("sys_dept", "sys_file", "sys_message",
            "sys_notice", "sys_role", "sys_user", "sys_user_social");

    @Autowired
    private TenantService tenantService;
    @Autowired
    private UserService userService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String suffix = String.valueOf(System.nanoTime());
    private final Map<String, Long> defaultTenantCounts =
        new HashMap<>(CLEAR_INVOLVED_TABLES.size());
    private final Map<String, Long> otherTenantCounts = new HashMap<>(CLEAR_INVOLVED_TABLES.size());

    @BeforeEach
    void insertProbeData() {
        // 租户与套餐表均在 ignore-tables 中，不经行级隔离，直插后供探针租户引用
        jdbcTemplate.update("INSERT INTO tenant_package (id, name, create_user, create_time) "
            + "VALUES (?, ?, 1, NOW())", PROBE_PACKAGE_ID, probeValue("pk", 0L));
        for (Long tenantId : List.of(TARGET_TENANT_ID, OTHER_TENANT_ID)) {
            jdbcTemplate.update("INSERT INTO tenant (id, name, code, package_id, create_user, "
                + "create_time) VALUES (?, ?, ?, ?, 1, NOW())", tenantId,
                probeValue("tn", tenantId),
                probeValue("tc", tenantId), PROBE_PACKAGE_ID);
        }
        for (String table : CLEAR_INVOLVED_TABLES) {
            for (Long tenantId : PROBE_TENANT_IDS) {
                insertProbeRow(table, tenantId);
            }
        }
        // 删除基线取插入完成后的实际行数（含种子数据），删除动作只允许影响目标租户
        for (String table : CLEAR_INVOLVED_TABLES) {
            defaultTenantCounts.put(table, countVisibleRows(table, DEFAULT_TENANT_ID));
            otherTenantCounts.put(table, countVisibleRows(table, OTHER_TENANT_ID));
        }
    }

    @AfterEach
    void removeProbeData() {
        for (Long tenantId : PROBE_TENANT_IDS) {
            jdbcTemplate.update("DELETE FROM sys_dept WHERE name = ?", probeValue("dp", tenantId));
            jdbcTemplate.update("DELETE FROM sys_role WHERE code = ?", probeValue("rc", tenantId));
            jdbcTemplate.update("DELETE FROM sys_user WHERE username = ?",
                probeValue("uu", tenantId));
            jdbcTemplate.update("DELETE FROM sys_user_social WHERE open_id = ?",
                probeValue("so", tenantId));
            jdbcTemplate
                .update("DELETE FROM sys_user_password_history WHERE user_id = ?",
                    probeUserId(tenantId));
            jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ? AND role_id = ?",
                probeUserId(tenantId), probeRoleId(tenantId));
            jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ? AND menu_id = ?",
                probeRoleId(tenantId), probeMenuId(tenantId));
            jdbcTemplate.update("DELETE FROM sys_role_dept WHERE role_id = ? AND dept_id = ?",
                probeRoleId(tenantId), probeDeptId(tenantId));
            jdbcTemplate.update("DELETE FROM sys_log WHERE description = ?",
                probeValue("lg", tenantId));
            jdbcTemplate.update("DELETE FROM sys_message WHERE title = ?",
                probeValue("ms", tenantId));
            jdbcTemplate.update("DELETE FROM sys_message_log WHERE message_id = ? AND user_id = ?",
                probeMessageId(tenantId), probeUserId(tenantId));
            jdbcTemplate.update("DELETE FROM sys_notice WHERE title = ?",
                probeValue("nt", tenantId));
            jdbcTemplate.update("DELETE FROM sys_file WHERE path = ?", probePath(tenantId));
        }
        jdbcTemplate.update("DELETE FROM tenant WHERE id IN (?, ?)", TARGET_TENANT_ID,
            OTHER_TENANT_ID);
        jdbcTemplate.update("DELETE FROM tenant_package WHERE id = ?", PROBE_PACKAGE_ID);
    }

    /**
     * 删除租户 999 后：目标租户 13 表有效行清零，默认租户 0 与其他租户 888 有效行保持基线，
     * 种子 admin 用户仍在且可被登录链路解析
     */
    @Test
    void tenantClearShouldOnlyAffectTargetTenant() {
        // 平台侧默认租户上下文中执行删除，与租户管理页面的真实调用视角一致
        TenantUtils.execute(DEFAULT_TENANT_ID,
            () -> tenantService.delete(List.of(TARGET_TENANT_ID)));

        for (String table : CLEAR_INVOLVED_TABLES) {
            assertThat(countVisibleRows(table, TARGET_TENANT_ID))
                .as("删除租户后 %s 中租户 %d 的有效行应清零", table, TARGET_TENANT_ID)
                .isZero();
            assertThat(countVisibleRows(table, DEFAULT_TENANT_ID))
                .as("删除租户后 %s 中默认租户 %d 的有效行应保持基线", table, DEFAULT_TENANT_ID)
                .isEqualTo(defaultTenantCounts.get(table));
            assertThat(countVisibleRows(table, OTHER_TENANT_ID))
                .as("删除租户后 %s 中其他租户 %d 的有效行应保持基线", table, OTHER_TENANT_ID)
                .isEqualTo(otherTenantCounts.get(table));
        }
        assertThat(countDefaultAdmin()).as("默认租户种子 admin 用户应保留").isEqualTo(1L);
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> assertThat(userService.getByUsername("admin"))
            .as("删除租户后 admin 应仍可被登录链路的用户名查询解析")
            .isNotNull());
    }

    /**
     * 删除请求命中默认租户 0 时被明确拒绝，且被拒绝的请求不产生任何数据清理副作用
     */
    @Test
    void deleteShouldRejectDefaultTenant() {
        assertThatThrownBy(() -> TenantUtils.execute(DEFAULT_TENANT_ID,
            () -> tenantService.delete(List.of(DEFAULT_TENANT_ID))))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("默认租户不允许删除");

        for (String table : CLEAR_INVOLVED_TABLES) {
            assertThat(countVisibleRows(table, DEFAULT_TENANT_ID))
                .as("删除默认租户被拒绝后 %s 中默认租户有效行应保持基线", table)
                .isEqualTo(defaultTenantCounts.get(table));
            assertThat(countVisibleRows(table, OTHER_TENANT_ID))
                .as("删除默认租户被拒绝后 %s 中其他租户有效行应保持基线", table)
                .isEqualTo(otherTenantCounts.get(table));
            assertThat(countVisibleRows(table, TARGET_TENANT_ID))
                .as("删除默认租户被拒绝后 %s 中探针租户数据不应被波及", table)
                .isEqualTo(1L);
        }
    }

    /**
     * 向指定表插入当前租户的探针行，各表列清单与 Liquibase DDL 对齐，仅填业务必需列，
     * 其余依赖列默认值
     *
     * @param table    表名
     * @param tenantId 租户 ID
     */
    private void insertProbeRow(String table, Long tenantId) {
        switch (table) {
            case "sys_dept" -> jdbcTemplate.update(
                "INSERT INTO sys_dept (name, create_user, create_time, tenant_id) VALUES (?, 1, NOW(), ?)",
                probeValue("dp", tenantId), tenantId);
            case "sys_file" -> jdbcTemplate.update(
                "INSERT INTO sys_file (name, original_name, path, type, storage_id, create_user, "
                    + "create_time, tenant_id) VALUES (?, ?, ?, 0, 1, 1, NOW(), ?)",
                probeValue("fn", tenantId), probeValue("fo", tenantId), probePath(tenantId),
                tenantId);
            case "sys_log" -> jdbcTemplate.update(
                "INSERT INTO sys_log (description, module, request_url, request_method, status_code, "
                    + "time_taken, create_time, tenant_id) VALUES (?, ?, ?, ?, 200, 0, NOW(), ?)",
                probeValue("lg", tenantId), "probe", "/probe", "GET", tenantId);
            case "sys_message" -> jdbcTemplate.update(
                "INSERT INTO sys_message (title, create_time, tenant_id) VALUES (?, NOW(), ?)",
                probeValue("ms", tenantId), tenantId);
            case "sys_message_log" -> jdbcTemplate.update(
                "INSERT INTO sys_message_log (message_id, user_id, tenant_id) VALUES (?, ?, ?)",
                probeMessageId(tenantId), probeUserId(tenantId), tenantId);
            case "sys_notice" -> jdbcTemplate.update(
                "INSERT INTO sys_notice (title, content, type, create_user, create_time, tenant_id) "
                    + "VALUES (?, ?, ?, 1, NOW(), ?)",
                probeValue("nt", tenantId), "probe", "ACTIVITY", tenantId);
            case "sys_role" -> jdbcTemplate.update(
                "INSERT INTO sys_role (name, code, create_user, create_time, tenant_id) "
                    + "VALUES (?, ?, 1, NOW(), ?)",
                probeValue("rn", tenantId), probeValue("rc", tenantId), tenantId);
            case "sys_role_dept" -> jdbcTemplate.update(
                "INSERT INTO sys_role_dept (role_id, dept_id, tenant_id) VALUES (?, ?, ?)",
                probeRoleId(tenantId), probeDeptId(tenantId), tenantId);
            case "sys_role_menu" -> jdbcTemplate.update(
                "INSERT INTO sys_role_menu (role_id, menu_id, tenant_id) VALUES (?, ?, ?)",
                probeRoleId(tenantId), probeMenuId(tenantId), tenantId);
            case "sys_user" -> jdbcTemplate.update(
                "INSERT INTO sys_user (username, nickname, dept_id, create_time, tenant_id) "
                    + "VALUES (?, ?, 1, NOW(), ?)",
                probeValue("uu", tenantId), probeValue("un", tenantId), tenantId);
            case "sys_user_password_history" -> jdbcTemplate.update(
                "INSERT INTO sys_user_password_history (user_id, password, create_time, tenant_id) "
                    + "VALUES (?, ?, NOW(), ?)",
                probeUserId(tenantId), probeValue("ph", tenantId), tenantId);
            case "sys_user_role" -> jdbcTemplate.update(
                "INSERT INTO sys_user_role (user_id, role_id, tenant_id) VALUES (?, ?, ?)",
                probeUserId(tenantId), probeRoleId(tenantId), tenantId);
            case "sys_user_social" -> jdbcTemplate.update(
                "INSERT INTO sys_user_social (source, open_id, user_id, create_time, tenant_id) "
                    + "VALUES (?, ?, ?, NOW(), ?)",
                "probe_source", probeValue("so", tenantId), probeUserId(tenantId), tenantId);
            default -> throw new IllegalStateException("未覆盖的探针表: " + table);
        }
    }

    /**
     * 生成当前用例唯一命名的探针值（各表唯一约束或按此列精确清理）
     *
     * @param prefix   业务前缀（各表独立）
     * @param tenantId 租户 ID
     * @return 探针值
     */
    private String probeValue(String prefix, Long tenantId) {
        return prefix + tenantId + "_" + suffix;
    }

    private String probePath(Long tenantId) {
        return "/probe/" + probeValue("fp", tenantId) + "/";
    }

    // 探针关联 ID 采用远离自增与雪花 ID 区间的固定基数，跨租户错开且可与种子数据区分
    private long probeUserId(Long tenantId) {
        return 71000000L + tenantId;
    }

    private long probeRoleId(Long tenantId) {
        return 72000000L + tenantId;
    }

    private long probeMenuId(Long tenantId) {
        return 73000000L + tenantId;
    }

    private long probeDeptId(Long tenantId) {
        return 74000000L + tenantId;
    }

    private long probeMessageId(Long tenantId) {
        return 75000000L + tenantId;
    }

    /**
     * 统计租户在指定表中的有效行数：逻辑删除表过滤 deleted = 0，物理删除表统计全部行
     *
     * @param table    表名
     * @param tenantId 租户 ID
     * @return 有效行数
     */
    private long countVisibleRows(String table, Long tenantId) {
        String deletedFilter = LOGICAL_DELETE_TABLES.contains(table) ? " AND deleted = 0" : "";
        Long count =
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE tenant_id = ?"
                + deletedFilter, Long.class, tenantId);
        return count == null ? 0L : count;
    }

    private long countDefaultAdmin() {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_user WHERE username = 'admin' AND tenant_id = 0 AND deleted = 0",
            Long.class);
        return count == null ? 0L : count;
    }
}
