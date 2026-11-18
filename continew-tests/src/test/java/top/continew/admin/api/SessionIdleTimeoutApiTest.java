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
 * C-104 闲置超时拒绝与自助通道豁免（R8 行为漂移 + R2 豁免路径不误伤 + 相邻行为 2）。
 *
 * <p>
 * 内置 PC 客户端 refresh_token_idle_timeout=0 无法触发闲置判定，本用例自建 APP 客户端。
 * 阈值取 API 最小合法值 60 秒：ClientServiceImpl#checkIdleTimeout 拒绝 1-59 秒。闲置静置
 * 窗口 65 秒同时覆盖 ClientPolicyCache 快照 60 秒 TTL，探针请求读取策略时快照必已过期重载，
 * 新客户端阈值必然参与判定，无需额外等待轮询。
 * </p>
 * <p>
 * 非豁免路径改用 /system/log（自建角色授予 monitor:log:list）：/system/user 需
 * system:user:list，普通 B 触发的是 403 权限拒绝而非闲置 401，判定面会被权限噪声污染。
 * 豁免路径按 auth.password.excludes 配置四条：/auth/user/info 与 /auth/user/route 在本用例断言。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class SessionIdleTimeoutApiTest extends L3FixtureSupport {

    /**
     * 主流程：触发闲置 401 → 豁免路径不误伤（200 且属主正确）→ OBS-3 记录 touch 后行为。
     */
    @Test
    void idleTimeoutRejectsNonExemptPathButSparesSelfServicePaths() {
        long timestamp = System.currentTimeMillis();
        String roleCode = "qa_idle_" + timestamp;
        String roleName = "qa_idle_role_" + timestamp;
        String username = "qa_idle_user_" + timestamp;
        String password = "QaIdle" + timestamp;
        Long roleId = null;
        Long userId = null;
        Long clientRowId = null;
        // 探针登录的 token 与 Cookie 要注销，否则在线会话会挡死客户端删除
        List<String> attemptTokens = new ArrayList<>();
        List<Map<String, String>> attemptCookies = new ArrayList<>();
        try {
            // 数据准备：角色授予 monitor:log:list（闲置判定只看路径与阈值，授权先行避免 403 噪声）
            roleId = createRole(roleName, roleCode);
            updateRolePermission(roleId,
                List.of(MENU_ID_MONITOR_LOG_DIR, MENU_ID_MONITOR_LOG_LIST));
            userId = createUser(username, "qa_idle_user", password, List.of(roleId));
            clientRowId = createClient("qa_idle_client_" + timestamp, IDLE_TIMEOUT_SECONDS);
            String clientId = readClientId(clientRowId);

            // 步骤 2+3：登录 → 活跃请求 → 等策略快照重载 → 预写过期活跃记录 → 探测必须闲置 401。
            // 闲置判定 = 会话活跃记录早于阈值 + 客户端策略可见，两条件分别由预写与快照重载窗口保证，
            // 不再依赖真静置 65 秒的概率性触发
            String token = login(username, password, clientId);
            attemptTokens.add(token);
            attemptCookies.add(getLoginCookies());
            Response active = asToken(token).get(LOG_PATH + "?page=1&size=10");
            log.info("C-104 建立活跃: HTTP {} code={}", active.statusCode(), active.jsonPath()
                .getString("code"));
            // 快照 TTL 60 秒：新建客户端的闲置策略在下一次快照重载后才可见，等待窗口只服务于此
            sleepSeconds(IDLE_WAIT_SECONDS);
            // 最后一次请求之后预写：活跃记录回退到阈值之外，预写值不会被 touch 覆盖
            presetExpiredActivity(token, IDLE_TIMEOUT_SECONDS * 2);
            Response probe = asToken(token).get(LOG_PATH + "?page=1&size=10");
            log.info("C-104 预写后探测: HTTP {} code={} msg={}", probe.statusCode(), probe.jsonPath()
                .getString("code"), probe.jsonPath().getString("msg"));
            Response idleReject = probe;
            String rejectedToken = token;
            // 预写与快照窗口均已保证，401 不出现即产品判定缺陷，不再阻塞跳过
            Assertions.assertEquals(401, idleReject.statusCode(),
                "C-104 预写过期活跃记录后非豁免路径应闲置 401，响应体：" + sanitize(idleReject.getBody()
                    .asString()));

            // 正式判定：401 + code="401" + data 为空；msg 精确匹配失败按 DEFECT-1（writeJson 未设
            // UTF-8）收集证据后继续，保证步骤 4/5（豁免不误伤与 OBS-3）拿到完整判定证据
            List<String> encodingViolations = new ArrayList<>();
            Assertions.assertEquals("401", idleReject.jsonPath().getString("code"),
                "C-104 闲置拒绝业务码应为 401");
            String idleMsg = idleReject.jsonPath().getString("msg");
            if (!IDLE_REJECT_MSG.equals(idleMsg)) {
                encodingViolations.add("[ts=" + System.currentTimeMillis() + "] 闲置 401 msg 期望「"
                    + IDLE_REJECT_MSG + "」实际「" + idleMsg + "」bodyHex=" + hexPrefix(idleReject, 48));
            }
            Assertions.assertNull(idleReject.jsonPath().get("data"),
                "C-104 闲置拒绝响应不应携带业务数据，实际：" + sanitize(idleReject.getBody().asString()));

            // 步骤 4：豁免路径不误伤——闲置 401 之后同 token 自助通道仍 200 且属主正确（R2 关键判定点）
            Response exemptInfo = asToken(rejectedToken).get(USER_INFO_PATH);
            assertOwner(exemptInfo, userId, username, "C-104 步骤 4：豁免路径 /auth/user/info");
            Response exemptRoute = asToken(rejectedToken).get(USER_ROUTE_PATH);
            Assertions.assertEquals(200, exemptRoute.statusCode(),
                "C-104 步骤 4：豁免路径 /auth/user/route 应 200，响应体：" + sanitize(exemptRoute.getBody()
                    .asString()));
            Assertions.assertEquals("0", exemptRoute.jsonPath().getString("code"),
                "C-104 步骤 4：豁免路径业务码应为 0");

            // 步骤 5：OBS-3 只记录——豁免请求会 touch 活跃时间，非豁免路径是否恢复需求侧未定义
            Response afterTouch = asToken(rejectedToken).get(LOG_PATH + "?page=1&size=10");
            log.info("OBS-3 闲置 401 后经豁免路径 touch，再访问非豁免路径: HTTP {} code={}", afterTouch.statusCode(),
                afterTouch
                    .jsonPath().getString("code"));
            if (!encodingViolations.isEmpty()) {
                encodingViolations.forEach(v -> log.error("C-104 encoding 违例: {}", v));
                Assertions.fail("C-104 短路响应中文乱码违例（DEFECT-1：SaExtensionInterceptor writeJson 未设 "
                    + "UTF-8 charset）" + encodingViolations.size() + " 条");
            }
        } finally {
            for (int i = 0; i < attemptTokens.size(); i++) {
                logoutQuietly(attemptTokens.get(i), attemptCookies.get(i));
            }
            cleanupPlatformSide();
            if (userId != null) {
                deleteQuietly(USER_PATH, Map.of("ids", List.of(userId)));
            }
            if (roleId != null) {
                deleteQuietly(ROLE_PATH, Map.of("ids", List.of(roleId)));
            }
            if (clientRowId != null) {
                deleteQuietly(CLIENT_PATH, Map.of("ids", List.of(clientRowId)));
            }
        }
    }

    /**
     * 清理前统一切回平台侧并重登超管。
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
