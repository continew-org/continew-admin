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

import java.util.List;
import java.util.Map;

/**
 * C-102 在线会话权限同步与管理员自身身份（R5 功能不回退 + R1 越权回归）。
 *
 * <p>
 * 与 PermissionMatrixApiTest 的关键差别：B 登录一次后全程不重登，验证
 * {@code RoleServiceImpl.updatePermission} 与 {@code UserServiceImpl.updateRole} 两处
 * 调用点经 {@code UserContextHolder.updateSessionContext} 对在线会话免重登生效
 * （docs/development/backend/security.mdx 行为契约，主理人裁定计入需求口径）。
 * 同时每步校验管理员自身身份未被替换为 B 的上下文（setContext 写当前线程上下文）。
 * </p>
 * <p>
 * 撤销语义说明：UserController#updateRole 为 PATCH 且 roleIds 标注 @NotEmpty，
 * 空集合会被参数校验拒绝，故撤销以「把 B 切换到另一空权限角色 R2」实现同等效果。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class OnlineSessionSyncApiTest extends L3FixtureSupport {

    /**
     * 主流程：拒绝基线 → 授权免重登生效（且 A 身份无损）→ 撤销免重登生效（且 A 身份无损）→ 交替回读一致。
     */
    @Test
    void onlineSessionPermissionSyncsWithoutReloginAndKeepsAdminIdentity() {
        long timestamp = System.currentTimeMillis();
        String roleCode = "qa_sync_" + timestamp;
        String roleName = "qa_sync_role_" + timestamp;
        String revokeRoleCode = "qa_sync2_" + timestamp;
        String revokeRoleName = "qa_sync_role2_" + timestamp;
        String username = "qa_sync_user_" + timestamp;
        String password = "QaSync" + timestamp;
        Long roleId = null;
        Long revokeRoleId = null;
        Long userId = null;
        String bToken = null;
        Map<String, String> bCookies = Map.of();
        try {
            // 数据准备：R1 空权限、R2 空权限（撤销目标态），B 初始绑定 R1
            roleId = createRole(roleName, roleCode);
            revokeRoleId = createRole(revokeRoleName, revokeRoleCode);
            userId = createUser(username, "qa_sync_user", password, List.of(roleId));

            bToken = login(username, password);
            bCookies = getLoginCookies();
            final String bSession = bToken;
            String aToken = loginAsDefaultAdmin();
            Response baseline = userInfo(aToken);
            Long aId = baseline.jsonPath().getLong("data.id");
            assertOwner(baseline, aId, "admin", "C-102 基线（A）");

            // 步骤 1：B 无 monitor:log:list → /system/log 拒绝（HTTP 403 + code=403 + 无权限 msg）
            Response denied = asToken(bToken).get(LOG_PATH + "?page=1&size=10");
            Assertions.assertEquals(403, denied.statusCode(),
                "C-102 步骤 1：无权限应 403，响应体：" + sanitize(denied
                    .getBody().asString()));
            Assertions.assertEquals("403", denied.jsonPath().getString("code"),
                "C-102 步骤 1：业务码应为 403");
            Assertions.assertTrue(denied.jsonPath().getString("msg").contains("没有访问权限"),
                "C-102 步骤 1：msg 应含「没有访问权限」");

            // 步骤 2：角色权限调用点授权 → A 自身身份与权限无损（若上下文被替换为 B，后续步会 403）
            updateRolePermission(roleId,
                List.of(MENU_ID_MONITOR_LOG_DIR, MENU_ID_MONITOR_LOG_LIST));
            Response aAfterGrant = userInfo(aToken);
            assertOwner(aAfterGrant, aId, "admin", "C-102 步骤 2：授权后 A 自读");
            List<String> aPerms = permissionsOf(aAfterGrant);
            Assertions.assertTrue(aPerms.contains("*:*:*") || aPerms.contains("system:user:list"),
                "C-102 步骤 2：A 权限应含超管通配或 system:user:list，实际 " + aPerms);
            Response userPage = asToken(aToken).get(USER_PATH + "?page=1&size=10");
            Assertions.assertEquals("0", userPage.jsonPath().getString("code"),
                "C-102 步骤 2：A 访问 /system/user 应业务成功，响应体：" + sanitize(userPage.getBody()
                    .asString()));

            // 步骤 3：授权对在线 B 免重登生效（不重登，仅靠会话上下文同步）
            awaitConsistency(() -> {
                Response logByB = asToken(bSession).get(LOG_PATH + "?page=1&size=10");
                Assertions.assertEquals("0", logByB.jsonPath().getString("code"),
                    "C-102 步骤 3：免重登授权后 B 访问 /system/log 应成功，响应体：" + sanitize(logByB.getBody()
                        .asString()));
                Assertions.assertNotNull(logByB.jsonPath().getList("data.list"),
                    "C-102 步骤 3：data.list 应为数组");
            });
            Response bAfterGrant = userInfo(bToken);
            assertOwner(bAfterGrant, userId, username, "C-102 步骤 3：B 自读");
            Assertions.assertTrue(permissionsOf(bAfterGrant).contains(PERM_MONITOR_LOG_LIST),
                "C-102 步骤 3：B 权限快照应含 monitor:log:list（免重登同步）");

            // 步骤 4：用户角色调用点撤销（切换到空权限 R2）→ A 身份无损、B 免重登被拒
            assignUserRoles(userId, List.of(revokeRoleId));
            Response aAfterRevoke = userInfo(aToken);
            assertOwner(aAfterRevoke, aId, "admin", "C-102 步骤 4：撤销后 A 自读");
            awaitConsistency(() -> {
                Response logAfterRevoke = asToken(bSession).get(LOG_PATH + "?page=1&size=10");
                Assertions.assertEquals(403, logAfterRevoke.statusCode(),
                    "C-102 步骤 4：撤销应免重登生效（B 再次 403），实际 HTTP " + logAfterRevoke.statusCode() + "，响应体："
                        + sanitize(
                            logAfterRevoke.getBody().asString()));
                Assertions.assertEquals("403", logAfterRevoke.jsonPath().getString("code"),
                    "C-102 步骤 4：撤销后业务码应为 403");
            });

            // 步骤 5：交替回读连续两轮，双方属主与规范化 JSON 恒等，无互相覆盖
            String refA = canonicalJson(userInfo(aToken).jsonPath().get("data"));
            String refB = canonicalJson(userInfo(bToken).jsonPath().get("data"));
            for (int round = 1; round <= 2; round++) {
                Response aRead = userInfo(aToken);
                assertOwner(aRead, aId, "admin", "C-102 步骤 5 第 " + round + " 轮 A 回读");
                Assertions.assertEquals(refA, canonicalJson(aRead.jsonPath().get("data")),
                    "C-102 步骤 5 第 " + round + " 轮：A 快照应恒等（未被 B 覆盖）");
                Response bRead = userInfo(bToken);
                assertOwner(bRead, userId, username, "C-102 步骤 5 第 " + round + " 轮 B 回读");
                Assertions.assertEquals(refB, canonicalJson(bRead.jsonPath().get("data")),
                    "C-102 步骤 5 第 " + round + " 轮：B 快照应恒等（未被 A 覆盖）");
            }
        } finally {
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
            if (revokeRoleId != null) {
                deleteQuietly(ROLE_PATH, Map.of("ids", List.of(revokeRoleId)));
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
