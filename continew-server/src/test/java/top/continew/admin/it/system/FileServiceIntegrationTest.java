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

package top.continew.admin.it.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.model.req.FileReq;
import top.continew.admin.system.service.FileService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 文件 Service 集成测试（L2）
 *
 * <p>
 * 聚焦黑盒接口覆盖不到的服务层点：目录创建只落元数据（path 由 name 与 parentPath 拼接、
 * 归属默认存储）、同名目录唯一约束、空目录删除走逻辑删除且不触碰存储介质。目录操作
 * 全程不产生真实文件 IO，物理路径正确性（元数据层）只能在此观测。
 * </p>
 * <p>
 * createDir 经 ORM 插入且 create_user 非空列依赖用户上下文填充，用例前置注入最小用户
 * 上下文（ID 取初始管理员）。自建数据按 name 唯一命名（it_dir_ 前缀）并物理清理。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class FileServiceIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long DEFAULT_STORAGE_ID = 1L;
    private static final Long ADMIN_USER_ID = 1L;

    @Autowired
    private FileService fileService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String probeDirName;

    @BeforeEach
    void setUpUserContext() {
        UserContext userContext = new UserContext();
        userContext.setId(ADMIN_USER_ID);
        userContext.setUsername("admin");
        UserContextHolder.setContext(userContext);
    }

    @AfterEach
    void removeProbeData() {
        UserContextHolder.clearContext();
        if (probeDirName != null) {
            jdbcTemplate.update("DELETE FROM sys_file WHERE name = ?", probeDirName);
            probeDirName = null;
        }
    }

    /**
     * 创建目录应落元数据记录：类型为目录、路径按 parentPath 拼接、归属默认存储
     */
    @Test
    void createDirShouldInsertDirectoryRecord() {
        probeDirName = "it_dir_" + System.nanoTime();
        AtomicReference<Long> dirIdRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID,
            () -> dirIdRef.set(fileService.createDir(buildFileReq())));

        Long dirId = dirIdRef.get();
        assertThat(dirId).as("目录创建应成功并返回 ID").isNotNull();
        Map<String, Object> row =
            jdbcTemplate.queryForMap("SELECT type, name, parent_path, path, storage_id "
                + "FROM sys_file WHERE id = ?", dirId);
        assertThat(row)
            .as("目录元数据应落库且与请求一致")
            .containsEntry("type", 0)
            .containsEntry("name", probeDirName)
            .containsEntry("parent_path", "/")
            .containsEntry("path", "/" + probeDirName);
        assertThat(((Number) row.get("storage_id")).longValue()).as("应归属默认存储")
            .isEqualTo(DEFAULT_STORAGE_ID);
    }

    /**
     * 重复创建同名目录应被拒绝且不产生新行
     */
    @Test
    void createDirShouldRejectDuplicate() {
        probeDirName = "it_dir_" + System.nanoTime();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> fileService.createDir(buildFileReq()));

        assertThatThrownBy(() -> TenantUtils.execute(DEFAULT_TENANT_ID,
            () -> fileService.createDir(buildFileReq())))
            .as("重复目录名应被拒绝")
            .isInstanceOf(BusinessException.class)
            .hasMessage("文件夹已存在");

        Long rowCount =
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_file WHERE name = ?", Long.class,
                probeDirName);
        assertThat(rowCount).as("唯一冲突不应产生新目录行").isEqualTo(1L);
    }

    /**
     * 删除空目录应仅做逻辑删除（deleted 置 1），不触碰存储介质
     */
    @Test
    void deleteEmptyDirShouldRemoveRecord() {
        probeDirName = "it_dir_" + System.nanoTime();
        jdbcTemplate.update(
            "INSERT INTO sys_file (name, original_name, parent_path, path, type, storage_id, "
                + "create_user, create_time, deleted, tenant_id) "
                + "VALUES (?, ?, '/', ?, 0, ?, 1, NOW(), 0, 0)",
            probeDirName, probeDirName, "/" + probeDirName, DEFAULT_STORAGE_ID);
        Long dirId = jdbcTemplate.queryForObject("SELECT id FROM sys_file WHERE name = ?",
            Long.class, probeDirName);

        TenantUtils.execute(DEFAULT_TENANT_ID, () -> fileService.delete(List.of(dirId)));

        Long deletedFlag =
            jdbcTemplate.queryForObject("SELECT deleted FROM sys_file WHERE id = ?", Long.class,
                dirId);
        // FileDO 字段级 @TableLogic(delval = "1") 覆盖全局 delval=id 配置，deleted=1 即进入回收站
        assertThat(deletedFlag).as("空目录删除应将 deleted 置为回收站标志 1").isEqualTo(1L);
    }

    /**
     * 构造根路径下的目录创建请求
     *
     * @return 创建请求
     */
    private FileReq buildFileReq() {
        FileReq req = new FileReq();
        req.setOriginalName(probeDirName);
        req.setParentPath("/");
        return req;
    }
}
