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

import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import top.continew.admin.api.support.ApiTestSupport;

import java.util.stream.Stream;

/**
 * 核心只读面回归测试：系统管理列表端点与仪表盘端点在超管视角下的最小契约。
 *
 * <p>
 * 端点与契约依据（评审 S-3：不只断言业务码，每个端点至少一条结构断言）：
 * 分页端点断言 data.list 为数组（BasePageResp：list + total），树/列表端点断言 data 为
 * 数组结构（BaseController CRUD 套件 TREE/LIST 返回 List），仪表盘对象端点断言 data 为对象。
 * 各端点路径以对应 Controller 源码为准（CrudRequestMapping 的 Api 枚举决定 /page、/list、
 * /tree 后缀，DashboardController 显式声明）。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Tag("api")
class CoreReadApiTest extends ApiTestSupport {

    /**
     * 响应 data 结构分类。
     */
    enum DataShape {
        /**
         * 分页结构：data.list 为数组且 data.total 非负。
         */
        PAGED,
        /**
         * 数组结构：data 为数组（树、列表、图表等）。
         */
        ARRAY,
        /**
         * 对象结构：data 为对象（总览等）。
         */
        OBJECT
    }

    /**
     * 系统管理 10 个只读端点：路径与期望 data 结构。
     *
     * @return 参数流
     */
    static Stream<Arguments> listEndpoints() {
        return Stream.of(Arguments.of("/system/user/page", DataShape.PAGED), Arguments
            .of("/system/dept/tree", DataShape.ARRAY),
            Arguments
                .of("/system/role/list", DataShape.ARRAY),
            Arguments
                .of("/system/menu/tree", DataShape.ARRAY),
            Arguments
                .of("/system/dict/list", DataShape.ARRAY),
            Arguments
                .of("/system/option", DataShape.ARRAY),
            Arguments
                .of("/system/notice/page", DataShape.PAGED),
            Arguments
                .of("/system/client/page", DataShape.PAGED),
            Arguments
                .of("/system/storage/list", DataShape.ARRAY),
            Arguments
                .of("/system/log", DataShape.PAGED));
    }

    /**
     * 仪表盘稳定统计端点（DashboardController 显式声明的 GET 端点中，除 PV/IP 实时计数外
     * 的全部端点，共 6 个）。这些端点返回图表/公告等缓存的固定统计，两次读取可断言一致。
     *
     * @return 端点路径流
     */
    static Stream<String> dashboardEndpoints() {
        return Stream.of("/dashboard/notice", "/dashboard/analysis/geo",
            "/dashboard/analysis/timeslot", "/dashboard/analysis/module",
            "/dashboard/analysis/os", "/dashboard/analysis/browser");
    }

    /**
     * 仪表盘实时计数端点（PV/IP 总览，DashboardOverviewCommonResp.total 为 sys_log 实时统计）。
     *
     * @return 端点路径流
     */
    static Stream<String> dashboardCounterEndpoints() {
        return Stream.of("/dashboard/analysis/overview/pv", "/dashboard/analysis/overview/ip");
    }

    /**
     * 访问趋势支持的天数参数（仅 7/30 天，DashboardController 校验 days 取值）。
     *
     * @return 天数流
     */
    static Stream<Integer> accessTrendDays() {
        return Stream.of(7, 30);
    }

    /**
     * 系统管理列表端点：业务码为 0 且 data 满足分页/数组结构契约。
     *
     * @param path
     *            端点路径
     * @param shape
     *            期望 data 结构
     */
    @ParameterizedTest(name = "[{index}] GET {0}")
    @MethodSource("listEndpoints")
    void listEndpointBizOkWithStructureContract(String path, DataShape shape) {
        Response response = givenAuth().get(path);
        assertBizOk(response);
        switch (shape) {
            case PAGED -> {
                assertThat(response.jsonPath().getList("data.list"))
                    .as("%s 的 data.list 应为数组结构", path)
                    .isNotNull();
                assertThat(response.jsonPath().getLong("data.total"))
                    .as("%s 的 data.total 应为非负数", path)
                    .isGreaterThanOrEqualTo(0);
            }
            case ARRAY -> assertThat(response.jsonPath().getList("data"))
                .as("%s 的 data 应为数组结构", path)
                .isNotNull();
            case OBJECT -> assertThat(response.jsonPath().getMap("data"))
                .as("%s 的 data 应为对象结构", path)
                .isNotNull();
            default -> throw new IllegalStateException("未覆盖的 data 结构类型: " + shape);
        }
    }

    /**
     * 仪表盘稳定统计端点各读两次：业务码为 0 且两次响应 data 的规范化 JSON 一致。
     * 部分端点带 @Cacheable（多级缓存），两次读取覆盖缓存命中路径的响应一致性。
     *
     * @param path
     *            端点路径
     */
    @ParameterizedTest(name = "[{index}] GET {0} twice")
    @MethodSource("dashboardEndpoints")
    void dashboardEndpointBizOkStableAcrossReads(String path) {
        Response first = givenAuth().get(path);
        assertBizOk(first);
        Response second = givenAuth().get(path);
        assertBizOk(second);
        assertThat(canonicalJson(first.jsonPath().get("data")))
            .as("%s 连续两次读取的 data 应一致", path)
            .isEqualTo(canonicalJson(second.jsonPath().get("data")));
    }

    /**
     * 仪表盘实时计数端点（PV/IP 总览）各读两次：业务码为 0、data 为对象结构，
     * 且 total 非递减（第二次读 >= 第一次读）。
     *
     * <p>
     * 不做相等断言的原因：total 由 sys_log 实时统计（DashboardService 的总览查询），
     * 登录等行为异步写入登录日志，插入可能落在两次读取之间，幂等相等断言对实时
     * 计数不成立（171 实测低频 flaky）。非递减断言保留「计数存在、单调合理、结构
     * 完整」的契约强度，同时容忍统计来源的合法增量。
     * </p>
     *
     * @param path
     *            端点路径
     */
    @ParameterizedTest(name = "[{index}] GET {0} twice (non-decreasing)")
    @MethodSource("dashboardCounterEndpoints")
    void dashboardCounterEndpointNonDecreasingAcrossReads(String path) {
        Response first = givenAuth().get(path);
        assertBizOk(first);
        long firstTotal = first.jsonPath().getLong("data.total");
        assertThat(firstTotal).as("%s 首次读 total 应为非负数", path)
            .isGreaterThanOrEqualTo(0);
        Response second = givenAuth().get(path);
        assertBizOk(second);
        long secondTotal = second.jsonPath().getLong("data.total");
        assertThat(secondTotal).as("%s 第二次读 total 应非递减（实时计数允许合法增量）", path)
            .isGreaterThanOrEqualTo(firstTotal);
    }

    /**
     * 访问趋势端点仅支持 7/30 天（DashboardController 校验 days 取值），
     * 各读两次：业务码为 0、data 为数组结构且两次响应一致。
     *
     * @param days
     *            天数
     */
    @ParameterizedTest(name = "[{index}] GET /dashboard/access/trend/{0} twice")
    @MethodSource("accessTrendDays")
    void accessTrendBizOkWithArrayContract(Integer days) {
        String path = "/dashboard/access/trend/" + days;
        Response first = givenAuth().get(path);
        assertBizOk(first);
        Response second = givenAuth().get(path);
        assertBizOk(second);
        assertThat(first.jsonPath().getList("data"))
            .as("访问趋势 data 应为数组结构")
            .isNotNull();
        assertThat(canonicalJson(first.jsonPath().get("data")))
            .as("%s 连续两次读取的 data 应一致", path)
            .isEqualTo(canonicalJson(second.jsonPath().get("data")));
    }
}
