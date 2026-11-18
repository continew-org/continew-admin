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

package top.continew.admin.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import top.continew.admin.system.enums.NoticeMethodEnum;
import top.continew.admin.system.enums.NoticeStatusEnum;
import top.continew.admin.system.mapper.NoticeMapper;
import top.continew.admin.system.model.entity.NoticeDO;
import top.continew.admin.system.service.NoticeService;
import top.continew.starter.data.mapper.BaseMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告发布任务单元测试。
 *
 * <p>覆盖待发布公告的查询、系统消息公告的顺序发送与状态批量更新；
 * 无待发布数据时不触发发送与更新。公告 Mapper 与公告服务经 SpringUtil 获取，统一静态模拟。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class NoticePublishJobTest {

    private NoticeMapper noticeMapper;
    private NoticeService noticeService;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        noticeMapper = mock(NoticeMapper.class);
        noticeService = mock(NoticeService.class);
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(NoticeMapper.class)).thenReturn(noticeMapper);
        springUtilMock.when(() -> SpringUtil.getBean(NoticeService.class))
            .thenReturn(noticeService);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldSendSystemMessageAndUpdateStatusWhenPendingNoticesExist() {
        NoticeDO notice = new NoticeDO();
        notice.setId(9L);
        notice.setStatus(NoticeStatusEnum.PENDING);
        notice.setNoticeMethods(List.of(NoticeMethodEnum.SYSTEM_MESSAGE.getValue()));
        stubPendingQuery(List.of(notice));
        stubStatusUpdate(true);

        NoticePublishJob.Scheduler scheduler = new NoticePublishJob.Scheduler();
        scheduler.publishNoticeWithSchedule();

        verify(noticeService).publish(notice);
        verify(noticeMapper).lambdaUpdate();
    }

    @Test
    void shouldSkipSendingWhenNoticeHasNoSystemMessageMethod() {
        NoticeDO notice = new NoticeDO();
        notice.setId(8L);
        notice.setStatus(NoticeStatusEnum.PENDING);
        notice.setNoticeMethods(List.of(NoticeMethodEnum.POPUP.getValue()));
        stubPendingQuery(List.of(notice));
        stubStatusUpdate(true);

        NoticePublishJob.Scheduler scheduler = new NoticePublishJob.Scheduler();
        scheduler.publishNoticeWithSchedule();

        verify(noticeService, never()).publish(any());
        verify(noticeMapper).lambdaUpdate();
    }

    @Test
    void shouldDoNothingWhenNoPendingNotices() {
        stubPendingQuery(List.of());

        NoticePublishJob.ScheduleJob scheduleJob = new NoticePublishJob.ScheduleJob();
        scheduleJob.publishNoticeWithScheduleJob();

        verify(noticeService, never()).publish(any());
        verify(noticeMapper, never()).lambdaUpdate();
    }

    /**
     * 桩定待发布公告查询链。
     *
     * @param notices 查询返回的待发布公告列表
     */
    @SuppressWarnings("unchecked")
    private void stubPendingQuery(List<NoticeDO> notices) {
        LambdaQueryChainWrapper<NoticeDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when((BaseMapper<NoticeDO>) noticeMapper).lambdaQuery();
        doReturn(chain).when(chain).eq(any(), any());
        doReturn(chain).when(chain).le(any(), any(LocalDateTime.class));
        when(chain.list()).thenReturn(notices);
    }

    /**
     * 桩定状态批量更新链。
     *
     * @param updateResult 更新终止方法返回值
     */
    @SuppressWarnings("unchecked")
    private void stubStatusUpdate(boolean updateResult) {
        LambdaUpdateChainWrapper<NoticeDO> chain = mock(LambdaUpdateChainWrapper.class);
        doReturn(chain).when((BaseMapper<NoticeDO>) noticeMapper).lambdaUpdate();
        doReturn(chain).when(chain).set(any(), any());
        doReturn(chain).when(chain).in(any(), Mockito.anyCollection());
        when(chain.update()).thenReturn(updateResult);
    }
}
