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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L3 权限矩阵黑盒测试：无权限拒绝、授权放行、超管放行、撤销回归拒绝。
 *
 * <p>
 * 目标端点 GET /system/log（@SaCheckPermission("monitor:log:list")，LogController），
 * 权限码取 sys_menu 初始数据 id=2031 的列表按钮（main_data.sql:125，父目录 2030 系统日志）。
 * 无权限响应形态：HTTP 403 + code="403" + msg「没有访问权限，请联系管理员授权"
 * （GlobalSaTokenExceptionHandler.handleNotPermissionException）；超管放行依据
 * RoleServiceImpl.listPermissionByUserId（角色码含 SUPER_ADMIN 时返回 ALL_PERMISSION 通配）。
 * </p>
 * <p>
 * 权限快照在登录时构建（AuthTokenServiceImpl.buildUserContext），授权与撤销后均重新登录
 * 获取新快照，不依赖在线上下文刷新语义。数据准备链路：创建角色 → 创建用户（绑定角色，
 * 密码经 RSA 加密）→ 登录访问目标端点。全部资源 qa_ 前缀 + 时间戳，finally 中按创建
 * 逆序清理（先用户后角色，均为批量删除端点 DELETE {base}，请求体 IdsReq.ids）。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Slf4j
@Tag("api")
class PermissionMatrixApiTest extends ApiTestSupport {

    /**
     * 角色管理 CRUD 端点。
     */
    private static final String ROLE_PATH = "/system/role";

    /**
     * 用户管理 CRUD 端点。
     */
    private static final String USER_PATH = "/system/user";

    /**
     * 系统日志端点（受 monitor:log:list 权限保护）。
     */
    private static final String LOG_PATH = "/system/log";

    /**
     * 角色功能权限修改端点。
     */
    private static final String ROLE_PERMISSION_PATH = "/permission";

    /**
     * monitor:log:list 权限按钮的菜单 ID（sys_menu 初始数据）。
     */
    private static final Long MENU_ID_MONITOR_LOG_LIST = 2031L;

    /**
     * monitor:log:list 所属系统日志目录的菜单 ID（sys_menu 初始数据）。
     */
    private static final Long MENU_ID_MONITOR_LOG_DIR = 2030L;

    /**
     * 无权限拒绝、授权放行、超管放行、撤销回归拒绝的完整矩阵。
     */
    @Test
    void permissionMatrixDenyGrantAdminAllowAndRevoke() {
        long timestamp = System.currentTimeMillis();
        // 角色编码匹配 GENERAL_CODE（2-30 位，字母开头），用户名匹配 USERNAME（字母开头）
        String roleCode = "qa_pmx_" + timestamp;
        String roleName = "qa_pmx_role_" + timestamp;
        String username = "qa_pmx_user_" + timestamp;
        // 密码匹配 PASSWORD 正则：8-32 位，至少含小写字母与数字
        String password = "QaPmx" + timestamp;
        Long roleId = null;
        Long userId = null;
        try {
            // 数据准备：创建角色与用户（用户创建时绑定角色，密码经 RSA 公钥加密）
            roleId = createRole(roleName, roleCode);
            assertThat(roleId).as("创建角色应返回非空 ID").isNotNull();
            userId = createUser(username, "qa_pmx_user", password, roleId);
            assertThat(userId).as("创建用户应返回非空 ID").isNotNull();

            // a. 角色未授权 monitor:log:list → 访问 /system/log 被拒绝（HTTP 403 + code 403）
            login(username, password);
            Response denied = givenAuth().get(LOG_PATH);
            assertThat(denied.statusCode()).as("无权限访问应返回 HTTP 403")
                .isEqualTo(403);
            assertThat(denied.jsonPath().getString("code")).as("无权限业务码应为 403")
                .isEqualTo("403");

            // b. 授权 monitor:log:list 后同一用户访问 → 通过（证明拒绝不是端点本身坏了）
            loginAsDefaultAdmin();
            useTenant(DEFAULT_TENANT_ID);
            updateRolePermission(roleId,
                List.of(MENU_ID_MONITOR_LOG_DIR, MENU_ID_MONITOR_LOG_LIST));
            login(username, password);
            Response granted = givenAuth().get(LOG_PATH);
            assertBizOk(granted);
            assertThat(granted.jsonPath().getList("data.list"))
                .as("授权后日志列表 data.list 应为数组结构")
                .isNotNull();

            // c. 超管访问同一端点 → 通过（ALL_PERMISSION 通配放行路径）
            loginAsDefaultAdmin();
            Response adminResp = givenAuth().get(LOG_PATH);
            assertBizOk(adminResp);

            // d. 撤销权限后再次访问 → 回归拒绝（重新登录取撤销后的权限快照）
            updateRolePermission(roleId, List.of());
            login(username, password);
            Response revoked = givenAuth().get(LOG_PATH);
            assertThat(revoked.statusCode()).as("撤销权限后访问应回归 HTTP 403")
                .isEqualTo(403);
        } finally {
            cleanupCreatedResources(userId, roleId);
        }
    }

    /**
     * 创建角色。
     *
     * @param name 角色名称（qa_ 前缀 + 时间戳）
     * @param code 角色编码（qa_ 前缀 + 时间戳）
     * @return 角色 ID
     */
    private Long createRole(String name, String code) {
        Map<String, Object> body = Map.of("name", name, "code", code, "sort", 1);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(ROLE_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 创建用户并绑定角色，密码按后端约定经 RSA 公钥加密（Create 组必填且校验格式）。
     *
     * @param username  用户名
     * @param nickname  昵称
     * @param rawPassword 明文密码
     * @param roleId    绑定角色 ID
     * @return 用户 ID
     */
    private Long createUser(String username, String nickname, String rawPassword, Long roleId) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("nickname", nickname);
        body.put("password", encryptByRsa(rawPassword));
        // 显式传性别（1=男，GenderEnum.value），不依赖服务端对缺省属性的隐式处理
        body.put("gender", 1);
        // 初始部门 id=1（main_data.sql:138 的 sys_dept 系统初始部门）
        body.put("deptId", 1L);
        body.put("roleIds", List.of(roleId));
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .post(USER_PATH);
        assertBizOk(response);
        return response.jsonPath().getLong("data.id");
    }

    /**
     * 修改角色功能权限（PUT /system/role/{id}/permission）。
     *
     * @param id      角色 ID
     * @param menuIds 菜单 ID 集合（空集即清空权限）
     */
    private void updateRolePermission(Long id, List<Long> menuIds) {
        Map<String, Object> body = Map.of("menuIds", menuIds);
        Response response = givenAuth().contentType(ContentType.JSON)
            .body(body)
            .put(ROLE_PATH + "/" + id + ROLE_PERMISSION_PATH);
        assertBizOk(response);
    }

    /**
     * 按创建逆序清理：先删用户再删角色。清理前重登超管并切回租户 0；
     * 用户与角色均为批量删除端点（DELETE {base}，请求体 IdsReq.ids），失败仅记录告警。
     *
     * @param userId 用户 ID
     * @param roleId 角色 ID
     */
    private void cleanupCreatedResources(Long userId, Long roleId) {
        useTenant(DEFAULT_TENANT_ID);
        try {
            loginAsDefaultAdmin();
        } catch (Exception e) {
            log.warn("L3 清理前重登超管失败，后续清理可能因无权限跳过：{}", e.getMessage());
        }
        if (userId != null) {
            deleteQuietly(USER_PATH, Map.of("ids", List.of(userId)));
        }
        if (roleId != null) {
            deleteQuietly(ROLE_PATH, Map.of("ids", List.of(roleId)));
        }
    }
}
