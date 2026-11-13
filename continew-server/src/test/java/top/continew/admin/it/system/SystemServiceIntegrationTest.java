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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.mapper.user.UserMapper;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.query.DictQuery;
import top.continew.admin.system.model.resp.DictResp;
import top.continew.admin.system.service.DictService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 系统模块 Service 契约集成测试
 *
 * <p>
 * 走真实库验证系统模块核心契约：初始数据按用户名可查、字典分页查询结构完整、加密
 * 字段经 ORM 写入后库中为密文且应用内可解密读回（往返一致）。租户行级隔离在无 Web
 * 请求上下文的测试线程中同样生效，查询与写入统一置于默认租户 0 上下文执行。
 * </p>
 * <p>
 * 用户分页经 DataPermissionMapper 附带数据权限过滤，依赖 Sa-Token 请求上下文，不适合
 * 无请求线程的集成测试，分页契约以无数据权限的字典 Mapper 为代表。测试自建数据按
 * username 唯一命名并在用例后清理，容器销毁亦不残留。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class SystemServiceIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;

    @Autowired
    private UserService userService;

    @Autowired
    private DictService dictService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String probeUsername;

    @AfterEach
    void removeProbeData() {
        if (probeUsername == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM sys_user WHERE username = ?", probeUsername);
        probeUsername = null;
    }

    /**
     * 初始数据管理员按用户名可查，主键与昵称非空
     */
    @Test
    void getByUsernameShouldReturnSeededAdmin() {
        AtomicReference<UserDO> adminRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID,
            () -> adminRef.set(userService.getByUsername("admin")));

        UserDO admin = adminRef.get();
        assertThat(admin).as("初始数据应包含 admin 用户").isNotNull();
        assertThat(admin.getId()).isNotNull();
        assertThat(admin.getNickname()).isNotBlank();
    }

    /**
     * 字典分页查询返回结构完整且包含初始数据
     */
    @Test
    void dictPageShouldReturnPagedDictionaries() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(1);
        pageQuery.setSize(10);

        AtomicReference<BasePageResp<DictResp>> pageRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID,
            () -> pageRef.set(dictService.page(new DictQuery(), pageQuery)));

        BasePageResp<DictResp> pageResp = pageRef.get();
        assertThat(pageResp).isNotNull();
        assertThat(pageResp.getTotal()).as("初始数据应包含字典").isGreaterThanOrEqualTo(1L);
        assertThat(pageResp.getList()).isNotEmpty();
    }

    /**
     * 加密字段经 ORM 写入后库中为密文，应用内读回与原值一致
     */
    @Test
    void fieldEncryptShouldStoreCipherAndRoundtrip() {
        probeUsername = "it_user_" + System.nanoTime();
        String rawPhone = "1380000" + String.format("%04d", System.nanoTime() % 10000);

        AtomicReference<String> dbPhoneRef = new AtomicReference<>();
        AtomicReference<String> ormPhoneRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> {
            UserDO user = new UserDO();
            user.setUsername(probeUsername);
            user.setNickname("加密探针");
            user.setPhone(rawPhone);
            user.setDeptId(1L);
            userMapper.insert(user);

            // 库中密文校验与 ORM 解密读回在同一租户上下文内完成
            dbPhoneRef.set(jdbcTemplate.queryForObject(
                "SELECT phone FROM sys_user WHERE username = ?", String.class, probeUsername));
            ormPhoneRef.set(userService.getByUsername(probeUsername).getPhone());
        });

        assertThat(dbPhoneRef.get()).as("库中 phone 应为密文而非明文").isNotEqualTo(rawPhone);
        assertThat(ormPhoneRef.get()).as("ORM 读回应解密回原值").isEqualTo(rawPhone);
    }
}
