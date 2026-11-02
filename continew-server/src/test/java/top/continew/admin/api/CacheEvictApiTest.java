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
 * 系统配置改读缓存失效黑盒测试：修改 SITE_TITLE 后轮询读回必须出现新值，验证多级缓存
 * （Caffeine L1 + Redis L2）不残留陈旧值，finally 恢复原值并轮询读回。
 *
 * <p>
 * 端点依据：GET /system/option?code=SITE_TITLE（OptionController#list，OptionQuery.code），
 * PUT /system/option（OptionController#update，请求体为 List&lt;OptionReq&gt;，id/code/value
 * 必传，服务端按 id 执行 updateById）。SITE_TITLE 为 sys_option 初始数据中的系统名称配置项。
 * </p>
 * <p>
 * 本用例会临时修改全局系统配置，只在独立测试实例上运行（up.sh 起的 compose 环境），
 * 禁止指向共享环境。恢复与验证均用 Awaitility 轮询（超时 5 秒、间隔 200 毫秒），禁裸 sleep。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class CacheEvictApiTest extends ApiTestSupport {

    /**
     * 系统配置查询端点。
     */
    private static final String OPTION_PATH = "/system/option";

    /**
     * 系统名称配置项编码（sys_option 初始数据）。
     */
    private static final String SITE_TITLE_CODE = "SITE_TITLE";

    /**
     * 读基线 → 预热两次 → 改为冒烟新值 → 轮询读回新值 → finally 恢复原值并轮询读回。
     */
    @Test
    void siteTitleUpdateVisibleAfterCacheEvictAndRestored() {
        // 1. 读基线，记录原值与配置项 id
        Map<?, ?> baseline = findSiteTitleOption();
        String originalValue = String.valueOf(baseline.get("value"));
        assertThat(originalValue).as("SITE_TITLE 基线值不应为空").isNotBlank();
        Object optionId = baseline.get("id");
        assertThat(optionId).as("SITE_TITLE 配置项应携带 id").isNotNull();
        try {
            // 2. 连续读两次预热缓存（覆盖 L1 命中路径）
            assertThat(readSiteTitleValue()).isEqualTo(originalValue);
            assertThat(readSiteTitleValue()).isEqualTo(originalValue);
            // 3. 修改为冒烟新值
            String smokeValue = "QA-SMOKE-TITLE-" + System.currentTimeMillis();
            updateSiteTitle(optionId, smokeValue);
            // 4. 轮询读回必须出现新值，验证 L1/L2 无陈旧
            awaitConsistency(() -> assertThat(readSiteTitleValue())
                .as("修改后读回应为新值（无缓存陈旧）")
                .isEqualTo(smokeValue));
        } finally {
            // 5. 恢复原值并轮询读回；恢复失败只告警，不吞主断言
            try {
                updateSiteTitle(optionId, originalValue);
                awaitConsistency(() -> assertThat(readSiteTitleValue())
                    .as("恢复后读回应为原值")
                    .isEqualTo(originalValue));
            } catch (Exception e) {
                log.warn("L3 恢复 SITE_TITLE 原值失败，请检查测试实例配置：{}", e.getMessage());
            }
        }
    }

    /**
     * 查询 SITE_TITLE 配置项（返回 data 数组取首元素）。
     *
     * @return 配置项 Map（id/name/code/value/defaultValue/description）
     */
    private Map<?, ?> findSiteTitleOption() {
        List<Map<?, ?>> options = listSiteTitleOptions();
        assertThat(options).as("SITE_TITLE 配置项应存在").isNotEmpty();
        return options.get(0);
    }

    /**
     * 读取当前 SITE_TITLE 生效值。
     *
     * @return 配置值
     */
    private String readSiteTitleValue() {
        List<Map<?, ?>> options = listSiteTitleOptions();
        assertThat(options).as("SITE_TITLE 配置项应存在").isNotEmpty();
        return String.valueOf(options.get(0).get("value"));
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
