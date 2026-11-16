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
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.enums.MenuTypeEnum;
import top.continew.admin.system.model.req.MenuReq;
import top.continew.admin.system.service.MenuService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单 Service 集成测试（L2）
 *
 * <p>
 * 聚焦黑盒接口覆盖不到的服务层点：删除时递归级联移除全部子菜单（服务层内部递归收集，
 * 接口层只能看到单条 ID 入参）与 ROLE_MENU 缓存的整簇失效（evictAll）。菜单树缓存
 * 按角色 ID 粒度存储，只能经缓存工具直接观测。
 * </p>
 * <p>
 * 自建菜单按 name 唯一命名（it 前缀）并在用例后物理清理，不触碰初始菜单数据。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class MenuServiceIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final String PROBE_MENU_TITLE_PREFIX = "IT集成菜单";

    private final List<Long> probeMenuIds = new ArrayList<>();

    @Autowired
    private MenuService menuService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void removeProbeData() {
        // 按 id 清理：update 用例会改写探针行 title 与 name，按前缀匹配会脱靶
        for (Long menuId : probeMenuIds) {
            jdbcTemplate.update("DELETE FROM sys_menu WHERE id = ?", menuId);
        }
        probeMenuIds.clear();
    }

    /**
     * 删除父菜单应级联逻辑删除全部子菜单，并失效 ROLE_MENU 缓存
     */
    @Test
    void deleteShouldCascadeChildrenAndEvictCache() {
        Long parentId =
            insertProbeMenu("父目录", "ItCascadeParent" + System.nanoTime(), 0L, MenuTypeEnum.DIR);
        Long childId = insertProbeMenu("子目录", "ItCascadeChild" + System.nanoTime(), parentId,
            MenuTypeEnum.DIR);
        CacheUtils.put(CacheConstants.ROLE_MENU_KEY_PREFIX, "it_cascade_probe_key", "stub");

        TenantUtils.execute(DEFAULT_TENANT_ID, () -> menuService.delete(List.of(parentId)));

        assertThat(jdbcTemplate.queryForObject("SELECT deleted FROM sys_menu WHERE id = ?",
            Long.class, parentId))
            .as("父菜单应被逻辑删除（deleted 置为自身 ID）")
            .isEqualTo(parentId);
        assertThat(jdbcTemplate.queryForObject("SELECT deleted FROM sys_menu WHERE id = ?",
            Long.class, childId))
            .as("子菜单应被级联逻辑删除")
            .isEqualTo(childId);
        Object cachedAfterDelete =
            CacheUtils.get(CacheConstants.ROLE_MENU_KEY_PREFIX, "it_cascade_probe_key");
        assertThat(cachedAfterDelete).as("删除菜单后 ROLE_MENU 缓存应整簇失效").isNull();
    }

    /**
     * 更新菜单应生效并失效 ROLE_MENU 缓存
     */
    @Test
    void updateShouldEvictRoleMenuCache() {
        Long menuId =
            insertProbeMenu("待更新菜单", "ItUpdateMenu" + System.nanoTime(), 0L, MenuTypeEnum.MENU);
        CacheUtils.put(CacheConstants.ROLE_MENU_KEY_PREFIX, "it_update_probe_key", "stub");

        MenuReq req = new MenuReq();
        req.setType(MenuTypeEnum.MENU);
        req.setTitle("更新后菜单");
        req.setName("ItUpdateMenuRenamed" + System.nanoTime());
        req.setParentId(0L);
        req.setSort(2);
        req.setStatus(DisEnableStatusEnum.ENABLE);
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> menuService.update(req, menuId));

        String title = jdbcTemplate.queryForObject("SELECT title FROM sys_menu WHERE id = ?",
            String.class, menuId);
        assertThat(title).as("菜单标题应更新为新值").isEqualTo("更新后菜单");
        Object cachedAfterUpdate =
            CacheUtils.get(CacheConstants.ROLE_MENU_KEY_PREFIX, "it_update_probe_key");
        assertThat(cachedAfterUpdate).as("更新菜单后 ROLE_MENU 缓存应整簇失效").isNull();
    }

    /**
     * 插入自建菜单探针行
     *
     * @param title    标题
     * @param name     组件名称（唯一）
     * @param parentId 上级菜单 ID
     * @param type     菜单类型
     * @return 菜单 ID
     */
    private Long insertProbeMenu(String title, String name, Long parentId, MenuTypeEnum type) {
        jdbcTemplate.update(
            "INSERT INTO sys_menu (title, parent_id, type, path, name, sort, status, "
                + "create_user, create_time, deleted) "
                + "VALUES (?, ?, ?, NULL, ?, 999, 1, 1, NOW(), 0)",
            PROBE_MENU_TITLE_PREFIX + title, parentId, type.getValue(), name);
        Long menuId =
            jdbcTemplate.queryForObject("SELECT id FROM sys_menu WHERE name = ?", Long.class, name);
        probeMenuIds.add(menuId);
        return menuId;
    }
}
