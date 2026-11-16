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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 多租户行级隔离集成测试
 *
 * <p>
 * 验证 LINE 隔离语义：租户上下文内的 Mapper 查询自动拼接 tenant_id 条件，租户 A 写入的
 * 数据对租户 B 不可见，默认租户 0 行为不变，忽略租户上下文（executeIgnore）可跨租户访问。
 * 隔离由 MyBatis-Plus TenantLineInnerInterceptor 在 SQL 层实现，探针行经 JdbcTemplate
 * 直插（绕过 ORM），确保可见性差异只能来自租户拦截器。
 * </p>
 * <p>
 * 探针行按 username 唯一命名并在用例后物理清理，容器销毁亦不残留。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class TenantIsolationIntegrationTest extends BaseIntegrationTest {

    private static final Long PROBE_TENANT_ID = 999L;
    private static final Long DEFAULT_TENANT_ID = 0L;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String probeUsername;

    @BeforeEach
    void insertProbeData() {
        // 探针行直插 sys_user（tenant_id 列由租户插件 changelog 补充），绕过 ORM 与租户拦截
        probeUsername = "tenant_probe_" + System.nanoTime();
        jdbcTemplate.update("INSERT INTO sys_user (username, nickname, gender, status, is_system, "
            + "dept_id, create_time, deleted, tenant_id) VALUES (?, '租户探针', 0, 1, b'0', 1, NOW(), 0, ?)",
            probeUsername, PROBE_TENANT_ID);
    }

    @AfterEach
    void removeProbeData() {
        jdbcTemplate.update("DELETE FROM sys_user WHERE username = ?", probeUsername);
    }

    /**
     * 租户 999 可见探针行，默认租户 0 不可见
     */
    @Test
    void tenantDataShouldBeInvisibleToOtherTenant() {
        AtomicReference<Long> countInProbeTenant = new AtomicReference<>();
        TenantUtils.execute(PROBE_TENANT_ID,
            () -> countInProbeTenant.set(countByUsername(probeUsername)));
        assertThat(countInProbeTenant.get()).as("租户 999 上下文应可见自建探针行").isEqualTo(1L);

        AtomicReference<Long> countInDefaultTenant = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID,
            () -> countInDefaultTenant.set(countByUsername(probeUsername)));
        assertThat(countInDefaultTenant.get()).as("默认租户 0 不应看到租户 999 的数据").isZero();
    }

    /**
     * 忽略租户上下文时跨租户可见（executeIgnore 语义）
     */
    @Test
    void ignoreTenantContextShouldSeeAllTenants() {
        AtomicReference<Long> countIgnored = new AtomicReference<>();
        TenantUtils.executeIgnore(() -> countIgnored.set(countByUsername(probeUsername)));
        assertThat(countIgnored.get()).as("忽略租户上下文应可见探针行").isEqualTo(1L);
    }

    /**
     * 统计当前租户上下文下指定用户名的行数
     *
     * @param username 用户名
     * @return 行数
     */
    private Long countByUsername(String username) {
        return userMapper
            .selectCount(new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, username));
    }
}
