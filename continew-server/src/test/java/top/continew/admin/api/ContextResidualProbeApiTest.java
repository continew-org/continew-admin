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
import java.util.Map;

/**
 * C-101 跨请求上下文残留探针 + C-107 异常夹带下的合法会话不误清。
 *
 * <p>
 * 针对 {@code UserContextHolder.getContext()} 归属校验与
 * {@code SaExtensionInterceptor.preHandle} 异常路径清理：以高密度交替采样验证池化线程
 * 复用场景下任何一次自读都归属令牌属主（R1 越权），且异常请求（伪造 token 401、跨租户 403、
 * 未认证 401）之后合法请求不被迫重登、快照无损（R2 误清反向风险）。
 * </p>
 * <p>
 * 违例分桶（保持断言严格性，失败可归位到具体缺陷）：core＝越权/误清本体（属主不符、合法请求
 * 401/403、资料串读）；sync＝权限撤销未在线同步（RoleServiceImpl.updatePermission 仅在
 * RoleMenuServiceImpl.add 返回 true 时同步会话，撤销置空走批量插入空集合返回 false 的分支）；
 * encoding＝拦截器手工 writeJson 短路响应中文乱码（ServletUtils.writeJson 经 hutool write
 * 不设 charset，Writer 按容器默认 ISO-8859-1 输出，'?'=0x3F 以字节十六进制佐证）。
 * </p>
 * <p>
 * 判定强度：Tomcat NIO 不保证同一连接固定同一 worker 线程，本用例是高密度采样而非必然复现；
 * 放行判据是 3 轮 × 150 次 core 桶全部 0 违例。跨租户夹带使用真实存在的探测租户，占位租户 ID
 * 会被上游租户存在性校验以业务码 500 拒绝，到不了 SaExtensionInterceptor 的 403 分支。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class ContextResidualProbeApiTest extends L3FixtureSupport {

    /**
     * C-101 单轮迭代次数（主理人裁定的最严口径，三轮合计 450 次）。
     */
    private static final int PROBE_ITERATIONS = 150;

    /**
     * 伪造 JWT 形态的非法令牌（解析失败按未认证 401 处理）。
     */
    private static final String TAMPERED_TOKEN = "eyJhbGciOiJIUzI1NiJ9.tampered.invalidsig";

    /**
     * C-107 迭代次数。
     */
    private static final int EXCLUDE_ITERATIONS = 30;

    /**
     * 每桶保留完整证据的最大条数（其余只计数，避免日志淹没）。
     */
    private static final int MAX_EVIDENCE_PER_BUCKET = 3;

    /**
     * C-101：150 次交替身份自读，夹带 grant/revoke 扰动、伪造 token 异常对与跨租户夹带。
     */
    @Test
    void crossRequestContextResidualProbe() {
        long timestamp = System.currentTimeMillis();
        String roleCode = "qa_ctx_" + timestamp;
        String roleName = "qa_ctx_role_" + timestamp;
        String username = "qa_ctx_user_" + timestamp;
        String password = "QaCtx" + timestamp;
        Long roleId = null;
        Long userId = null;
        Long probePackageId = null;
        Long probeTenantId = null;
        String bToken = null;
        Map<String, String> bCookies = Map.of();
        List<String> coreViolations = new ArrayList<>();
        List<String> syncViolations = new ArrayList<>();
        List<String> encodingViolations = new ArrayList<>();
        int snapshotStaleCount = 0;
        int revokedStillAccessible = 0;
        int garbled403Count = 0;
        int successA = 0;
        int successB = 0;
        int denied401 = 0;
        int denied403 = 0;
        try {
            // 数据准备：低权限角色 R1（初始不含 monitor:log:list）+ 用户 B 绑定 + 真实探测租户
            roleId = createRole(roleName, roleCode);
            userId = createUser(username, "qa_ctx_user", password, List.of(roleId));
            probePackageId = createPackage("QA-CTX-" + timestamp + "-PKG");
            probeTenantId =
                createTenant("qa_ctx_tenant_" + timestamp, "qa-ctx-" + timestamp + ".test.local",
                    probePackageId, "qa_ctx_admin_" + timestamp, "QaCtxAdm" + timestamp);

            bToken = login(username, password);
            bCookies = getLoginCookies();
            String aToken = loginAsDefaultAdmin();
            Response first = userInfo(aToken);
            Long aId = first.jsonPath().getLong("data.id");
            assertOwner(first, aId, "admin", "C-101 基线");

            for (int k = 1; k <= PROBE_ITERATIONS; k++) {
                // 步骤 1：A 自读，属主恒为 admin
                Response respA = userInfo(aToken);
                if (isOwner(respA, aId, "admin")) {
                    successA++;
                } else {
                    recordViolation(coreViolations, k, "A 自读属主异常（合法请求 401/403 或串读）", respA);
                }
                // 步骤 2：B 自读属主恒为 B；撤销未同步（快照仍含 monitor:log:list）记 sync 桶
                Response respB = userInfo(bToken);
                if (!isOwner(respB, userId, username)) {
                    recordViolation(coreViolations, k, "B 自读属主异常（串读/误清）", respB);
                } else {
                    successB++;
                    if (permissionsOf(respB).contains(PERM_MONITOR_LOG_LIST)) {
                        snapshotStaleCount++;
                        recordViolation(syncViolations, k,
                            "撤销后 B 在线快照仍含 monitor:log:list（DEFECT-2）", respB);
                    }
                }
                // 步骤 3（每 10 次）：grant/revoke 扰动 + 扰动后双方自读 + 撤销残留访问探测
                if (k % 10 == 0) {
                    updateRolePermission(roleId,
                        List.of(MENU_ID_MONITOR_LOG_DIR, MENU_ID_MONITOR_LOG_LIST));
                    Response adminAfterGrant = userInfo(aToken);
                    if (!isOwner(adminAfterGrant, aId, "admin")) {
                        recordViolation(coreViolations, k, "授权扰动后 A 自读属主异常（被他人上下文替换即此形态）",
                            adminAfterGrant);
                    }
                    Response logByAdmin = asToken(aToken).get(LOG_PATH + "?page=1&size=10");
                    if (logByAdmin.statusCode() == 401 || logByAdmin.statusCode() == 403) {
                        recordViolation(coreViolations, k, "授权扰动后 A 访问 /system/log 被 401/403",
                            logByAdmin);
                    }
                    updateRolePermission(roleId, List.of());
                    Response bAfterRevoke = userInfo(bToken);
                    if (!isOwner(bAfterRevoke, userId, username)) {
                        recordViolation(coreViolations, k, "撤销扰动后 B 自读属主异常", bAfterRevoke);
                    }
                    // DEFECT-2 安全影响证据：撤销接口成功后，在线 B 仍可通过已撤销权限访问 /system/log
                    Response logAfterRevoke = asToken(bToken).get(LOG_PATH + "?page=1&size=10");
                    if ("0".equals(safeCode(logAfterRevoke))) {
                        revokedStillAccessible++;
                        recordViolation(syncViolations, k, "撤销后在线 B 仍可访问 /system/log（已撤销权限残留）",
                            logAfterRevoke);
                    }
                }
                // 步骤 4（每 25 次）：伪造 token 异常对——401 之后合法 A 请求必须完好
                if (k % 25 == 0) {
                    Response forged = userInfo(TAMPERED_TOKEN);
                    if (forged.statusCode() == 401
                        && "401".equals(forged.jsonPath().getString("code"))) {
                        denied401++;
                    } else {
                        recordViolation(coreViolations, k, "伪造 token 期望 HTTP 401 + code=401",
                            forged);
                    }
                    Response aAfterForged = userInfo(aToken);
                    if (isOwner(aAfterForged, aId, "admin")) {
                        successA++;
                    } else {
                        recordViolation(coreViolations, k, "伪造 token 异常后 A 自读被误清", aAfterForged);
                    }
                }
                // 步骤 5（每 25 次）：跨租户夹带（真实存在的探测租户）——403 之后合法 A 请求必须完好
                if (k % 25 == 0) {
                    Response crossTenant =
                        asToken(aToken, probeTenantId).get(USER_PATH + "?page=1&size=10");
                    if (crossTenant.statusCode() == 403
                        && "403".equals(crossTenant.jsonPath().getString("code"))) {
                        denied403++;
                        if (isAllQuestionMarks(crossTenant.jsonPath().getString("msg"))) {
                            garbled403Count++;
                            recordViolation(encodingViolations, k,
                                "跨租户 403 响应 msg 中文乱码（writeJson 未设 UTF-8，bodyHex="
                                    + hexPrefix(crossTenant, 64)
                                    + "）",
                                crossTenant);
                        }
                    } else {
                        recordViolation(coreViolations, k, "跨租户夹带期望 HTTP 403 + code=403",
                            crossTenant);
                    }
                    Response aAfterCross = userInfo(aToken);
                    if (isOwner(aAfterCross, aId, "admin")) {
                        successA++;
                    } else {
                        recordViolation(coreViolations, k, "跨租户 403 后 A 自读被污染", aAfterCross);
                    }
                }
                // 串读反向检查：A 的响应体不得出现 B 的用户名，反之亦然
                if (respA.getBody().asString().contains(username)) {
                    recordViolation(coreViolations, k, "A 响应体出现 B 的用户名（资料串读）", respA);
                }
                if (respB.getBody().asString().contains("\"username\":\"admin\"")) {
                    recordViolation(coreViolations, k, "B 响应体出现平台超管资料（资料串读）", respB);
                }
            }

            // 附带断言（安全矩阵「/monitor/online 仅超管可见」）：B 无 monitor:online:list → 403
            Response onlineDenied = asToken(bToken).get(ONLINE_PATH + "?page=1&size=10");
            if (onlineDenied.statusCode() != 403) {
                recordViolation(coreViolations, PROBE_ITERATIONS, "B 访问 /monitor/online 期望 403",
                    onlineDenied);
            }
            Response onlineAllowed = asToken(aToken).get(ONLINE_PATH + "?page=1&size=10");
            if (onlineAllowed.statusCode() != 200
                || !"0".equals(onlineAllowed.jsonPath().getString("code"))) {
                recordViolation(coreViolations, PROBE_ITERATIONS,
                    "A 访问 /monitor/online 期望 200/code=0",
                    onlineAllowed);
            }
            log.info("C-101 计数汇总: iterations={} successA={} successB={} denied401={} denied403={} "
                + "syncEvidence（快照残留 {} 次 / 撤销后仍可访问 {} 次）encodingEvidence（乱码 403 {} 次）",
                PROBE_ITERATIONS, successA, successB, denied401, denied403, snapshotStaleCount,
                revokedStillAccessible,
                garbled403Count);
            Assertions.assertEquals(6, denied401, "C-101 伪造 token 异常对触发次数应为 150/25=6");
            Assertions.assertEquals(6, denied403, "C-101 跨租户夹带（探测租户）触发次数应为 150/25=6");
        } finally {
            log.info("C-101 违例条数: core={} sync={} encoding={}", coreViolations.size(),
                syncViolations.size(),
                encodingViolations.size());
            coreViolations.forEach(v -> log.error("C-101 core 违例: {}", v));
            syncViolations.forEach(v -> log.error("C-101 sync 违例: {}", v));
            encodingViolations.forEach(v -> log.error("C-101 encoding 违例: {}", v));
            if (bToken != null) {
                logoutQuietly(bToken, bCookies);
            }
            cleanupPlatformSide();
            if (userId != null) {
                deleteQuietly(USER_PATH, Map.of("ids", List.of(userId)));
            }
            if (roleId != null) {
                deleteQuietly(ROLE_PATH, Map.of("ids", List.of(roleId)));
            }
            if (probeTenantId != null) {
                deleteQuietly(TENANT_PATH + "/" + probeTenantId);
            }
            if (probePackageId != null) {
                deleteQuietly(PACKAGE_PATH + "/" + probePackageId);
            }
        }
        // 主判定点（最严口径）：core 桶任何一次 (a)(b)(c)(d) 事件即失败；sync/encoding 桶分别定位产品缺陷
        if (!coreViolations.isEmpty()) {
            Assertions.fail("C-101 越权/误清核心违例 " + coreViolations.size() + " 条（明细见 core 违例日志）");
        }
        if (!syncViolations.isEmpty()) {
            Assertions.fail("C-101 权限撤销未在线同步违例（DEFECT-2：RoleServiceImpl.updatePermission 撤销分支跳过 "
                + "updateUserContext）证据 " + syncViolations.size() + " 条（明细见 sync 违例日志）");
        }
        if (!encodingViolations.isEmpty()) {
            Assertions.fail("C-101 短路响应中文乱码违例（DEFECT-1：拦截器 writeJson 未设 UTF-8 charset）证据 "
                + encodingViolations.size() + " 条（明细见 encoding 违例日志）");
        }
    }

    /**
     * C-107：30 次「未认证异常请求夹在合法请求之间」，合法会话不得被迫重登且快照逐次一致。
     */
    @Test
    void unauthorizedProbesDoNotEvictValidSession() {
        long timestamp = System.currentTimeMillis();
        String roleCode = "qa_exc_" + timestamp;
        String roleName = "qa_exc_role_" + timestamp;
        String username = "qa_exc_user_" + timestamp;
        String password = "QaExc" + timestamp;
        Long roleId = null;
        Long userId = null;
        List<String> violations = new ArrayList<>();
        try {
            roleId = createRole(roleName, roleCode);
            userId = createUser(username, "qa_exc_user", password, List.of(roleId));
            String aToken = loginAsDefaultAdmin();
            Response baseline = userInfo(aToken);
            Long aId = baseline.jsonPath().getLong("data.id");
            assertOwner(baseline, aId, "admin", "C-107 基线");
            String refJson = canonicalJson(baseline.jsonPath().get("data"));

            for (int k = 1; k <= EXCLUDE_ITERATIONS; k++) {
                // 步骤 2：夹带乱码令牌的未认证请求 → 401
                Response garbage = asToken(TAMPERED_TOKEN).get(USER_PATH + "?page=1&size=10");
                if (garbage.statusCode() != 401) {
                    recordViolation(violations, k, "乱码令牌访问 /system/user 期望 401", garbage);
                }
                // 步骤 3：夹带无 Authorization 头的未认证请求 → 401
                Response noAuth = given().header("X-Tenant-Id", String.valueOf(DEFAULT_TENANT_ID))
                    .get(USER_INFO_PATH);
                if (noAuth.statusCode() != 401) {
                    recordViolation(violations, k, "无令牌访问 /auth/user/info 期望 401", noAuth);
                }
                // 步骤 4：立即合法 A 自读 → 200、属主 A、快照与基线规范化 JSON 恒等（未误清、无需重登）
                Response afterProbe = userInfo(aToken);
                if (!isOwner(afterProbe, aId, "admin")) {
                    recordViolation(violations, k, "未认证夹带后 A 自读被误清（被迫重登即此形态）", afterProbe);
                } else if (!canonicalJson(afterProbe.jsonPath().get("data")).equals(refJson)) {
                    recordViolation(violations, k, "未认证夹带后 A 快照与基线不一致", afterProbe);
                }
                // 步骤 5：会话完好（非豁免读路径仍可用）
                Response userList = asToken(aToken).get(USER_PATH + "?page=1&size=10");
                if (userList.statusCode() != 200
                    || !"0".equals(userList.jsonPath().getString("code"))) {
                    recordViolation(violations, k, "未认证夹带后 A 访问 /system/user 期望 200/code=0",
                        userList);
                }
            }
            log.info("C-107 计数汇总: iterations={} violations={}", EXCLUDE_ITERATIONS,
                violations.size());
        } finally {
            violations.forEach(v -> log.error("C-107 违例: {}", v));
            cleanupPlatformSide();
            if (userId != null) {
                deleteQuietly(USER_PATH, Map.of("ids", List.of(userId)));
            }
            if (roleId != null) {
                deleteQuietly(ROLE_PATH, Map.of("ids", List.of(roleId)));
            }
        }
        if (!violations.isEmpty()) {
            Assertions.fail("C-107 误清探针违例 " + violations.size() + " 条（明细见违例日志）");
        }
    }

    /**
     * 判断自读响应是否严格归属指定属主（HTTP 200 + 业务码 0 + id + username 四条件）。
     *
     * @param response
     *            user/info 响应
     * @param expectedId
     *            属主 ID
     * @param expectedUsername
     *            属主用户名
     * @return 是否归属正确
     */
    private boolean isOwner(Response response, Long expectedId, String expectedUsername) {
        if (response.statusCode() != 200 || !"0".equals(response.jsonPath().getString("code"))) {
            return false;
        }
        return expectedId.equals(response.jsonPath().getLong("data.id"))
            && expectedUsername.equals(response
                .jsonPath().getString("data.username"));
    }

    /**
     * 记录违例：时间戳 + 迭代序号 + 状态码 + 业务码 + 脱敏响应体；超过证据上限后只计数。
     *
     * @param violations
     *            违例收集器
     * @param iteration
     *            迭代序号
     * @param note
     *            违例描述
     * @param response
     *            触发现场响应
     */
    private void recordViolation(List<String> violations, int iteration, String note,
        Response response) {
        if (violations.size() < MAX_EVIDENCE_PER_BUCKET) {
            String entry = String.format("[ts=%d iter=%d] %s：HTTP %s code=%s body=%s",
                System.currentTimeMillis(),
                iteration, note, response.statusCode(), safeCode(response),
                sanitize(response.getBody().asString()));
            violations.add(entry);
            log.error("L3 现场证据 {}", entry);
        } else if (violations.size() == MAX_EVIDENCE_PER_BUCKET) {
            violations.add("（同类违例继续发生，仅计数不再输出明细）");
        }
    }

    /**
     * 安全读取业务码（非 JSON 响应体时返回 N/A，避免证据记录本身抛错）。
     *
     * @param response
     *            响应
     * @return 业务码文本
     */
    private static String safeCode(Response response) {
        try {
            return String.valueOf(response.jsonPath().getString("code"));
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * 清理前统一切回平台侧并重登超管（基座登录态可能被其他会话覆盖）。
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
