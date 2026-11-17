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

package top.continew.admin.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.system.enums.NoticeMethodEnum;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.enums.NoticeStatusEnum;
import top.continew.admin.system.mapper.NoticeMapper;
import top.continew.admin.system.model.entity.NoticeDO;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.req.NoticeReq;
import top.continew.admin.system.service.MessageService;
import top.continew.admin.system.service.NoticeLogService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.data.mapper.BaseMapper;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告业务单元测试。
 *
 * <p>覆盖创建/更新前钩子的状态推导与已发布保护约束、更新后钩子的定时发布时间
 * 重置与消息发送、删除后钩子的日志清理与发布消息的模板填充。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    private MockedStatic<SpringUtil> springUtilMock;

    @Mock
    private NoticeMapper noticeMapper;
    @Mock
    private NoticeLogService noticeLogService;
    @Mock
    private MessageService messageService;

    @Spy
    @InjectMocks
    private NoticeServiceImpl noticeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(noticeService, "baseMapper", noticeMapper);
        // CheckUtils 的静态块经 SpringUtil 取 Bean 校验器，以本地 Validator 供给
        springUtilMock = Mockito.mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldKeepDraftStatusWhenCreatingDraft() {
        NoticeReq req = newNoticeReq();
        req.setStatus(NoticeStatusEnum.DRAFT);
        req.setIsTiming(true);

        noticeService.beforeCreate(req);

        assertEquals(NoticeStatusEnum.DRAFT, req.getStatus());
        assertNull(req.getPublishTime());
    }

    @Test
    void shouldMarkPendingWhenCreatingTimedNotice() {
        NoticeReq req = newNoticeReq();
        req.setStatus(NoticeStatusEnum.DRAFT);
        req.setIsTiming(true);
        req.setStatus(NoticeStatusEnum.PUBLISHED);

        noticeService.beforeCreate(req);

        assertEquals(NoticeStatusEnum.PENDING, req.getStatus());
        assertNull(req.getPublishTime());
    }

    @Test
    void shouldMarkPublishedWithPublishTimeWhenCreatingImmediateNotice() {
        NoticeReq req = newNoticeReq();
        // 表单提交「发布」意图时状态非草稿，终态由 isTiming 推导
        req.setStatus(NoticeStatusEnum.PUBLISHED);
        req.setIsTiming(false);

        noticeService.beforeCreate(req);

        assertEquals(NoticeStatusEnum.PUBLISHED, req.getStatus());
        assertNotNull(req.getPublishTime());
    }

    @Test
    void shouldRejectStatusChangeAfterPublished() {
        NoticeDO oldNotice = oldNotice(NoticeStatusEnum.PUBLISHED, false, NoticeScopeEnum.ALL);
        stubOldNotice(oldNotice);
        NoticeReq req = newNoticeReq();
        req.setStatus(NoticeStatusEnum.DRAFT);
        req.setIsTiming(false);
        req.setNoticeScope(NoticeScopeEnum.ALL);
        req.setNoticeMethods(List.of(NoticeMethodEnum.SYSTEM_MESSAGE.getValue()));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> noticeService.beforeUpdate(req, 1L));
        assertEquals("公告已发布，不允许修改状态", exception.getMessage());
    }

    @Test
    void shouldRejectTimingChangeAfterPublished() {
        NoticeDO oldNotice = oldNotice(NoticeStatusEnum.PUBLISHED, true, NoticeScopeEnum.ALL);
        stubOldNotice(oldNotice);
        NoticeReq req = newNoticeReq();
        req.setStatus(NoticeStatusEnum.PUBLISHED);
        req.setIsTiming(false);
        req.setNoticeScope(NoticeScopeEnum.ALL);
        req.setNoticeMethods(List.of(NoticeMethodEnum.SYSTEM_MESSAGE.getValue()));
        req.setPublishTime(oldNotice.getPublishTime());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> noticeService.beforeUpdate(req, 1L));
        assertEquals("公告已发布，不允许修改定时发布信息", exception.getMessage());
    }

    @Test
    void shouldRejectNoticeUsersChangeForUserScopeAfterPublished() {
        NoticeDO oldNotice = oldNotice(NoticeStatusEnum.PUBLISHED, false, NoticeScopeEnum.USER);
        oldNotice.setNoticeUsers(List.of("u1"));
        stubOldNotice(oldNotice);
        NoticeReq req = newNoticeReq();
        req.setStatus(NoticeStatusEnum.PUBLISHED);
        req.setIsTiming(false);
        req.setNoticeScope(NoticeScopeEnum.USER);
        req.setNoticeUsers(List.of("u2"));
        req.setNoticeMethods(List.of(NoticeMethodEnum.SYSTEM_MESSAGE.getValue()));
        req.setPublishTime(oldNotice.getPublishTime());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> noticeService.beforeUpdate(req, 1L));
        assertEquals("公告已发布，不允许修改通知用户", exception.getMessage());
    }

    @Test
    void shouldRejectNoticeMethodsChangeAfterPublished() {
        NoticeDO oldNotice = oldNotice(NoticeStatusEnum.PUBLISHED, false, NoticeScopeEnum.ALL);
        stubOldNotice(oldNotice);
        NoticeReq req = newNoticeReq();
        req.setStatus(NoticeStatusEnum.PUBLISHED);
        req.setIsTiming(false);
        req.setNoticeScope(NoticeScopeEnum.ALL);
        req.setNoticeMethods(List.of(NoticeMethodEnum.POPUP.getValue()));
        req.setPublishTime(oldNotice.getPublishTime());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> noticeService.beforeUpdate(req, 1L));
        assertEquals("公告已发布，不允许修改通知方式", exception.getMessage());
    }

    @Test
    void shouldKeepPublishedFieldsWhenUpdatingPublishedNotice() {
        LocalDateTime publishTime = LocalDateTime.now();
        NoticeDO oldNotice = oldNotice(NoticeStatusEnum.PUBLISHED, false, NoticeScopeEnum.ALL);
        oldNotice.setPublishTime(publishTime);
        stubOldNotice(oldNotice);
        NoticeReq req = newNoticeReq();
        req.setStatus(NoticeStatusEnum.PUBLISHED);
        req.setIsTiming(false);
        req.setNoticeScope(NoticeScopeEnum.ALL);
        req.setNoticeMethods(List.of(NoticeMethodEnum.SYSTEM_MESSAGE.getValue()));
        req.setPublishTime(publishTime.plusDays(1));

        noticeService.beforeUpdate(req, 1L);

        assertEquals(publishTime, req.getPublishTime());
    }

    @Test
    void shouldMarkPendingWhenDraftUpgradedToTimedPublish() {
        NoticeDO oldNotice = oldNotice(NoticeStatusEnum.DRAFT, false, NoticeScopeEnum.ALL);
        stubOldNotice(oldNotice);
        NoticeReq req = newNoticeReq();
        req.setStatus(NoticeStatusEnum.PUBLISHED);
        req.setIsTiming(true);

        noticeService.beforeUpdate(req, 1L);

        assertEquals(NoticeStatusEnum.PENDING, req.getStatus());
    }

    @Test
    void shouldResetResidualPublishTimeAfterNonTimedPendingUpdate() {
        // 非已发布且非定时的公告残留 publishTime 时，更新后钩子须清空该字段
        NoticeDO entity = oldNotice(NoticeStatusEnum.PENDING, false, NoticeScopeEnum.ALL);
        entity.setId(7L);
        entity.setPublishTime(LocalDateTime.now());
        LambdaUpdateChainWrapper<NoticeDO> update = Mockito.mock(LambdaUpdateChainWrapper.class);
        doReturn(update).when((BaseMapper) noticeMapper).lambdaUpdate();
        doReturn(update).when(update).set(any(), any());
        doReturn(update).when(update).eq(any(), any());
        when(update.update()).thenReturn(true);
        NoticeReq req = newNoticeReq();

        noticeService.afterUpdate(req, entity);

        verify(noticeMapper).lambdaUpdate();
        Mockito.verifyNoInteractions(messageService);
    }

    @Test
    void shouldSendMessagesAfterImmediatePublishUpdate() {
        NoticeDO entity = oldNotice(NoticeStatusEnum.PUBLISHED, false, NoticeScopeEnum.ALL);
        entity.setId(7L);
        entity.setNoticeUsers(List.of("u1", "u2"));
        entity.setNoticeMethods(List.of(NoticeMethodEnum.SYSTEM_MESSAGE.getValue()));
        NoticeReq req = newNoticeReq();

        noticeService.afterUpdate(req, entity);

        verify(messageService).add(any(MessageReq.class), eq(List.of("u1", "u2")));
    }

    @Test
    void shouldNotSendMessagesWhenDraftUpdated() {
        NoticeDO entity = oldNotice(NoticeStatusEnum.DRAFT, true, NoticeScopeEnum.ALL);
        entity.setId(7L);
        NoticeReq req = newNoticeReq();

        noticeService.afterUpdate(req, entity);

        verify(messageService, never()).add(any(MessageReq.class), anyList());
        verify(noticeMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldDeleteNoticeLogsAfterDelete() {
        noticeService.afterDelete(List.of(3L, 4L));

        verify(noticeLogService).deleteByNoticeIds(List.of(3L, 4L));
    }

    @Test
    void shouldSendFormattedMessageOnPublish() {
        NoticeDO notice = oldNotice(NoticeStatusEnum.PUBLISHED, false, NoticeScopeEnum.ALL);
        notice.setId(9L);
        notice.setTitle("系统维护");
        notice.setNoticeUsers(List.of("u1"));

        noticeService.publish(notice);

        org.mockito.ArgumentCaptor<MessageReq> captor = org.mockito.ArgumentCaptor
            .forClass(MessageReq.class);
        verify(messageService).add(captor.capture(), eq(List.of("u1")));
        MessageReq message = captor.getValue();
        assertEquals("您有一条新的公告", message.getTitle());
        assertEquals("公告《系统维护》已发布，请及时查看。", message.getContent());
        assertEquals("/user/notice?id=9", message.getPath());
    }

    @Test
    void shouldSkipPublishWhenSystemMessageMethodAbsent() {
        NoticeDO notice = oldNotice(NoticeStatusEnum.PUBLISHED, false, NoticeScopeEnum.ALL);
        notice.setNoticeMethods(List.of(NoticeMethodEnum.POPUP.getValue()));

        noticeService.publish(notice);

        Mockito.verifyNoInteractions(messageService);
    }

    private NoticeReq newNoticeReq() {
        return new NoticeReq();
    }

    private NoticeDO oldNotice(NoticeStatusEnum status, boolean isTiming, NoticeScopeEnum scope) {
        NoticeDO notice = new NoticeDO();
        notice.setId(1L);
        notice.setStatus(status);
        notice.setIsTiming(isTiming);
        notice.setNoticeScope(scope);
        notice.setNoticeMethods(List.of(NoticeMethodEnum.SYSTEM_MESSAGE.getValue()));
        notice.setPublishTime(LocalDateTime.now());
        return notice;
    }

    private void stubOldNotice(NoticeDO oldNotice) {
        doReturn(oldNotice).when(noticeService).getById(1L);
    }
}
