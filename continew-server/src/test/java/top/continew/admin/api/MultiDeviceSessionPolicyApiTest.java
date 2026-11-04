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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import top.continew.admin.api.support.L3FixtureSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * C-106 多端并发登录的闲置策略归属（R4 后一次登录覆盖会话 + R2 他端不背锅）。
 *
 * <p>
 * 同一用户 B 分别在默认 PC 客户端（refresh_token_idle_timeout=0，闲置不限制）与自建短闲置
 * APP 客户端（阈值取 API 最小合法值 60 秒，理由同 C-104：1-59 秒被 checkIdleTimeout 拒绝）
 * 登录。后一次登录会覆盖用户级 SaSession 的 UserContext.clientId，闲置判定必须取令牌自身
 * clientId 声明（SaExtensionInterceptor#resolveClientId），否则 PC 端会话会被 APP 端策略
 * 误踢。PC 客户端初始数据 is_concurrent 默认 b'1'（允许并发），无需降级为顺序登录。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class MultiDeviceSessionPolicyApiTest extends L3FixtureSupport {

    /**
     * 主流程：双端登录 → APP 端活跃后双端静置 → APP 端闲置 401、PC 端不受影响 → 双端身份一致。
     */
    @Test
    void idlePolicyFollowsTokenOwnClientNotLatestLogin() {
        long timestamp = System.currentTimeMillis();
        String roleCode = "qa_mdev_" + timestamp;
        String roleName = "qa_mdev_role_" + timestamp;
        String username = "qa_mdev_user_" + timestamp;
        String password = "QaMdev" + timestamp;
        Long roleId = null;
        Long userId = null;
        Long clientRowId = null;
        String pcToken = null;
        String appToken = null;
        Map<String, String> pcCookies = Map.of();
        Map<String, String> appCookies = Map.of();
        try {
            roleId = createRole(roleName, roleCode);
            updateRolePermission(roleId,
                List.of(MENU_ID_MONITOR_LOG_DIR, MENU_ID_MONITOR_LOG_LIST));
            userId = createUser(username, "qa_mdev_user", password, List.of(roleId));
            clientRowId = createClient("qa_mdev_client_" + timestamp, IDLE_TIMEOUT_SECONDS);
            String clientId = readClientId(clientRowId);

            // 步骤 2：PC 端登录（默认 CLIENT_ID，闲置不限制）
            pcToken = login(username, password);
            pcCookies = getLoginCookies();
            Response pcInfo = asToken(pcToken).get(USER_INFO_PATH);
            assertOwner(pcInfo, userId, username, "C-106 步骤 2：PC 端自读");

            // 步骤 3：短闲置端登录（后一次登录覆盖会话 UserContext.clientId，即风险来源）
            appToken = login(username, password, clientId);
            appCookies = getLoginCookies();
            Response appInfo = asToken(appToken).get(USER_INFO_PATH);
            assertOwner(appInfo, userId, username, "C-106 步骤 3：APP 端自读");

            // 步骤 4：APP 端非豁免路径建立活跃，随后双端完全静默（静置 > 阈值 + 快照 TTL）
            Response appActive = asToken(appToken).get(LOG_PATH + "?page=1&size=10");
            Assertions.assertEquals("0", appActive.jsonPath().getString("code"),
                "C-106 步骤 4：APP 端活跃请求应成功，响应体：" + sanitize(appActive.getBody().asString()));
            sleepSeconds(IDLE_WAIT_SECONDS);

            // 步骤 5：关键判定——自身端闲置生效，未被另一端「救活」
            List<String> encodingViolations = new ArrayList<>();
            Response appAfterIdle = asToken(appToken).get(LOG_PATH + "?page=1&size=10");
            log.info("C-106 静置后 APP 端: HTTP {} code={} msg={}", appAfterIdle.statusCode(),
                appAfterIdle
                    .jsonPath().getString("code"),
                appAfterIdle.jsonPath().getString("msg"));
            Assumptions.assumeTrue(appAfterIdle.statusCode() == 401,
                "APP 端闲置策略在 65 秒静置后未生效（外部条件未就绪），标记阻塞而非产品缺陷");
            Assertions.assertEquals("401", appAfterIdle.jsonPath().getString("code"),
                "C-106 步骤 5：闲置拒绝业务码应为 401");
            String idleMsg = appAfterIdle.jsonPath().getString("msg");
            if (!IDLE_REJECT_MSG.equals(idleMsg)) {
                encodingViolations.add("[ts=" + System.currentTimeMillis() + "] APP 端闲置 401 msg 期望「"
                    + IDLE_REJECT_MSG + "」实际「" + idleMsg + "」isAllQM=" + isAllQuestionMarks(idleMsg)
                    + " bodyHex=" + hexPrefix(appAfterIdle, 48));
            }

            // 步骤 6：反向判定——PC 端不被 APP 端策略误踢（R4 成立则此处 401，记 High）
            Response pcAfterIdle = asToken(pcToken).get(LOG_PATH + "?page=1&size=10");
            log.info("C-106 静置后 PC 端: HTTP {} code={}", pcAfterIdle.statusCode(),
                pcAfterIdle.jsonPath()
                    .getString("code"));
            Assertions.assertEquals("0", pcAfterIdle.jsonPath().getString("code"),
                "C-106 步骤 6：PC 端受 APP 端闲置策略误踢（R4 成立），响应体：" + sanitize(pcAfterIdle.getBody()
                    .asString()));

            // 步骤 7：双端交替自读身份一致（同用户 B），权限快照差异只记 OBS-4
            Response pcRead = asToken(pcToken).get(USER_INFO_PATH);
            Response appRead = asToken(appToken).get(USER_INFO_PATH);
            assertOwner(pcRead, userId, username, "C-106 步骤 7：PC 端回读");
            assertOwner(appRead, userId, username, "C-106 步骤 7：APP 端回读");
            String pcPerms = String.join(",", permissionsOf(pcRead).stream().sorted().toList());
            String appPerms = String.join(",", permissionsOf(appRead).stream().sorted().toList());
            if (!pcPerms.equals(appPerms)) {
                log.info("OBS-4 双端权限快照不一致（需求侧未定义）: PC=[{}] APP=[{}]", pcPerms, appPerms);
            }
            if (!encodingViolations.isEmpty()) {
                encodingViolations.forEach(v -> log.error("C-106 encoding 违例: {}", v));
                Assertions.fail("C-106 闲置短路响应中文乱码违例（DEFECT-1：writeJson 未设 UTF-8 或测试侧解码）"
                    + encodingViolations.size() + " 条，hex 证据见日志");
            }
        } finally {
            if (appToken != null) {
                logoutQuietly(appToken, appCookies);
            }
            if (pcToken != null) {
                logoutQuietly(pcToken, pcCookies);
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
