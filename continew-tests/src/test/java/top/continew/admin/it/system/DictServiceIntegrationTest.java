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
import top.continew.admin.system.model.req.DictReq;
import top.continew.admin.system.service.DictService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.List;

/**
 * 字典 Service 集成测试（L2）
 *
 * <p>
 * 聚焦黑盒接口覆盖不到的服务层点：编码唯一校验（beforeCreate 钩子在写入前抛出，无
 * 脏数据残留）、删除字典时级联逻辑删除全部字典项并失效 DICT 缓存。字典项级联与缓存
 * 清理均为服务层内部副作用，HTTP 响应不可观测。
 * </p>
 * <p>
 * 自建字典按 code 唯一命名（it_dict_ 前缀）并在用例后物理清理。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class DictServiceIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final String PROBE_DICT_CODE = "it_dict_" + System.nanoTime();

    @Autowired
    private DictService dictService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void removeProbeData() {
        jdbcTemplate.update("DELETE FROM sys_dict_item WHERE dict_id IN "
            + "(SELECT id FROM sys_dict WHERE code = ?)", PROBE_DICT_CODE);
        jdbcTemplate.update("DELETE FROM sys_dict WHERE code = ?", PROBE_DICT_CODE);
    }

    /**
     * 创建字典撞已存在编码时应拒绝，且不产生新行
     */
    @Test
    void createShouldRejectDuplicateCode() {
        insertProbeDict();

        DictReq req = new DictReq();
        req.setName("IT集成字典重复" + System.nanoTime());
        req.setCode(PROBE_DICT_CODE);
        assertThatThrownBy(
            () -> TenantUtils.execute(DEFAULT_TENANT_ID, () -> dictService.create(req)))
            .as("重复编码应被拒绝")
            .isInstanceOf(BusinessException.class)
            .hasMessage("编码为 [%s] 的字典已存在".formatted(PROBE_DICT_CODE));

        Long rowCount =
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_dict WHERE code = ?", Long.class,
                PROBE_DICT_CODE);
        assertThat(rowCount).as("唯一冲突不应产生新字典行").isEqualTo(1L);
    }

    /**
     * 删除字典应级联逻辑删除其全部字典项，并失效 DICT 缓存
     */
    @Test
    void deleteShouldCascadeItemsAndEvictCache() {
        Long dictId = insertProbeDict();
        Long itemId = insertProbeDictItem(dictId);
        CacheUtils.put(CacheConstants.DICT_KEY_PREFIX, PROBE_DICT_CODE, List.of("stub"));

        TenantUtils.execute(DEFAULT_TENANT_ID, () -> dictService.delete(List.of(dictId)));

        assertThat(jdbcTemplate.queryForObject("SELECT deleted FROM sys_dict WHERE id = ?",
            Long.class, dictId))
            .as("字典应被逻辑删除（deleted 置为自身 ID）")
            .isEqualTo(dictId);
        assertThat(jdbcTemplate.queryForObject("SELECT deleted FROM sys_dict_item WHERE id = ?",
            Long.class,
            itemId)).as("字典项应被级联逻辑删除").isEqualTo(itemId);
        Object cachedDict = CacheUtils.get(CacheConstants.DICT_KEY_PREFIX, PROBE_DICT_CODE);
        assertThat(cachedDict).as("删除字典后 DICT 缓存应失效").isNull();
    }

    /**
     * 插入自建字典探针行（非内置）
     *
     * @return 字典 ID
     */
    private Long insertProbeDict() {
        jdbcTemplate.update(
            "INSERT INTO sys_dict (name, code, is_system, create_user, create_time, deleted) "
                + "VALUES (?, ?, b'0', 1, NOW(), 0)",
            "IT集成字典" + System.nanoTime(), PROBE_DICT_CODE);
        return jdbcTemplate.queryForObject("SELECT id FROM sys_dict WHERE code = ?", Long.class,
            PROBE_DICT_CODE);
    }

    /**
     * 插入自建字典项探针行
     *
     * @param dictId 字典 ID
     * @return 字典项 ID
     */
    private Long insertProbeDictItem(Long dictId) {
        String itemValue = "it_item_" + System.nanoTime();
        jdbcTemplate.update(
            "INSERT INTO sys_dict_item (label, value, sort, status, dict_id, create_user, "
                + "create_time, deleted) VALUES ('IT集成字典项', ?, 999, 1, ?, 1, NOW(), 0)",
            itemValue, dictId);
        return jdbcTemplate.queryForObject("SELECT id FROM sys_dict_item WHERE value = ?",
            Long.class, itemValue);
    }
}
