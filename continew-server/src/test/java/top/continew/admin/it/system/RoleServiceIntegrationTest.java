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

package top.continew.admin.it.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.model.req.RolePermissionUpdateReq;
import top.continew.admin.system.service.RoleService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.List;

/**
 * 角色 Service 集成测试（L2）
 *
 * <p>
 * 聚焦黑盒接口覆盖不到的服务层点：系统内置角色（is_system）的删除与功能权限修改保护、
 * 权限分配事务落 sys_role_menu 并失效 ROLE_MENU 缓存。缓存与菜单关联属于服务层内部
 * 副作用，HTTP 层无法直接观测。
 * </p>
 * <p>
 * 自建角色按 code 唯一命名（it_role_ 前缀）并物理清理，不触碰任何内置角色数据。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class RoleServiceIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long SUPER_ADMIN_ROLE_ID = 1L;
    private static final Long SEEDED_MENU_ID = 1000L;
    private static final String PROBE_ROLE_CODE = "it_role_" + System.nanoTime();

    @Autowired
    private RoleService roleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void removeProbeData() {
        jdbcTemplate.update(
            "DELETE FROM sys_role_menu WHERE role_id IN (SELECT id FROM sys_role WHERE code = ?)",
            PROBE_ROLE_CODE);
        jdbcTemplate.update("DELETE FROM sys_role WHERE code = ?", PROBE_ROLE_CODE);
    }

    /**
     * 删除系统内置角色应被拒绝，角色数据保持不变
     */
    @Test
    void deleteShouldRejectSystemRole() {
        assertThatThrownBy(() -> TenantUtils.execute(DEFAULT_TENANT_ID, () -> roleService
            .delete(List.of(SUPER_ADMIN_ROLE_ID))))
            .as("内置角色删除应被拒绝")
            .isInstanceOf(BusinessException.class)
            .hasMessage("所选角色 [超级管理员] 是系统内置角色，不允许删除");

        Long rowCount =
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role WHERE id = ?", Long.class,
                SUPER_ADMIN_ROLE_ID);
        assertThat(rowCount).as("内置角色不应被删除").isEqualTo(1L);
    }

    /**
     * 修改系统内置角色的功能权限应被拒绝
     */
    @Test
    void updatePermissionShouldRejectSystemRole() {
        RolePermissionUpdateReq req = new RolePermissionUpdateReq();
        req.setMenuIds(List.of(SEEDED_MENU_ID));
        req.setMenuCheckStrictly(true);

        assertThatThrownBy(() -> TenantUtils.execute(DEFAULT_TENANT_ID, () -> roleService
            .updatePermission(SUPER_ADMIN_ROLE_ID, req)))
            .as("内置角色功能权限修改应被拒绝")
            .isInstanceOf(BusinessException.class)
            .hasMessage("[超级管理员] 是系统内置角色，不允许修改角色功能权限");

        Long linkCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_role_menu WHERE role_id = ?", Long.class,
            SUPER_ADMIN_ROLE_ID);
        assertThat(linkCount).as("被拒绝的修改不应写入角色菜单关联").isZero();
    }

    /**
     * 自建角色分配权限应写入 sys_role_menu、更新父子关联开关并失效 ROLE_MENU 缓存
     */
    @Test
    void updatePermissionShouldWriteRoleMenuAndEvictCache() {
        Long roleId = insertProbeRole();

        CacheUtils.put(CacheConstants.ROLE_MENU_KEY_PREFIX, roleId, List.of("stub"));
        RolePermissionUpdateReq req = new RolePermissionUpdateReq();
        req.setMenuIds(List.of(SEEDED_MENU_ID));
        req.setMenuCheckStrictly(true);
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> roleService.updatePermission(roleId, req));

        Long linkCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_role_menu WHERE role_id = ? AND menu_id = ?", Long.class,
            roleId,
            SEEDED_MENU_ID);
        assertThat(linkCount).as("权限分配应写入角色菜单关联").isEqualTo(1L);

        Boolean menuCheckStrictly = jdbcTemplate.queryForObject(
            "SELECT menu_check_strictly FROM sys_role WHERE id = ?", Boolean.class, roleId);
        assertThat(menuCheckStrictly).as("父子菜单关联开关应同步更新").isTrue();

        Object cached = CacheUtils.get(CacheConstants.ROLE_MENU_KEY_PREFIX, roleId);
        assertThat(cached).as("权限分配后角色菜单缓存应失效").isNull();
    }

    /**
     * 插入自建角色探针行（非内置、全部数据权限）
     *
     * @return 角色 ID
     */
    private Long insertProbeRole() {
        jdbcTemplate.update(
            "INSERT INTO sys_role (name, code, data_scope, sort, is_system, create_user, "
                + "create_time, deleted, tenant_id) VALUES (?, ?, 1, 999, b'0', 1, NOW(), 0, 0)",
            "IT集成角色" + System.nanoTime(), PROBE_ROLE_CODE);
        return jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE code = ?", Long.class,
            PROBE_ROLE_CODE);
    }
}
