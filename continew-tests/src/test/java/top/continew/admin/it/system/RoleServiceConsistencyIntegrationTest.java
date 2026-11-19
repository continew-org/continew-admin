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

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.DataScopeEnum;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.model.query.RoleQuery;
import top.continew.admin.system.model.req.RoleReq;
import top.continew.admin.system.model.resp.role.RoleResp;
import top.continew.admin.system.service.RoleService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 角色 Service 数据一致性集成测试
 *
 * <p>
 * 聚焦黑盒接口覆盖不到的服务层点：create 在 sys_role 已写入后于 sys_role_dept 阶段失败时
 * 整体回滚（关联插入撞主键为最晚步骤，可直接放大成失败信号）、编码与名称重复的拒绝不产生
 * 脏行（拒绝发生在两阶段关联写入之前，光看表行数无法区分事务回滚与前置校验两种机制）、
 * 删除后的行保留但业务查询（dict/page）不可见。
 * </p>
 * <p>
 * 探针角色按 code 唯一命名（qa_ 前缀 + nanoTime）并在用例后按关联行、角色行的顺序物理清理；
 * 部门探针行引用种子部门链，不触碰任何内置数据。写操作与业务查询均处于种子超管登录态
 * （create_user 非空列与防自删校验依赖用户上下文），探针数据均为自建行。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class RoleServiceConsistencyIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long SEEDED_PARENT_DEPT_ID = 547887852587843590L;
    private static final Long SEEDED_ROOT_DEPT_ID = 1L;
    private static final String PROBE_ROLE_CODE = "qa_role_it_" + System.nanoTime();

    @Autowired
    private RoleService roleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long probeDeptId;

    private SaTokenContext originalSaTokenContext;

    @BeforeEach
    void setUpUserContext() {
        // create 的 insertFill 依赖用户上下文填充 create_user（非空列），delete 的防自删校验
        // 与 dict 的租户管理员判定均需登录态；登录种子超管并让 ThreadLocal 上下文携带超管角色
        originalSaTokenContext = SaManager.getSaTokenContext();
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());
        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(1L);
        UserContext operator = new UserContext();
        operator.setId(1L);
        operator.setUsername("admin");
        operator.setRoleCodes(Set.of("super_admin"));
        UserContextHolder.setContext(operator, false);
    }

    @AfterEach
    void removeProbeData() {
        StpUtil.logout(1L);
        SaTokenContextMockUtil.clearContext();
        SaManager.setSaTokenContext(originalSaTokenContext);
        UserContextHolder.clearContext();
        // 先删关联行再删主表行；data_scope 用例创建的自建部门独立清理
        jdbcTemplate.update("DELETE FROM sys_role_dept WHERE role_id IN "
            + "(SELECT id FROM sys_role WHERE code = ?)", PROBE_ROLE_CODE);
        jdbcTemplate.update("DELETE FROM sys_role WHERE code = ?", PROBE_ROLE_CODE);
        if (probeDeptId != null) {
            jdbcTemplate.update("DELETE FROM sys_dept WHERE id = ?", probeDeptId);
            probeDeptId = null;
        }
    }

    /**
     * create 撞已存在编码应被拒绝，且不残留任何角色行
     */
    @Test
    void createShouldRejectDuplicateCode() {
        insertProbeRole(DataScopeEnum.ALL, null);

        RoleReq req = buildRoleReq(PROBE_ROLE_CODE, "qa_rn_" + System.nanoTime());
        assertThatThrownBy(() -> TenantUtils
            .execute(DEFAULT_TENANT_ID, () -> roleService.create(req)))
            .as("重复编码应被拒绝")
            .isInstanceOf(BusinessException.class)
            .hasMessage("编码为 [%s] 的角色已存在".formatted(PROBE_ROLE_CODE));

        Long rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_role WHERE code = ?", Long.class, PROBE_ROLE_CODE);
        assertThat(rowCount).as("唯一冲突不应产生新角色行").isEqualTo(1L);
    }

    /**
     * create 撞已存在名称应被拒绝，且不残留任何角色行
     */
    @Test
    void createShouldRejectDuplicateName() {
        String probeName = "qa_rn_" + System.nanoTime();
        insertProbeRole(DataScopeEnum.ALL, probeName);

        RoleReq req = buildRoleReq("qa_code_" + System.nanoTime(), probeName);
        assertThatThrownBy(() -> TenantUtils
            .execute(DEFAULT_TENANT_ID, () -> roleService.create(req)))
            .as("重复名称应被拒绝")
            .isInstanceOf(BusinessException.class)
            .hasMessage("名称为 [%s] 的角色已存在".formatted(probeName));

        Long rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_role WHERE name = ?", Long.class, probeName);
        assertThat(rowCount).as("唯一冲突不应产生新角色行").isEqualTo(1L);
    }

    /**
     * create 在角色行已写入、角色部门关联插入撞主键时应整体回滚，角色行不残留
     */
    @Test
    void createShouldRollbackRoleWhenRoleDeptInsertFails() {
        // 关联表主键 (role_id, dept_id)，插入重复 dept_id 即失败，是 create 事务内最晚的写入步骤
        RoleReq req = buildRoleReq(PROBE_ROLE_CODE, "qa_rn_" + System.nanoTime());
        req.setDeptIds(List.of(SEEDED_PARENT_DEPT_ID, SEEDED_PARENT_DEPT_ID));
        assertThatThrownBy(() -> TenantUtils
            .execute(DEFAULT_TENANT_ID, () -> roleService.create(req)))
            .as("重复部门 ID 的关联插入应失败")
            .isInstanceOf(PersistenceException.class);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role WHERE code = ?",
            Long.class, PROBE_ROLE_CODE)).as("事务回滚后角色行不应残留").isZero();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_role_dept WHERE dept_id = ? AND role_id IN "
                + "(SELECT id FROM sys_role WHERE code = ?)",
            Long.class, SEEDED_PARENT_DEPT_ID,
            PROBE_ROLE_CODE)).as("事务回滚后角色部门关联不应残留").isZero();
    }

    /**
     * 删除角色后业务查询（dict/page）不可见，表行保留且 deleted 置为自身 ID
     */
    @Test
    void deleteShouldKeepRowButHideFromBusinessQueries() {
        Long roleId = insertProbeRole(DataScopeEnum.ALL, null);
        insertRoleDeptLink(roleId, SEEDED_ROOT_DEPT_ID);

        TenantUtils.execute(DEFAULT_TENANT_ID, () -> roleService.delete(List.of(roleId)));

        Long deletedFlag = jdbcTemplate.queryForObject(
            "SELECT deleted FROM sys_role WHERE id = ?", Long.class, roleId);
        assertThat(deletedFlag).as("逻辑删除应将 deleted 置为自身 ID").isEqualTo(roleId);

        AtomicReference<List<LabelValueResp>> dictRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> dictRef.set(roleService
            .dict(new RoleQuery(), new SortQuery())));
        assertThat(dictRef.get()).as("删除后 dict 不应返回该角色")
            .noneMatch(item -> item.getValue().equals(roleId));

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(1);
        pageQuery.setSize(100);
        AtomicReference<BasePageResp<RoleResp>> pageRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> pageRef.set(roleService
            .page(new RoleQuery(), pageQuery)));
        assertThat(pageRef.get().getList()).as("删除后 page 不应返回该角色")
            .noneMatch(resp -> resp.getId().equals(roleId));
    }

    /**
     * 构造角色创建请求（自定义数据权限，关联指定部门）
     *
     * @param code 编码
     * @param name 名称
     * @return 创建请求
     */
    private RoleReq buildRoleReq(String code, String name) {
        RoleReq req = new RoleReq();
        req.setName(name);
        req.setCode(code);
        req.setDataScope(DataScopeEnum.CUSTOM);
        req.setDeptIds(List.of(SEEDED_ROOT_DEPT_ID));
        return req;
    }

    /**
     * 插入自建角色探针行（非内置）
     *
     * @param dataScope 数据权限
     * @param name      名称（null 时取 nanoTime 唯一值）
     * @return 角色 ID
     */
    private Long insertProbeRole(DataScopeEnum dataScope, String name) {
        jdbcTemplate.update(
            "INSERT INTO sys_role (name, code, data_scope, sort, is_system, create_user, "
                + "create_time, deleted, tenant_id) VALUES (?, ?, ?, 999, b'0', 1, NOW(), 0, 0)",
            name != null ? name : "qa_rn_" + System.nanoTime(), PROBE_ROLE_CODE, dataScope
                .getValue());
        return jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE code = ?", Long.class,
            PROBE_ROLE_CODE);
    }

    /**
     * 直插角色部门关联行
     *
     * @param roleId 角色 ID
     * @param deptId 部门 ID
     */
    private void insertRoleDeptLink(Long roleId, Long deptId) {
        jdbcTemplate.update("INSERT INTO sys_role_dept (role_id, dept_id, tenant_id) "
            + "VALUES (?, ?, 0)", roleId, deptId);
    }
}
