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

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.enums.MessageTypeEnum;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.enums.NoticeStatusEnum;
import top.continew.admin.system.model.query.NoticeQuery;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.req.NoticeReq;
import top.continew.admin.system.model.resp.notice.NoticeResp;
import top.continew.admin.system.service.MessageService;
import top.continew.admin.system.service.NoticeService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 公告 Service 集成测试
 *
 * <p>
 * 聚焦黑盒接口覆盖不到的服务层点：发布状态公告创建时同步级联写入系统消息（消息 title 携带
 * 公告标题，HTTP 层看不到写入过程）、删除后业务查询不可见且公告日志被物理清理、消息服务的
 * 指定用户写入路径可独立落库。公告删除为逻辑删除（deleted 置为自身 ID），公告日志为物理删除。
 * </p>
 * <p>
 * 探针公告按 title 唯一命名（qa_ 前缀 + nanoTime）并在用例后按消息日志、消息、公告日志、
 * 公告的顺序物理清理；探针消息按 title 唯一命名独立清理，不触碰任何内置数据。写操作处于
 * 种子超管登录态（create_user 非空列依赖用户上下文填充），探针数据均为自建行。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
class NoticeServiceIntegrationTest extends BaseIntegrationTest {

    private static final Long DEFAULT_TENANT_ID = 0L;
    private static final Long ADMIN_USER_ID = 1L;
    private static final String PROBE_NOTICE_TITLE = "qa_notice_it_" + System.nanoTime();
    private static final String PROBE_MESSAGE_TITLE = "qa_message_it_" + System.nanoTime();

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SaTokenContext originalSaTokenContext;

    @BeforeEach
    void setUpUserContext() {
        // create 的 insertFill 依赖用户上下文填充 create_user（非空列），登录种子超管
        originalSaTokenContext = SaManager.getSaTokenContext();
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());
        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(ADMIN_USER_ID);
        UserContext operator = new UserContext();
        operator.setId(ADMIN_USER_ID);
        operator.setUsername("admin");
        operator.setRoleCodes(Set.of("super_admin"));
        UserContextHolder.setContext(operator, false);
    }

    @AfterEach
    void removeProbeData() {
        StpUtil.logout(ADMIN_USER_ID);
        SaTokenContextMockUtil.clearContext();
        SaManager.setSaTokenContext(originalSaTokenContext);
        UserContextHolder.clearContext();
        jdbcTemplate.update("DELETE FROM sys_message_log WHERE message_id IN "
            + "(SELECT id FROM sys_message WHERE title = ?)", PROBE_MESSAGE_TITLE);
        jdbcTemplate.update("DELETE FROM sys_message WHERE title = ?", PROBE_MESSAGE_TITLE);
        jdbcTemplate.update("DELETE FROM sys_notice_log WHERE notice_id IN "
            + "(SELECT id FROM sys_notice WHERE title = ?)", PROBE_NOTICE_TITLE);
        jdbcTemplate.update("DELETE FROM sys_notice WHERE title = ?", PROBE_NOTICE_TITLE);
    }

    /**
     * 创建已发布公告应级联发送系统消息，消息内容携带公告标题且独立可查
     */
    @Test
    void createPublishedNoticeShouldCascadeSystemMessage() {
        AtomicReference<Long> noticeIdRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> noticeIdRef.set(noticeService
            .create(buildPublishedNoticeReq())));
        Long noticeId = noticeIdRef.get();
        assertThat(noticeId).as("公告创建应成功并返回 ID").isNotNull();

        // NOTICE_PUBLISH 模板：title 固定为「您有一条新的公告」，公告标题进 content
        Long messageCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_message WHERE content LIKE CONCAT('%', ?, '%') "
                + "AND title = '您有一条新的公告'",
            Long.class, PROBE_NOTICE_TITLE);
        assertThat(messageCount).as("发布公告应级联写入系统消息").isEqualTo(1L);

        Object noticeStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM sys_notice WHERE id = ?", Object.class, noticeId);
        assertThat(((Number) noticeStatus).intValue()).as("公告应落为已发布状态").isEqualTo(3);
        Long noticeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_notice WHERE id = ? AND deleted = 0", Long.class, noticeId);
        assertThat(noticeCount).as("公告行应保留且未删除").isEqualTo(1L);
    }

    /**
     * 删除公告后业务查询不可见、表行保留（deleted 置为自身 ID）且公告日志被物理清理
     */
    @Test
    void deleteShouldHideNoticeAndRemoveLogRows() {
        AtomicReference<Long> noticeIdRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> noticeIdRef.set(noticeService
            .create(buildPublishedNoticeReq())));
        Long noticeId = noticeIdRef.get();
        jdbcTemplate.update("INSERT INTO sys_notice_log (notice_id, user_id, read_time) "
            + "VALUES (?, ?, NOW())", noticeId, ADMIN_USER_ID);

        TenantUtils.execute(DEFAULT_TENANT_ID, () -> noticeService.delete(List.of(noticeId)));

        Long deletedFlag = jdbcTemplate.queryForObject(
            "SELECT deleted FROM sys_notice WHERE id = ?", Long.class, noticeId);
        assertThat(deletedFlag).as("逻辑删除应将 deleted 置为自身 ID").isEqualTo(noticeId);

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(1);
        pageQuery.setSize(100);
        AtomicReference<BasePageResp<NoticeResp>> pageRef = new AtomicReference<>();
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> pageRef.set(noticeService
            .page(new NoticeQuery(), pageQuery)));
        assertThat(pageRef.get().getList()).as("删除后公告列表不应返回该公告")
            .noneMatch(resp -> resp.getId().equals(noticeId));

        Long logCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_notice_log WHERE notice_id = ?", Long.class, noticeId);
        assertThat(logCount).as("公告删除应物理清理公告日志").isZero();
    }

    /**
     * 系统消息按探针标题写入指定用户可查（消息服务直连验证，作为公告级联的锚点）
     */
    @Test
    void messageAddShouldInsertRowForProbeTitle() {
        MessageReq req = new MessageReq(MessageTypeEnum.SYSTEM);
        req.setTitle(PROBE_MESSAGE_TITLE);
        req.setContent("qa_msg_content");
        TenantUtils.execute(DEFAULT_TENANT_ID, () -> messageService.add(req, List
            .of(String.valueOf(ADMIN_USER_ID))));

        Long messageCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_message WHERE title = ?", Long.class, PROBE_MESSAGE_TITLE);
        assertThat(messageCount).as("指定用户的系统消息应落库").isEqualTo(1L);
    }

    /**
     * 构造已发布状态、通知所有人的公告创建请求
     *
     * @return 创建请求
     */
    private NoticeReq buildPublishedNoticeReq() {
        NoticeReq req = new NoticeReq();
        req.setTitle(PROBE_NOTICE_TITLE);
        req.setContent("qa_notice_content_" + System.nanoTime());
        req.setType("1");
        req.setNoticeScope(NoticeScopeEnum.ALL);
        req.setNoticeMethods(List.of(1));
        req.setIsTiming(false);
        req.setStatus(NoticeStatusEnum.PUBLISHED);
        req.setPublishTime(LocalDateTime.now());
        return req;
    }
}
