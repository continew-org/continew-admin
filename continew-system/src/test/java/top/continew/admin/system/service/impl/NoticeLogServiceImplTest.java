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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.system.mapper.NoticeLogMapper;
import top.continew.admin.system.model.entity.NoticeLogDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告日志业务单元测试。
 *
 * <p>覆盖通知用户关联的增量写入（全量新增/部分跳过/无增量短路）、按公告批量删除
 * 的空列表守卫与删除条件、按公告反查用户列表。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class NoticeLogServiceImplTest {

    private static final long NOTICE_ID = 5L;

    private NoticeLogMapper noticeLogMapper;
    private NoticeLogServiceImpl noticeLogService;
    private LocalDateTime testStart;

    @BeforeEach
    void setUp() {
        testStart = LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID);
        // 真实链式 wrapper 依赖实体元数据推导列名，须先注册 TableInfo
        TableInfoHelper
            .initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), NoticeLogDO.class
                .getName()), NoticeLogDO.class);
        noticeLogMapper = mock(NoticeLogMapper.class);
        noticeLogService = new NoticeLogServiceImpl(noticeLogMapper);
    }

    @AfterEach
    void tearDown() {
        TableInfoHelper.remove(NoticeLogDO.class);
    }

    @Test
    void shouldInsertAllUsersWhenNoExistingRelation() {
        stubExistingUserIds(List.of());
        when(noticeLogMapper.insertBatch(any())).thenReturn(true);

        boolean result = noticeLogService.add(List.of(1L, 2L), NOTICE_ID);

        assertTrue(result);
        ArgumentCaptor<List<NoticeLogDO>> captor = forNoticeLogs();
        verify(noticeLogMapper).insertBatch(captor.capture());
        List<Long> insertedUserIds =
            captor.getValue().stream().map(NoticeLogDO::getUserId).toList();
        assertEquals(List.of(1L, 2L), insertedUserIds);
        captor.getValue().forEach(log -> assertEquals(NOTICE_ID, log.getNoticeId()));
    }

    @Test
    void shouldInsertOnlyUsersWithoutExistingRelation() {
        stubExistingUserIds(List.of(1L));
        when(noticeLogMapper.insertBatch(any())).thenReturn(true);

        boolean result = noticeLogService.add(List.of(1L, 2L, 3L), NOTICE_ID);

        assertTrue(result);
        ArgumentCaptor<List<NoticeLogDO>> captor = forNoticeLogs();
        verify(noticeLogMapper).insertBatch(captor.capture());
        List<Long> insertedUserIds =
            captor.getValue().stream().map(NoticeLogDO::getUserId).toList();
        assertEquals(List.of(2L, 3L), insertedUserIds);
    }

    @Test
    void shouldReturnFalseWithoutInsertWhenAllUsersExist() {
        stubExistingUserIds(List.of(1L, 2L));

        boolean result = noticeLogService.add(List.of(1L, 2L), NOTICE_ID);

        assertFalse(result);
        verify(noticeLogMapper, never()).insertBatch(any());
    }

    @Test
    void shouldFillReadTimeOnInsert() {
        stubExistingUserIds(List.of());
        when(noticeLogMapper.insertBatch(any())).thenReturn(true);

        noticeLogService.add(List.of(9L), NOTICE_ID);

        ArgumentCaptor<List<NoticeLogDO>> captor = forNoticeLogs();
        verify(noticeLogMapper).insertBatch(captor.capture());
        // 已读时间取上海时区当前时刻，落在用例执行窗口内
        LocalDateTime readTime = captor.getValue().get(0).getReadTime();
        assertTrue(!readTime.isBefore(testStart), () -> "已读时间早于用例开始：" + readTime);
        assertTrue(
            readTime.isBefore(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID).plusSeconds(1)),
            () -> "已读时间晚于当前时刻：" + readTime);
    }

    @Test
    void shouldSkipDeleteWhenNoticeIdsEmpty() {
        noticeLogService.deleteByNoticeIds(List.of());

        verify(noticeLogMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldDeleteByNoticeIdCondition() {
        LambdaUpdateChainWrapper<NoticeLogDO> updateChain = realUpdateChain();
        doReturn(updateChain).when(noticeLogMapper).lambdaUpdate();
        when(noticeLogMapper.delete(any())).thenReturn(2);

        noticeLogService.deleteByNoticeIds(List.of(NOTICE_ID, 6L));

        verify(noticeLogMapper).delete(any());
        // 占位符数量与公告 ID 个数一致
        assertEquals("(notice_id IN (?,?))", updateChain.getWrapper().getTargetSql());
    }

    @Test
    void shouldListUserIdsByNoticeId() {
        LambdaQueryChainWrapper<NoticeLogDO> queryChain = realQueryChain();
        doReturn(queryChain).when(noticeLogMapper).lambdaQuery();
        when(noticeLogMapper.selectList(any()))
            .thenReturn(List.of(new NoticeLogDO(NOTICE_ID, 1L, null), new NoticeLogDO(
                NOTICE_ID, 2L, null)));

        List<Long> userIds = noticeLogService.listUserIdByNoticeId(NOTICE_ID);

        assertEquals(List.of(1L, 2L), userIds);
        assertEquals("(notice_id = ?)", queryChain.getWrapper().getTargetSql());
    }

    private void stubExistingUserIds(List<Long> existingUserIds) {
        LambdaQueryChainWrapper<NoticeLogDO> queryChain = realQueryChain();
        doReturn(queryChain).when(noticeLogMapper).lambdaQuery();
        when(noticeLogMapper.selectList(any())).thenReturn(existingUserIds.stream()
            .map(userId -> new NoticeLogDO(NOTICE_ID, userId, null))
            .toList());
    }

    private LambdaQueryChainWrapper<NoticeLogDO> realQueryChain() {
        // lambdaQuery 为接口 default 方法，打桩为真实调用后由 mock mapper 承接底层查询
        when(noticeLogMapper.currentEntityClass()).thenReturn((Class) NoticeLogDO.class);
        when(noticeLogMapper.lambdaQuery()).thenCallRealMethod();
        return noticeLogMapper.lambdaQuery();
    }

    private LambdaUpdateChainWrapper<NoticeLogDO> realUpdateChain() {
        when(noticeLogMapper.lambdaUpdate()).thenCallRealMethod();
        return noticeLogMapper.lambdaUpdate();
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<NoticeLogDO>> forNoticeLogs() {
        return ArgumentCaptor.forClass((Class) List.class);
    }
}
