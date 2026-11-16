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

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import top.continew.admin.api.support.L3FixtureSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * C-103 租户边界 403 与拒绝后不污染（R6 越权漂移 + R4 令牌自身声明判定 + 相邻行为 3）。
 *
 * <p>
 * 现有 28 条无任何 403/401 租户断言。本用例以租户管理员 E1 的令牌携带他租户头触发
 * {@code SaExtensionInterceptor} 的 extra 租户比对短路（msg「您当前没有访问该租户的权限」），
 * 并以「同令牌同自身租户立刻 200」反证拒绝不是端点故障、403 分支 clearContext 后
 * 下一请求身份正确（回源后归属无损）。租户边界取令牌自身 UserExtraContext 而非会话
 * （R4：会话级 UserContext 会被后一次登录覆盖）。
 * </p>
 * <p>
 * 平台超管携带他租户头仅记录观察项 OBS-2（放行需求侧未定义，不判失败）。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class TenantBoundaryApiTest extends L3FixtureSupport {

    /**
     * 主流程：建 T1/T2 → E1 携带 T2 头 403 → 自身租户 200 反证 → 403 后不污染 → OBS-2 → 数据面负断言。
     */
    @Test
    void tenantMismatchForbiddenAndNoResidualAfterRejection() {
        long timestamp = System.currentTimeMillis();
        String e1Username = "qa_tbd_e1_" + timestamp;
        String e2Username = "qa_tbd_e2_" + timestamp;
        // 口令匹配 PASSWORD 正则：8-32 位，含小写字母与数字
        String e1Password = "QaTbd1" + timestamp;
        String e2Password = "QaTbd2" + timestamp;
        Long packageId1 = null;
        Long packageId2 = null;
        Long tenantId1 = null;
        Long tenantId2 = null;
        String e1Token = null;
        try {
            // 步骤 1：数据准备（D-3 链路：套餐 → 租户 → 租户管理员凭据）
            packageId1 = createPackage("QA-TBD-" + timestamp + "-PKG1");
            packageId2 = createPackage("QA-TBD-" + timestamp + "-PKG2");
            tenantId1 =
                createTenant("qa_tbd_t1_" + timestamp, "qa-tbd1-" + timestamp + ".test.local",
                    packageId1, e1Username, e1Password);
            tenantId2 =
                createTenant("qa_tbd_t2_" + timestamp, "qa-tbd2-" + timestamp + ".test.local",
                    packageId2, e2Username, e2Password);
            Assertions.assertNotNull(tenantId1, "创建租户 T1 应返回非空 ID");
            Assertions.assertNotNull(tenantId2, "创建租户 T2 应返回非空 ID");

            // E1 登录必须携带自身租户头（登录请求经基座 useTenant 注入 X-Tenant-Id）
            useTenant(tenantId1);
            e1Token = login(e1Username, e1Password);

            // 步骤 2：跨租户拒绝——E1 令牌 + X-Tenant-Id=T2 → HTTP 403，msg 精确匹配，无用户资料
            // msg 采用收集式断言：writeJson 短路响应若中文乱码，记 encoding 证据（附 bodyHex）后
            // 继续，保证步骤 3-6（不污染、数据隔离等核心判定）拿到完整证据
            List<String> encodingViolations = new ArrayList<>();
            Response cross = userInfo(e1Token, tenantId2);
            Assertions.assertEquals(403, cross.statusCode(),
                "C-103 步骤 2：他租户头应 403，响应体：" + sanitize(cross.getBody().asString()));
            Assertions.assertEquals("403", cross.jsonPath().getString("code"),
                "C-103 步骤 2：业务码应为 403");
            String forbiddenMsg = cross.jsonPath().getString("msg");
            if (!TENANT_FORBIDDEN_MSG.equals(forbiddenMsg)) {
                encodingViolations.add("[ts=" + System.currentTimeMillis() + "] 403 msg 期望「"
                    + TENANT_FORBIDDEN_MSG + "」实际「" + forbiddenMsg + "」isAllQM="
                    + isAllQuestionMarks(forbiddenMsg) + " bodyHex=" + hexPrefix(cross, 48));
            }
            Assertions.assertFalse(cross.getBody().asString().contains("username"),
                "C-103 步骤 2：403 响应不得含任何用户资料字段");
            // 无条件输出 writeJson 短路响应的字节证据（0x3f＝服务端乱码；e6/c3＝测试侧解码问题）
            log.info("C-103 writeJson 403 证据: msg={} isAllQM={} bodyHex={}", forbiddenMsg,
                isAllQuestionMarks(forbiddenMsg), hexPrefix(cross, 64));

            // 步骤 3：反证——同令牌 + 自身租户头 200 且属主 E1（不是端点本身坏了）
            Response self = userInfo(e1Token, tenantId1);
            assertOwner(self, self.jsonPath().getLong("data.id"), e1Username, "C-103 步骤 3：自身租户");

            // 步骤 4：拒绝后不污染——403 与 200 紧贴交替，回源后身份始终正确
            for (int i = 1; i <= 3; i++) {
                Response rejected = userInfo(e1Token, tenantId2);
                Assertions.assertEquals(403, rejected.statusCode(),
                    "C-103 步骤 4 第 " + i + " 次：应仍 403");
                Response ok = userInfo(e1Token, tenantId1);
                assertOwner(ok, ok.jsonPath().getLong("data.id"), e1Username,
                    "C-103 步骤 4 第 " + i + " 次：403 后自读应完好");
            }

            // 步骤 5：平台侧对照（OBS-2 只记录不判定）——超管登录必须在租户 0 上下文下发起
            useTenant(DEFAULT_TENANT_ID);
            String aToken = loginAsDefaultAdmin();
            Response adminCross = userInfo(aToken, tenantId1);
            log.info("OBS-2 平台超管携带他租户头: HTTP {} code={} msg={}", adminCross.statusCode(), adminCross
                .jsonPath().getString("code"), adminCross.jsonPath().getString("msg"));

            // 步骤 6：数据面负断言——E1 用户列表不见平台超管（与 TenantApiTest 同负断言口径）
            useTenant(tenantId1);
            String e1SessionToken = login(e1Username, e1Password);
            Response userList =
                asToken(e1SessionToken, tenantId1).queryParam("description", "admin")
                    .get("/system/user/list");
            assertBizOk(userList);
            List<String> usernames = userList.jsonPath().getList("data.username", String.class);
            Assertions.assertFalse(usernames.contains("admin"),
                "C-103 步骤 6：租户列表混入平台超管即跨租户泄露，实际 " + usernames);
            if (!encodingViolations.isEmpty()) {
                encodingViolations.forEach(v -> log.error("C-103 encoding 违例: {}", v));
                Assertions.fail("C-103 403 短路响应中文乱码违例（DEFECT-1：writeJson 未设 UTF-8 或测试侧解码）"
                    + encodingViolations.size() + " 条，hex 证据见日志");
            }
        } finally {
            cleanupPlatformSide();
            if (tenantId1 != null) {
                deleteQuietly(TENANT_PATH + "/" + tenantId1);
            }
            if (tenantId2 != null) {
                deleteQuietly(TENANT_PATH + "/" + tenantId2);
            }
            if (packageId1 != null) {
                deleteQuietly(PACKAGE_PATH + "/" + packageId1);
            }
            if (packageId2 != null) {
                deleteQuietly(PACKAGE_PATH + "/" + packageId2);
            }
        }
    }

    /**
     * 清理前统一切回平台侧并重登超管（中途登录态可能停留在租户侧）。
     */
    private void cleanupPlatformSide() {
        useTenant(DEFAULT_TENANT_ID);
        try {
            loginAsDefaultAdmin();
        } catch (Exception e) {
            log.warn("L3 清理前重登超管失败，后续清理可能因无权限跳过：{}", e.getMessage());
        }
    }
}
