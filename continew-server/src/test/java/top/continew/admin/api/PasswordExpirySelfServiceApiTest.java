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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import top.continew.admin.api.support.L3FixtureSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * C-105 密码过期自助通道（R7 行为回退 + 验收 #2 异常抛出后线程本地不残留的 L3 观察点）。
 *
 * <p>
 * 拦截形态：SaTokenConfiguration 的 check 函数在非豁免路径对过期用户抛
 * PasswordExpiredException，经 AdminGlobalExceptionHandler 返回 HTTP 200 + code="428"
 * （default-http-status-code-on-error: 200），rejectExpiredPassword 抛出前已 clearContext；
 * 本用例以「428 之后另一正常会话 B 立即自读归属无损」验证异常路径不残留给下一请求。
 * 有效期在登录签发令牌时读取 PASSWORD_EXPIRATION_DAYS 并写入 UserContext，因此必须先改
 * 参数、回拨 pwd_reset_time，再登录 E（顺序颠倒则过期判定不生效）。
 * </p>
 * <p>
 * pwd_reset_time 回拨按主理人授权经 docker exec 直连隔离测试实例 MySQL（API 无该写入面）；
 * PASSWORD_EXPIRATION_DAYS 沿用 CacheEvictApiTest 的「改-验-恢复」模式，finally 必恢复。
 * 非豁免路径取 /system/log（E 的角色已授予 monitor:log:list），避免 system:user:list
 * 权限噪声与闲置用例同口径。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class PasswordExpirySelfServiceApiTest extends L3FixtureSupport {

    /**
     * 密码有效期参数编码（sys_option 初始数据，0 表示永不过期）。
     */
    private static final String PASSWORD_EXPIRATION_CODE = "PASSWORD_EXPIRATION_DAYS";

    /**
     * 临时有效期天数（回拨 30 天后必然过期）。
     */
    private static final String TEMP_EXPIRATION_DAYS = "7";

    /**
     * 正常对照用户 B 的会话令牌与登录 Cookie（提前签发以保持「未过期」快照，
     * 声明为实例字段以满足 Checkstyle 变量声明距离约束）。
     */
    private String bToken;

    private Map<String, String> bCookies = Map.of();

    /**
     * 过期用户 E 首次登录的 Cookie（改密前的旧会话，供清理注销）。
     */
    private Map<String, String> eCookies = Map.of();

    /**
     * 主流程：准备过期态 → 豁免路径 200 → 非豁免 428 → 异常后不残留 → 自助改密 → 恢复。
     */
    @Test
    void expiredPasswordKeepsSelfServiceChannelAndClearsContextOnReject() {
        long timestamp = System.currentTimeMillis();
        String roleCode = "qa_pwd_" + timestamp;
        String roleName = "qa_pwd_role_" + timestamp;
        String userB = "qa_pwd_userB_" + timestamp;
        String passB = "QaPwdB" + timestamp;
        String userE = "qa_pwd_userE_" + timestamp;
        String passE = "QaPwdE" + timestamp;
        String passE2 = "QaPwdN" + timestamp;
        Long roleId = null;
        Long userIdB = null;
        Long userIdE = null;
        Object optionId = null;
        String originalValue = null;
        boolean optionChanged = false;
        try {
            // 数据准备：角色（授予日志读权限）+ 正常用户 B + 待过期用户 E
            roleId = createRole(roleName, roleCode);
            updateRolePermission(roleId,
                List.of(MENU_ID_MONITOR_LOG_DIR, MENU_ID_MONITOR_LOG_LIST));
            userIdB = createUser(userB, "qa_pwd_userB", passB, List.of(roleId));
            userIdE = createUser(userE, "qa_pwd_userE", passE, List.of(roleId));

            // 步骤 1（D-5）：记录参数基线 → 改 7 天 → DB 回拨 E 的 pwd_reset_time 30 天 → 再登录 E
            // 参数读改需超管权限，必须在以 B / E 覆盖基座登录态之前完成
            Map<?, ?> baseline = findOption(PASSWORD_EXPIRATION_CODE);
            optionId = baseline.get("id");
            originalValue = String.valueOf(baseline.get("value"));
            log.info("C-105 参数基线: code={} id={} value={}", PASSWORD_EXPIRATION_CODE, optionId,
                originalValue);
            updateOption(optionId, PASSWORD_EXPIRATION_CODE, TEMP_EXPIRATION_DAYS);
            optionChanged = true;
            execMysql(
                "UPDATE sys_user SET pwd_reset_time = DATE_SUB(NOW(), INTERVAL 30 DAY) WHERE username='"
                    + userE + "'");

            bToken = login(userB, passB);
            bCookies = getLoginCookies();

            String eToken = login(userE, passE);
            eCookies = getLoginCookies();

            // 步骤 2：豁免路径 /auth/user/info 对过期用户放行，且过期标记正确
            Response info = asToken(eToken).get(USER_INFO_PATH);
            assertOwner(info, userIdE, userE, "C-105 步骤 2：过期用户豁免自读");
            Assertions.assertTrue(info.jsonPath().getBoolean("data.pwdExpired"),
                "C-105 步骤 2：data.pwdExpired 应为 true");
            Assertions.assertEquals(0, (int) info.jsonPath().getInt("data.pwdExpireDays"),
                "C-105 步骤 2：过期时 data.pwdExpireDays 应为 0（AuthController 同源推导）");

            // 步骤 3：非豁免路径拦截——HTTP 200 + code="428" + msg 精确匹配 + 无业务数据
            Response blocked = asToken(eToken).get(LOG_PATH + "?page=1&size=10");
            Assertions.assertEquals(200, blocked.statusCode(),
                "C-105 步骤 3：428 走全局错误响应，HTTP 应为 200");
            Assertions.assertEquals(PASSWORD_EXPIRED_CODE, blocked.jsonPath().getString("code"),
                "C-105 步骤 3：业务码应为 428，响应体：" + sanitize(blocked.getBody().asString()));
            Assertions.assertEquals(PASSWORD_EXPIRED_MSG, blocked.jsonPath().getString("msg"),
                "C-105 步骤 3：拦截消息应精确匹配");
            Assertions.assertFalse(blocked.getBody().asString().contains("\"list\""),
                "C-105 步骤 3：拦截响应不应携带业务数据");

            // 步骤 4：异常抛出后线程本地不残留（验收 #2 的 L3 观察点）——B 会话立即自读无损
            Response bInfo = asToken(bToken).get(USER_INFO_PATH);
            assertOwner(bInfo, userIdB, userB, "C-105 步骤 4：428 之后 B 自读（残留判定点）");
            Response blockedAgain = asToken(eToken).get(LOG_PATH + "?page=1&size=10");
            Assertions.assertEquals(PASSWORD_EXPIRED_CODE,
                blockedAgain.jsonPath().getString("code"),
                "C-105 步骤 4：E 重复请求应仍 428");

            // 步骤 5：自助改密（豁免路径 PATCH /user/profile/password，updatePassword 同步刷新 pwd_reset_time）
            Map<String, Object> pwdBody = new HashMap<>();
            pwdBody.put("oldPassword", encryptByRsa(passE));
            pwdBody.put("newPassword", encryptByRsa(passE2));
            Response changePwd = asToken(eToken).contentType(ContentType.JSON)
                .body(pwdBody)
                .patch("/user/profile/password");
            assertBizOk(changePwd);

            // 步骤 6：以新口令重登后恢复——非豁免路径 200、过期标记清除
            String eToken2 = login(userE, passE2);
            Response recovered = asToken(eToken2).get(LOG_PATH + "?page=1&size=10");
            Assertions.assertEquals("0", recovered.jsonPath().getString("code"),
                "C-105 步骤 6：改密重登后非豁免路径应恢复，响应体：" + sanitize(recovered.getBody().asString()));
            Response infoAfter = asToken(eToken2).get(USER_INFO_PATH);
            Assertions.assertFalse(
                Boolean.TRUE.equals(infoAfter.jsonPath().getBoolean("data.pwdExpired")),
                "C-105 步骤 6：改密后 data.pwdExpired 应为 false");

            // 登出本次全部会话，避免登录态干扰恢复校验
            logoutQuietly(eToken2, getLoginCookies());
            logoutQuietly(eToken, eCookies);
            logoutQuietly(bToken, bCookies);
        } finally {
            // 必须恢复参数并回读确认，否则污染后续轮次全部登录
            if (optionChanged && optionId != null) {
                try {
                    cleanupPlatformSide();
                    updateOption(optionId, PASSWORD_EXPIRATION_CODE, originalValue);
                    Map<?, ?> restored = findOption(PASSWORD_EXPIRATION_CODE);
                    Assertions.assertEquals(originalValue, String.valueOf(restored.get("value")),
                        "C-105 清理：PASSWORD_EXPIRATION_DAYS 应恢复原值");
                    log.info("C-105 参数已恢复: {}", originalValue);
                } catch (Exception e) {
                    log.error("C-105 恢复 PASSWORD_EXPIRATION_DAYS 失败，需人工复原为 {}：{}", originalValue,
                        e.getMessage());
                }
            }
            cleanupPlatformSide();
            if (userIdE != null) {
                deleteQuietly(USER_PATH, Map.of("ids", List.of(userIdE)));
            }
            if (userIdB != null) {
                deleteQuietly(USER_PATH, Map.of("ids", List.of(userIdB)));
            }
            if (roleId != null) {
                deleteQuietly(ROLE_PATH, Map.of("ids", List.of(roleId)));
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
