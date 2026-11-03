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

package top.continew.admin.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import top.continew.admin.api.support.ApiTestSupport;

import java.util.List;
import java.util.Map;

/**
 * 系统配置改读缓存失效黑盒测试：修改 SITE_TITLE 后经带缓存的公共字典端点轮询读回，
 * 必须出现新值，验证多级缓存（Caffeine L1 + Redis L2）不残留陈旧值，
 * finally 恢复原值并轮询读回。
 *
 * <p>
 * 端点依据：读走 GET /system/common/dict/option/site（CommonController#listSiteOptionDict，
 * 标注 @Cacheable(cacheNames="OPTION:", key="'SITE'") 且 @SaIgnore 免认证），label 为
 * 配置项编码、value 为生效值；这是前端登录页实际消费的站点配置读路径，缓存清理缺失
 * 时该端点会持续返回旧值。写走 PUT /system/option（OptionController#update，请求体为
 * List&lt;OptionReq&gt;，id/code/value 必传），更新侧经 OptionServiceImpl#clearCache 对
 * cacheName "OPTION:" 执行 evictAll（含 L1 失效广播）与 Redis L2 清理。配置项 id 经
 * GET /system/option?code=SITE_TITLE（管理端直查库）获取。
 * </p>
 * <p>
 * 本用例会临时修改全局系统配置，只在独立测试实例上运行（up.sh 起的 compose 环境），
 * 禁止指向共享环境。恢复与验证均用 Awaitility 轮询（超时 5 秒、间隔 200 毫秒），禁裸 sleep。
 * 单实例用例只验证本进程 L1 与 L2 的失效，不宣称覆盖跨 JVM 的 L1 广播失效。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class CacheEvictApiTest extends ApiTestSupport {

    /**
     * 系统配置查询端点（管理端，直查库）。
     */
    private static final String OPTION_PATH = "/system/option";

    /**
     * 站点配置公共字典端点（带 @Cacheable 的真实读路径）。
     */
    private static final String SITE_DICT_PATH = "/system/common/dict/option/site";

    /**
     * 系统名称配置项编码（sys_option 初始数据）。
     */
    private static final String SITE_TITLE_CODE = "SITE_TITLE";

    /**
     * 读基线 → 预热两次 → 改为冒烟新值 → 轮询读回新值 → finally 恢复原值并轮询读回。
     */
    @Test
    void siteTitleUpdateVisibleAfterCacheEvictAndRestored() {
        // 1. 管理端读基线，记录原值与配置项 id。黑盒口径说明：初始数据 value 为 NULL 时接口
        // 读取会回填 default_value，测试无法还原 DB 侧 NULL；清理目标定义为「接口可见
        // 值回到本次读到的初始可见值」，黑盒可观测即达成清理目标
        Map<?, ?> baseline = findSiteTitleOption();
        String originalValue = String.valueOf(baseline.get("value"));
        assertThat(originalValue).as("SITE_TITLE 基线值不应为空").isNotBlank();
        Object optionId = baseline.get("id");
        assertThat(optionId).as("SITE_TITLE 配置项应携带 id").isNotNull();
        try {
            // 2. 经缓存端点连续读两次预热（首读回源并填充 L1/L2，次读命中 L1）；
            //    缓存路径读到的值与管理端基线应一致
            assertThat(readSiteTitleValueFromCachePath()).isEqualTo(originalValue);
            assertThat(readSiteTitleValueFromCachePath()).isEqualTo(originalValue);
            // 3. 修改为冒烟新值
            String smokeValue = "QA-SMOKE-TITLE-" + System.currentTimeMillis();
            updateSiteTitle(optionId, smokeValue);
            // 4. 轮询经缓存端点读回必须出现新值，验证 L1/L2 无陈旧
            awaitConsistency(() -> assertThat(readSiteTitleValueFromCachePath())
                .as("修改后缓存端点读回应为新值（无缓存陈旧）")
                .isEqualTo(smokeValue));
        } finally {
            // 5. 恢复原值并轮询读回；恢复失败只告警，不吞主断言
            try {
                updateSiteTitle(optionId, originalValue);
                awaitConsistency(() -> assertThat(readSiteTitleValueFromCachePath())
                    .as("恢复后缓存端点读回应为原值")
                    .isEqualTo(originalValue));
            } catch (Exception e) {
                log.warn("L3 恢复 SITE_TITLE 原值失败，请检查测试实例配置：{}", e.getMessage());
            }
        }
    }

    /**
     * 查询 SITE_TITLE 配置项（管理端直查库，返回 data 数组取首元素）。
     *
     * @return 配置项 Map（id/name/code/value/defaultValue/description）
     */
    private Map<?, ?> findSiteTitleOption() {
        List<Map<?, ?>> options = listSiteTitleOptions();
        assertThat(options).as("SITE_TITLE 配置项应存在").isNotEmpty();
        return options.get(0);
    }

    /**
     * 经带 @Cacheable 的站点字典端点读取 SITE_TITLE 生效值（前端登录页的实际读路径）。
     *
     * @return 配置值
     */
    private String readSiteTitleValueFromCachePath() {
        Response response = given().get(SITE_DICT_PATH);
        assertBizOk(response);
        List<Map<?, ?>> items = response.jsonPath().getList("data");
        assertThat(items).as("站点配置字典应非空").isNotEmpty();
        return items.stream()
            .filter(item -> SITE_TITLE_CODE.equals(String.valueOf(item.get("label"))))
            .map(item -> String.valueOf(item.get("value")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("站点配置字典中未找到 " + SITE_TITLE_CODE));
    }

    /**
     * 请求 SITE_TITLE 配置列表（带业务断言）。
     *
     * @return 配置项列表
     */
    private List<Map<?, ?>> listSiteTitleOptions() {
        Response response = givenAuth().queryParam("code", SITE_TITLE_CODE).get(OPTION_PATH);
        assertBizOk(response);
        return response.jsonPath().getList("data");
    }

    /**
     * 修改 SITE_TITLE（PUT /system/option，请求体为单元素数组，按 id 更新）。
     *
     * @param optionId
     *            配置项 id
     * @param value
     *            新值
     */
    private void updateSiteTitle(Object optionId, String value) {
        Map<String, Object> option =
            Map.of("id", optionId, "code", SITE_TITLE_CODE, "value", value);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(List.of(option))
            .put(OPTION_PATH);
        assertBizOk(response);
    }
}
