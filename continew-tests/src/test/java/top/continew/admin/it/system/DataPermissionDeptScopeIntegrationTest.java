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

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.DataScopeEnum;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 数据权限部门范围集成测试
 *
 * <p>
 * 验证 DataPermissionInterceptor 按 DEPT 范围改写 SQL：探针用户携带 DEPT 数据权限角色，
 * UserMapper.selectList 追加 {@code dept_id = ?} 条件后仅返回本部门行，子树内行与子树外行
 * 在同一结果集上以「子树内可见 + 子树外不可见」双断言锁定过滤边界。UserMapper 的
 * selectList 挂数据权限注解且走内置列名（dept_id），过滤效果完全来自拦截器与上下文。
 * </p>
 * <p>
 * 沿用既有测试惯例：Sa-Token ThreadLocal 会话 + 登录探针用户 ID，使数据权限用户数据提供者
 * 能从登录态取到 userId；UserContextHolder 携带探针角色的 DEPT 范围与探针部门 ID。探针数据
 * 按用户名与角色编码唯一命名（qa_ 前缀 + nanoTime），清理按关联行、用户行、角色行的顺序，
 * 不触碰任何内置数据。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class DataPermissionDeptScopeIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long SEEDED_ROOT_DEPT_ID = 1L;
    private static final Long PROBE_DEPT_ID = 990000000000000001L
        + (System.nanoTime() % 100000000000000000L);
    // sys_role.code 为 varchar(30)，前缀加长会截断报错
    private static final String PROBE_ROLE_CODE = "qa_dps_" + System.nanoTime() % 100000000L;
    private static final String PROBE_USERNAME_IN = "qa_dpscope_in_" + System.nanoTime();
    private static final String PROBE_USERNAME_OUT = "qa_dpscope_out_" + System.nanoTime();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long probeUserId;
    private SaTokenContext originalSaTokenContext;

    @BeforeEach
    void setUpLoginContext() {
        // 数据权限拦截链在 isSuperAdmin 判定时无条件 checkLogin，测试线程需可解析登录态；
        // 登录探针用户 ID 即可，UserContext 由本类装配 DEPT 范围角色
        originalSaTokenContext = SaManager.getSaTokenContext();
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());
        SaTokenContextMockUtil.setMockContext();
    }

    @AfterEach
    void removeProbeData() {
        if (probeUserId != null) {
            StpUtil.logout(probeUserId);
        }
        SaTokenContextMockUtil.clearContext();
        SaManager.setSaTokenContext(originalSaTokenContext);
        UserContextHolder.clearContext();
        if (probeUserId != null) {
            jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", probeUserId);
        }
        jdbcTemplate.update("DELETE FROM sys_user WHERE username = ?", PROBE_USERNAME_IN);
        jdbcTemplate.update("DELETE FROM sys_user WHERE username = ?", PROBE_USERNAME_OUT);
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE role_id IN "
            + "(SELECT id FROM sys_role WHERE code = ?)", PROBE_ROLE_CODE);
        jdbcTemplate.update("DELETE FROM sys_role WHERE code = ?", PROBE_ROLE_CODE);
        jdbcTemplate.update("DELETE FROM sys_dept WHERE id = ?", PROBE_DEPT_ID);
        probeUserId = null;
    }

    /**
     * DEPT 数据权限下 UserMapper.selectList 仅返回本部门行：子树内行可见，子树外行不可见
     */
    @Test
    void selectListShouldFilterByDeptScope() {
        insertProbeRole();
        insertProbeDept();
        Long userInId = insertProbeUser(PROBE_USERNAME_IN, PROBE_DEPT_ID);
        Long userOutId = insertProbeUser(PROBE_USERNAME_OUT, SEEDED_ROOT_DEPT_ID);
        linkUserRole(userInId);
        probeUserId = userInId;

        setUpProbeUserContext(userInId, PROBE_DEPT_ID);

        AtomicReference<List<UserDO>> visibleRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> visibleRef.set(userMapper.selectList(null)));
        assertThat(visibleRef.get()).as("本部门行应在数据权限过滤后可见")
            .anySatisfy(user -> assertThat(user.getId()).isEqualTo(userInId))
            .noneSatisfy(user -> assertThat(user.getId()).as("子树外行不应出现在结果中")
                .isEqualTo(userOutId));
    }

    /**
     * 插入 DEPT 数据权限探针角色
     */
    private void insertProbeRole() {
        jdbcTemplate.update(
            "INSERT INTO sys_role (name, code, data_scope, sort, is_system, create_user, "
                + "create_time, deleted, tenant_id) VALUES (?, ?, ?, 999, b'0', 1, NOW(), 0, 0)",
            "qa_rn_" + System.nanoTime(), PROBE_ROLE_CODE, DataScopeEnum.DEPT
                .getValue());
    }

    /**
     * 插入探针部门（挂在种子根部门下，便于以子树内外区分断言）
     */
    private void insertProbeDept() {
        jdbcTemplate.update(
            "INSERT INTO sys_dept (id, name, parent_id, ancestors, sort, status, is_system, "
                + "create_user, create_time, deleted, tenant_id) "
                + "VALUES (?, ?, ?, ?, 999, 1, b'0', 1, NOW(), 0, 0)",
            PROBE_DEPT_ID, "qa_dn_" + System.nanoTime(), SEEDED_ROOT_DEPT_ID, "0,"
                + SEEDED_ROOT_DEPT_ID);
    }

    /**
     * 插入探针用户
     *
     * @param username 用户名
     * @param deptId   部门 ID
     * @return 用户 ID
     */
    private Long insertProbeUser(String username, Long deptId) {
        jdbcTemplate.update(
            "INSERT INTO sys_user (username, nickname, gender, status, is_system, dept_id, "
                + "create_user, create_time, deleted, tenant_id) "
                + "VALUES (?, ?, 0, 1, b'0', ?, 1, NOW(), 0, 0)",
            username, "qa_un_" + System.nanoTime(), deptId);
        return jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = ?", Long.class,
            username);
    }

    /**
     * 绑定探针用户与探针角色
     *
     * @param userId 用户 ID
     */
    private void linkUserRole(Long userId) {
        Long roleId = jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE code = ?",
            Long.class, PROBE_ROLE_CODE);
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id, tenant_id) "
            + "VALUES (?, ?, 0)", userId, roleId);
    }

    /**
     * 装配探针用户上下文：DEPT 范围角色 + 探针部门 ID，与登录态 ID 一致以通过归属校验
     *
     * @param userId 用户 ID
     * @param deptId 部门 ID
     */
    private void setUpProbeUserContext(Long userId, Long deptId) {
        StpUtil.login(userId);
        UserContext context = new UserContext();
        context.setId(userId);
        context.setUsername(PROBE_USERNAME_IN);
        context.setDeptId(deptId);
        Long roleId = jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE code = ?",
            Long.class, PROBE_ROLE_CODE);
        RoleContext roleContext = new RoleContext(roleId, PROBE_ROLE_CODE, DataScopeEnum.DEPT);
        context.setRoles(Set.of(roleContext));
        UserContextHolder.setContext(context, false);
    }
}
