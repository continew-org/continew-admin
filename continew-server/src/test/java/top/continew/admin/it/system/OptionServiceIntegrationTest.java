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
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.enums.OptionCategoryEnum;
import top.continew.admin.system.model.req.OptionReq;
import top.continew.admin.system.service.OptionService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 参数 Service 集成测试（L2）
 *
 * <p>
 * 聚焦黑盒接口覆盖不到的服务层点：getValueByCode 的缓存未命中回源（value 为空时回落
 * defaultValue 并写回 Redis）、update 后三处参数缓存（Redis 参数键、公共列表、分类
 * Map）同步失效并回源新值。缓存分布与失效链路是服务层内部实现，HTTP 层不可观测。
 * </p>
 * <p>
 * 只修改初始参数 SITE_TITLE 的 value 并在用例后恢复为 NULL 与清空参数缓存，不影响其他
 * 初始数据。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class OptionServiceIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long SITE_TITLE_OPTION_ID = 1L;
    private static final String SITE_TITLE_CODE = "SITE_TITLE";
    private static final String SEEDED_SITE_TITLE = "ContiNew Admin";
    private static final String UPDATED_SITE_TITLE = "IT集成测试站名";

    @Autowired
    private OptionService optionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void restoreOption() {
        jdbcTemplate.update("UPDATE sys_option SET value = NULL WHERE id = ?",
            SITE_TITLE_OPTION_ID);
        CacheUtils.evictAll(CacheConstants.OPTION_KEY_PREFIX + "MAP:");
        RedisUtils.deleteByPattern(CacheConstants.OPTION_KEY_PREFIX + "*");
    }

    /**
     * getValueByCode 未命中 Redis 时应回源 DB，value 为空回落 defaultValue 并写回 Redis
     */
    @Test
    void getValueByCodeShouldFallBackToDefaultAndCacheInRedis() {
        String redisKey = CacheConstants.OPTION_KEY_PREFIX + SITE_TITLE_CODE;
        RedisUtils.delete(redisKey);

        AtomicReference<String> valueRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> valueRef.set(optionService
            .getValueByCode(SITE_TITLE_CODE, Function.identity())));

        assertThat(valueRef.get()).as("value 为空应回落到初始默认值").isEqualTo(SEEDED_SITE_TITLE);
        assertThat(RedisUtils.<String>get(redisKey)).as("回源后应写回 Redis 供后续命中")
            .isEqualTo(SEEDED_SITE_TITLE);
    }

    /**
     * update 后分类参数缓存应失效，再次读取回源新值，Redis 参数键同步清理
     */
    @Test
    void updateShouldEvictCategoryCacheAndReload() {
        AtomicReference<Map<String, String>> beforeRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> beforeRef.set(optionService
            .getByCategory(OptionCategoryEnum.SITE)));
        assertThat(beforeRef.get()).as("预热读取应返回初始默认值")
            .containsEntry(SITE_TITLE_CODE, SEEDED_SITE_TITLE);

        OptionReq req = new OptionReq();
        req.setId(SITE_TITLE_OPTION_ID);
        req.setCode(SITE_TITLE_CODE);
        req.setValue(UPDATED_SITE_TITLE);
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> optionService.update(List.of(req)));

        Object cachedCategoryMap =
            CacheUtils.get(CacheConstants.OPTION_KEY_PREFIX + "MAP:", OptionCategoryEnum.SITE
                .name());
        assertThat(cachedCategoryMap).as("update 后分类参数缓存应失效").isNull();
        assertThat(RedisUtils.<String>get(CacheConstants.OPTION_KEY_PREFIX + SITE_TITLE_CODE))
            .as("update 后 Redis 参数键应被清理")
            .isNull();

        AtomicReference<Map<String, String>> afterRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> afterRef.set(optionService
            .getByCategory(OptionCategoryEnum.SITE)));
        assertThat(afterRef.get()).as("再次读取应回源到更新后的值")
            .containsEntry(SITE_TITLE_CODE, UPDATED_SITE_TITLE);
    }
}
