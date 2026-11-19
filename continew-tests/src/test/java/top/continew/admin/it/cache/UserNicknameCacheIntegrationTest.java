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

package top.continew.admin.it.cache;

import static org.assertj.core.api.Assertions.assertThat;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.common.api.system.UserApi;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.service.UserService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 用户昵称缓存失效集成测试
 *
 * <p>
 * 直查缓存内部态验证 USER 缓存链路：getNicknameById 经 @Cacheable 未命中回源 DB 并写回
 * 缓存、删除用户后逐个 evict 并同步失效 Redis 侧缓存键。缓存键的写入与清理均是服务层
 * 内部副作用，HTTP 返回值只能看到昵称文本，观测不到键的存在与消失。
 * </p>
 * <p>
 * 沿用既有测试惯例：探针用户按 username 唯一命名（qa_ 前缀 + nanoTime）并物理清理；evict
 * 的 Redis 端清理经异步缓存写入器执行，以 Awaitility 终态轮询等待，禁裸 sleep。删除路径
 * 处于种子超管登录态（防自删校验读取当前用户 ID）。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class UserNicknameCacheIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long SEEDED_ADMIN_ID = 1L;
    private static final String PROBE_USERNAME = "qa_nickcache_it_" + System.nanoTime();

    @Autowired
    private UserApi userApi;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<Long> probeUserIds = new ArrayList<>();

    private SaTokenContext originalSaTokenContext;

    @BeforeEach
    void setUpUserContext() {
        // delete 的防自删校验读取当前用户 ID，登录态缺失会 NPE；登录种子超管
        originalSaTokenContext = SaManager.getSaTokenContext();
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());
        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(SEEDED_ADMIN_ID);
        UserContext operator = new UserContext();
        operator.setId(SEEDED_ADMIN_ID);
        operator.setUsername("admin");
        operator.setRoleCodes(Set.of("super_admin"));
        UserContextHolder.setContext(operator, false);
    }

    @AfterEach
    void removeProbeData() {
        StpUtil.logout(SEEDED_ADMIN_ID);
        SaTokenContextMockUtil.clearContext();
        SaManager.setSaTokenContext(originalSaTokenContext);
        UserContextHolder.clearContext();
        for (Long probeUserId : probeUserIds) {
            jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", probeUserId);
            jdbcTemplate.update("DELETE FROM sys_user WHERE id = ?", probeUserId);
        }
        probeUserIds.clear();
        CacheUtils.evict(CacheConstants.USER_KEY_PREFIX, SEEDED_ADMIN_ID);
    }

    /**
     * 昵称读取未命中应回源 DB 并写回缓存：统一入口与 Redis 键均出现该昵称
     */
    @Test
    void getNicknameByIdShouldLoadThroughAndLandInCache() {
        // 同 JVM 前序用例可能已预热该键，先失效并等 Redis 端清理落定，确保从回源路径进入
        CacheUtils.evict(CacheConstants.USER_KEY_PREFIX, SEEDED_ADMIN_ID);
        Awaitility.await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(200))
            .untilAsserted(() -> assertThat(redisKeysEndingWith(SEEDED_ADMIN_ID))
                .as("前置失效后 Redis 键应清空").isEmpty());

        AtomicReference<String> nicknameRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> nicknameRef.set(userApi
            .getNicknameById(SEEDED_ADMIN_ID)));
        assertThat(nicknameRef.get()).as("回源读取应返回初始管理员昵称").isNotBlank();

        assertThat(CacheUtils.<String>get(CacheConstants.USER_KEY_PREFIX, SEEDED_ADMIN_ID))
            .as("回源后统一入口应命中缓存").isEqualTo(nicknameRef.get());
        // 回填的 Redis 端写入以终态轮询等待其完成
        Awaitility.await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(200))
            .untilAsserted(() -> assertThat(redisKeysEndingWith(SEEDED_ADMIN_ID))
                .as("回源后 Redis 应出现对应键").isNotEmpty());
    }

    /**
     * 查询 Redis 中以缓存名前缀开头、以指定 ID 结尾的缓存键（键格式为缓存名 + '::' + 键）
     *
     * @param id 缓存键 ID
     * @return 匹配的键集合
     */
    private Set<String> redisKeysEndingWith(Long id) {
        Set<String> keys = stringRedisTemplate.keys(CacheConstants.USER_KEY_PREFIX + "*");
        return keys == null ? Set.of() : keys.stream()
            .filter(key -> key.endsWith(":" + id))
            .collect(Collectors.toSet());
    }

    /**
     * 批量删除探针用户应逐个失效 USER 缓存，各 Redis 键在终态消失
     */
    @Test
    void deleteUserShouldEvictNicknameCache() {
        // 两个探针覆盖 userIds.forEach 逐个失效的多 ID 路径，避免单元素调用空过循环边界
        Long firstId = insertProbeUser(PROBE_USERNAME);
        Long secondId = insertProbeUser(PROBE_USERNAME + "_2");
        probeUserIds.add(firstId);
        probeUserIds.add(secondId);
        List<Long> probeIds = List.of(firstId, secondId);
        for (Long probeId : probeIds) {
            CacheUtils.put(CacheConstants.USER_KEY_PREFIX, probeId,
                "qa_nick_probe_" + System.nanoTime());
            // 先确认 put 在统一入口与 Redis 两侧落定，否则后续 evict 断言会空洞通过
            assertThat(CacheUtils.<String>get(CacheConstants.USER_KEY_PREFIX, probeId))
                .as("预置缓存后统一入口应命中").isNotNull();
            // put 的 Redis 端写入经异步缓存写入器，先等键落库再删除，避免与 evict 的清理乱序
            Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> assertThat(redisKeysEndingWith(probeId))
                    .as("put 后 Redis 应出现对应键").isNotEmpty());
        }

        TenantUtils.execute(DEFAULT_TENANT_ID, () -> userService.delete(probeIds));

        for (Long probeId : probeIds) {
            assertThat(CacheUtils.<String>get(CacheConstants.USER_KEY_PREFIX, probeId))
                .as("删除用户后统一入口不应命中缓存").isNull();
            // evict 的 Redis 端键清理以终态轮询等待其完成
            Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> assertThat(redisKeysEndingWith(probeId))
                    .as("删除用户后 Redis 键应消失").isEmpty());
        }
    }

    /**
     * 直插探针用户行
     *
     * @param username 用户名
     * @return 用户 ID
     */
    private Long insertProbeUser(String username) {
        jdbcTemplate.update(
            "INSERT INTO sys_user (username, nickname, gender, status, is_system, dept_id, "
                + "create_user, create_time, deleted, tenant_id) "
                + "VALUES (?, 'qa_nick_probe_', 0, 1, b'0', 1, 1, NOW(), 0, 0)",
            username);
        return jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = ?", Long.class,
            username);
    }
}
