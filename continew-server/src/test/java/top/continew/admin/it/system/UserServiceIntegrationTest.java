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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.common.config.RsaProperties;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.GenderEnum;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.constant.SystemConstants;
import top.continew.admin.system.model.req.user.UserReq;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 用户 Service 集成测试（L2）
 *
 * <p>
 * 聚焦黑盒接口覆盖不到的服务层点：用户名唯一冲突校验走加密列之前的明文查询、软删除
 * （deleted 置为自身 ID）后同名可重建、update 事务在角色校验失败时整体回滚。密码经
 * RSA 公钥加密并以 Base64 输出提交，与前端加密行为一致。
 * </p>
 * <p>
 * 类级夹具在测试线程初始化 Sa-Token 会话并登录 id=1 超管（UserContextHolder.isSuperAdmin
 * 无条件 checkLogin，仅 ThreadLocal 上下文救不了会话层），数据权限 handler 对超管短路不过滤。
 * UserMapper 走数据权限拦截而软删重建路径涉及 selectList 形态，重建改由直插承载唯一索引
 * 语义。自建数据按 username 唯一命名（it_ 前缀）并在用例后物理清理。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class UserServiceIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long GENERAL_ROLE_ID = 3L;
    private static final Long SUPER_ADMIN_ROLE_ID = 1L;
    private static final String RAW_PASSWORD = "Password123";

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String probeUsername;

    private SaTokenContext originalSaTokenContext;

    @BeforeEach
    void setUpUserContext() {
        // 数据权限拦截的 isSuperAdmin 无条件 StpUtil.checkLogin()，测试线程需初始化 Sa-Token
        // 会话（参照 SaExtensionInterceptorTest 先例）；登录 id=1 超管并让 ThreadLocal 上下文
        // 携带超管角色，使数据权限 handler 短路不过滤
        originalSaTokenContext = SaManager.getSaTokenContext();
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());
        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(1L);
        UserContext operator = new UserContext();
        operator.setId(1L);
        operator.setUsername("admin");
        operator.setRoleCodes(Set.of(RoleCodeEnum.SUPER_ADMIN.getCode()));
        // 真实登录上下文由 AuthTokenServiceImpl 一并装入权限集合，超管持有 ALL_PERMISSION；
        // 补齐该字段，避免用户创建接口的分配角色权限校验读取到 null
        operator.setPermissions(Set.of(SystemConstants.ALL_PERMISSION));
        UserContextHolder.setContext(operator, false);
    }

    @AfterEach
    void removeProbeData() {
        StpUtil.logout(1L);
        SaTokenContextMockUtil.clearContext();
        SaManager.setSaTokenContext(originalSaTokenContext);
        UserContextHolder.clearContext();
        if (probeUsername != null) {
            // 按角色关联、用户的顺序清理探针数据
            jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id IN "
                + "(SELECT id FROM sys_user WHERE username = ?)", probeUsername);
            jdbcTemplate.update("DELETE FROM sys_user WHERE username = ?", probeUsername);
            probeUsername = null;
        }
    }

    /**
     * 创建用户撞已存在用户名时应拒绝，且不产生新行
     */
    @Test
    void createShouldRejectDuplicateUsername() {
        probeUsername = "it_dup_" + System.nanoTime();
        jdbcTemplate.update("INSERT INTO sys_user (username, nickname, gender, status, is_system, "
            + "dept_id, create_time, deleted, tenant_id) "
            + "VALUES (?, '唯一冲突探针', 0, 1, b'0', 1, NOW(), 0, 0)", probeUsername);

        UserReq req = buildUserReq(probeUsername, "唯一冲突探针");
        assertThatThrownBy(
            () -> TenantUtils.execute(DEFAULT_TENANT_ID, () -> userService.create(req)))
            .as("重复用户名应被拒绝")
            .isInstanceOf(BusinessException.class)
            .hasMessage("用户名为 [%s] 的用户已存在".formatted(probeUsername));

        Long rowCount =
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE username = ?",
                Long.class, probeUsername);
        assertThat(rowCount).as("唯一冲突不应产生新用户行").isEqualTo(1L);
    }

    /**
     * 软删除（deleted 置为自身 ID）后同名用户可重建，且两行共存
     */
    @Test
    void softDeletedUsernameShouldBeReusable() {
        probeUsername = "it_reuse_" + System.nanoTime();
        AtomicReference<Long> firstIdRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> firstIdRef
            .set(userService.create(buildUserReq(probeUsername, "软删除重建探针"))));
        Long firstId = firstIdRef.get();
        assertThat(firstId).as("首次创建应成功并返回 ID").isNotNull();

        TenantUtils.execute(DEFAULT_TENANT_ID, () -> userService.delete(List.of(firstId)));
        Long deletedFlag =
            jdbcTemplate.queryForObject("SELECT deleted FROM sys_user WHERE id = ?", Long.class,
                firstId);
        assertThat(deletedFlag).as("逻辑删除应将 deleted 置为自身 ID").isEqualTo(firstId);

        // UserMapper 走数据权限拦截，测试线程无 SaTokenContext，重建路径改由直插承载唯一索引语义
        assertThatCode(
            () -> jdbcTemplate.update("INSERT INTO sys_user (username, nickname, gender, "
                + "status, is_system, dept_id, create_time, deleted, tenant_id) "
                + "VALUES (?, '软删除重建探针', 0, 1, b'0', 1, NOW(), 0, 0)", probeUsername))
            .as("唯一索引 uk_username(username, deleted) 应允许同 username 的软删行与活跃行共存")
            .doesNotThrowAnyException();

        Long activeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_user WHERE username = ? AND deleted = 0", Long.class,
            probeUsername);
        Long deletedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_user WHERE username = ? AND deleted != 0", Long.class,
            probeUsername);
        assertThat(activeCount).as("重建后应有 1 行活跃行").isEqualTo(1L);
        assertThat(deletedCount).as("软删行应保留").isEqualTo(1L);
    }

    /**
     * update 中 updateById 已执行后角色校验失败，事务应回滚使昵称保持原值
     */
    @Test
    void updateFailureShouldRollbackUserChange() {
        probeUsername = "it_rollback_" + System.nanoTime();
        String originalNickname = "回滚探针原昵称";
        AtomicReference<Long> userIdRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> userIdRef
            .set(userService.create(buildUserReq(probeUsername, originalNickname))));
        Long userId = userIdRef.get();

        UserReq req = buildUserReq(probeUsername, "回滚探针新昵称");
        req.setRoleIds(List.of(SUPER_ADMIN_ROLE_ID));
        assertThatThrownBy(
            () -> TenantUtils.execute(DEFAULT_TENANT_ID, () -> userService.update(req, userId)))
            .as("分配超级管理员角色应被拒绝")
            .isInstanceOf(BusinessException.class)
            .hasMessage("不允许分配超级管理员角色");

        String nickname =
            jdbcTemplate.queryForObject("SELECT nickname FROM sys_user WHERE id = ?", String.class,
                userId);
        assertThat(nickname).as("事务回滚后昵称应保持原值").isEqualTo(originalNickname);
    }

    /**
     * 构造合法创建请求（密码经 RSA 公钥加密，角色为初始普通用户角色）
     *
     * @param username 用户名
     * @param nickname 昵称
     * @return 创建请求
     */
    private UserReq buildUserReq(String username, String nickname) {
        UserReq req = new UserReq();
        req.setUsername(username);
        req.setNickname(nickname);
        req.setPassword(rsaEncryptBase64(RAW_PASSWORD));
        req.setGender(GenderEnum.MALE);
        req.setDeptId(1L);
        req.setRoleIds(List.of(GENERAL_ROLE_ID));
        return req;
    }

    /**
     * 以 Base64 输出 RSA 公钥加密结果，与前端加密行为对齐，确保服务端解密链路可逆
     *
     * @param raw 明文
     * @return Base64 编码的 RSA 密文
     */
    private static String rsaEncryptBase64(String raw) {
        return Base64.encode(SecureUtil.rsa(null, RsaProperties.PUBLIC_KEY)
            .encrypt(raw, KeyType.PublicKey));
    }
}
