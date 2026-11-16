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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户完整链路黑盒测试：创建套餐 → 创建租户（指定初始管理员凭据）→ 域名解析 →
 * 租户管理员登录拉取用户列表（含租户隔离正/负断言）→ 更换域名 → 逆序清理。
 *
 * <p>
 * 端点依据：POST /tenant/package 与 /tenant/package/menu/tree（PackageController），
 * /tenant/management 的 CRUD 端点（TenantController @CrudRequestMapping），GET
 * /tenant/common/id（tenant 包 CommonController，@SaIgnore 免认证）。创建租户时
 * adminUsername 与 adminPassword 为 Create 组必填（TenantReq），密码经 RSA 公钥加密后
 * 由 TenantDataApiForSystemImpl 解密并校验格式（8-32 位，至少含小写字母与数字）。
 * 域名解析走多级缓存（TenantServiceImpl.getCachedId），更新域名触发 evictAll，
 * 断言用 Awaitility 轮询（超时 5 秒、间隔 200 毫秒），禁止裸 sleep。
 * </p>
 * <p>
 * 资源清理：finally 中按创建逆序（先租户后套餐）删除；清理前重登超管并切回租户 0，
 * 避免链路中途失败后残留租户管理员登录态导致清理无权限。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class TenantApiTest extends ApiTestSupport {

    /**
     * 套餐菜单树端点。
     */
    private static final String PACKAGE_MENU_TREE_PATH = "/tenant/package/menu/tree";

    /**
     * 套餐管理 CRUD 端点。
     */
    private static final String PACKAGE_PATH = "/tenant/package";

    /**
     * 租户管理 CRUD 端点。
     */
    private static final String TENANT_PATH = "/tenant/management";

    /**
     * 免认证的域名解析端点。
     */
    private static final String RESOLVE_PATH = "/tenant/common/id";

    /**
     * 本用例创建的套餐 ID，finally 清理使用。
     */
    private Long packageId;

    /**
     * 本用例创建的租户 ID，finally 清理使用。
     */
    private Long tenantId;

    /**
     * 套餐 → 租户 → 域名解析 → 租户管理员登录 → 更新域名的完整链路。
     */
    @Test
    void tenantLifecyclePackageTenantResolveLoginAndUpdate() {
        long timestamp = System.currentTimeMillis();
        String packageName = "QA-SMOKE-" + timestamp + "-PKG";
        // 域名需匹配 HTTP_HOST 正则（域名/IPv4/IPv6），用带多级标签的子域形态
        String domainV1 = "qa-smoke-" + timestamp + ".test.local";
        String domainV2 = "qa-smoke-2-" + timestamp + ".test.local";
        // 管理员用户名需匹配 ^[a-zA-Z][a-zA-Z0-9_]{3,63}$（以字母开头）
        String adminUsername = "qa_smoke_admin" + timestamp;
        // 密码需匹配 PASSWORD 正则：8-32 位，至少含小写字母与数字
        String adminPassword = "QaSmoke" + timestamp;
        try {
            // 1. 创建套餐（menuIds 取菜单树前 30 个 id）
            List<Long> menuIds = collectFirstThirtyMenuIds();
            packageId = createPackage(packageName, menuIds);
            assertThat(packageId).as("创建套餐应返回非空 ID").isNotNull();
            // 2. 创建租户并绑定套餐，记录租户 ID 与编码
            tenantId = createTenant(tenantName(timestamp), domainV1, packageId, adminUsername,
                adminPassword);
            assertThat(tenantId).as("创建租户应返回非空 ID").isNotNull();
            String tenantCode =
                assertBizOk(givenAuth().get(TENANT_PATH + "/" + tenantId), "data.code");
            assertThat(tenantCode).as("租户详情应返回非空编码").isNotBlank();
            // 3. 域名解析连续 3 次都等于租户 ID
            for (int i = 0; i < 3; i++) {
                assertThat(resolveTenantIdByDomain(domainV1))
                    .as("第 %d 次域名解析应等于租户 ID", i + 1)
                    .isEqualTo(tenantId);
            }
            // 4. 租户管理员登录，拉取租户用户列表（登录请求须携带租户上下文，否则按默认租户定位不到用户）
            useTenant(tenantId);
            login(adminUsername, adminPassword);
            // 列表验证用 LIST 端点（LIST 显式声明 /list 后缀；分页契约映射根路径，见 docs/development/backend/crud.mdx）。
            // 隔离断言（评审 F3）：只判「data 是数组」时空数组/混入其他租户用户也能通过，
            // 必须以用户名关键词过滤后既见自己、又不见平台 admin（跨租户混入即失败）
            // 正断言：以租户管理员自己的用户名过滤，列表可见该账号
            Response userList =
                givenAuth().queryParam("description", adminUsername).get("/system/user/list");
            assertBizOk(userList);
            List<Map<?, ?>> visibleUsers = userList.jsonPath().getList("data");
            assertThat(visibleUsers).as("租户用户列表 data 应为数组结构").isNotNull();
            assertThat(visibleUsers.stream().map(u -> String.valueOf(u.get("username"))))
                .as("租户管理员登录后应能看到自己创建的账号")
                .contains(adminUsername);
            // 负断言：description 为 LIKE 子串过滤，不可断言空列表——租户初始管理员
            // qa_smoke_admin<时间戳> 同样含 "admin" 子串。跨租户隔离的正确负断言是
            // 结果里不出现平台超管的精确用户名，混入平台用户即失败
            Response platformLeakProbe =
                givenAuth().queryParam("description", "admin").get("/system/user/list");
            assertBizOk(platformLeakProbe);
            assertThat(platformLeakProbe.jsonPath().getList("data").stream()
                .map(u -> String.valueOf(((Map<?, ?>) u).get("username"))))
                .as("过滤结果不应混入平台超管 admin（跨租户隔离）")
                .doesNotContain("admin");
            // 5. 回平台侧更新租户域名，旧域名解析为空、新域名可解析（登录前先切回默认租户，登录请求会携带当前租户上下文）
            useTenant(DEFAULT_TENANT_ID);
            loginAsDefaultAdmin();
            updateTenantDomain(tenantId, tenantName(timestamp), domainV2, packageId);
            awaitConsistency(() -> assertThat(resolveDataByDomain(domainV1))
                .as("旧域名解析结果应为空")
                .isNull());
            awaitConsistency(() -> assertThat(resolveTenantIdByDomain(domainV2))
                .as("新域名应解析为租户 ID")
                .isEqualTo(tenantId));
        } finally {
            cleanupCreatedResources();
        }
    }

    /**
     * 收集租户套餐菜单树的前 30 个 id（深度优先遍历，含父子节点）。
     *
     * @return 菜单 ID 列表
     */
    private List<Long> collectFirstThirtyMenuIds() {
        Response response = givenAuth().get(PACKAGE_MENU_TREE_PATH);
        assertBizOk(response);
        List<Long> menuIds = new ArrayList<>(30);
        // 菜单树在响应 data 字段（全局响应封装 code/msg/data），$ 是根对象会引发类型转换失败
        collectIds(response.jsonPath().getList("data"), menuIds, 30);
        assertThat(menuIds).as("租户套餐菜单树不应为空").isNotEmpty();
        return menuIds;
    }

    /**
     * 深度优先收集树节点 id，累计到上限即停。
     *
     * @param nodes
     *            当前层节点列表
     * @param ids
     *            收集结果
     * @param limit
     *            上限
     */
    private static void collectIds(List<?> nodes, List<Long> ids, int limit) {
        for (Object node : nodes) {
            if (ids.size() >= limit) {
                return;
            }
            Map<?, ?> nodeMap = (Map<?, ?>) node;
            if (nodeMap.get("key") instanceof Number numberId) { // Tree 节点 id 键名为 key（hutool 默认 idKey，isSimple=true）
                ids.add(numberId.longValue());
            }
            if (nodeMap.get("children") instanceof List<?> children) {
                collectIds(children, ids, limit);
            }
        }
    }

    /**
     * 创建套餐。
     *
     * @param name
     *            套餐名称（QA-SMOKE 前缀 + 时间戳）
     * @param menuIds
     *            关联菜单 ID 列表
     * @return 套餐 ID
     */
    private Long createPackage(String name, List<Long> menuIds) {
        Map<String, Object> body = Map.of("name", name, "menuIds", menuIds);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(PACKAGE_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 创建租户：创建时指定初始管理员凭据（TenantReq 的 Create 组必填字段），
     * 管理员密码按后端约定经 RSA 公钥加密传输。
     *
     * @param name
     *            租户名称
     * @param domain
     *            绑定域名
     * @param packageIdValue
     *            套餐 ID
     * @param adminUsername
     *            管理员用户名
     * @param adminPassword
     *            管理员明文密码
     * @return 租户 ID
     */
    private Long createTenant(String name, String domain, Long packageIdValue, String adminUsername,
        String adminPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("domain", domain);
        body.put("expireTime", "2099-12-31 08:08:08");
        body.put("packageId", packageIdValue);
        body.put("adminUsername", adminUsername);
        body.put("adminPassword", encryptByRsa(adminPassword));
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(TENANT_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 更新租户域名。更新请求必须携带 name 与 packageId（TenantReq 的 Default 组必填校验）。
     *
     * @param id
     *            租户 ID
     * @param name
     *            租户名称
     * @param newDomain
     *            新域名
     * @param packageIdValue
     *            套餐 ID
     */
    private void updateTenantDomain(Long id, String name, String newDomain, Long packageIdValue) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("domain", newDomain);
        body.put("expireTime", "2099-12-31 08:08:08");
        body.put("packageId", packageIdValue);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .put(TENANT_PATH + "/" + id);
        assertBizOk(response);
    }

    /**
     * 按域名解析租户 ID（免认证端点），断言业务码为 0 后取 data。
     *
     * @param domain
     *            域名
     * @return 租户 ID（不存在时为 null）
     */
    private Long resolveTenantIdByDomain(String domain) {
        Response response = given().queryParam("domain", domain).get(RESOLVE_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data");
    }

    /**
     * 按域名解析的原始 data（用于断言为空的场景；getLong 对 null 会返回 0L，不可用）。
     *
     * @param domain
     *            域名
     * @return 原始 data 值
     */
    private Object resolveDataByDomain(String domain) {
        Response response = given().queryParam("domain", domain).get(RESOLVE_PATH);
        assertBizOk(response);
        return response.jsonPath().get("data");
    }

    /**
     * 生成租户名称（qa_ 前缀 + 时间戳）。
     *
     * @param timestamp
     *            时间戳
     * @return 租户名称
     */
    private static String tenantName(long timestamp) {
        return "qa_smoke_tenant_" + timestamp;
    }

    /**
     * 按创建逆序清理：先删租户再删套餐。清理前重登超管并切回租户 0，
     * 保证链路中途失败时清理请求仍具备平台侧权限；单步失败仅记录告警。
     */
    private void cleanupCreatedResources() {
        useTenant(DEFAULT_TENANT_ID);
        try {
            loginAsDefaultAdmin();
        } catch (Exception e) {
            log.warn("L3 清理前重登超管失败，后续清理可能因无权限跳过：{}", e.getMessage());
        }
        if (tenantId != null) {
            deleteQuietly(TENANT_PATH + "/" + tenantId);
        }
        if (packageId != null) {
            deleteQuietly(PACKAGE_PATH + "/" + packageId);
        }
    }
}
