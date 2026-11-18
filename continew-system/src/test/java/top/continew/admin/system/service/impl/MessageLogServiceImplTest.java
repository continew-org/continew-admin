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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.continew.admin.system.mapper.MessageLogMapper;
import top.continew.admin.system.model.entity.MessageLogDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息日志业务单元测试。
 *
 * <p>覆盖已读记录的按用户批量写入与空列表守卫、按消息批量删除的空列表守卫
 * 与删除条件。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class MessageLogServiceImplTest {

    private static final long USER_ID = 9L;

    private MessageLogMapper messageLogMapper;
    private MessageLogServiceImpl messageLogService;

    @BeforeEach
    void setUp() {
        // 真实链式 wrapper 依赖实体元数据推导列名，须先注册 TableInfo
        TableInfoHelper
            .initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), MessageLogDO.class
                .getName()), MessageLogDO.class);
        messageLogMapper = mock(MessageLogMapper.class);
        messageLogService = new MessageLogServiceImpl(messageLogMapper);
    }

    @AfterEach
    void tearDown() {
        TableInfoHelper.remove(MessageLogDO.class);
    }

    @Test
    void shouldInsertReadLogsForEachMessage() {
        when(messageLogMapper.insert(anyCollection())).thenReturn(List.of());

        messageLogService.addWithUserId(List.of(1L, 2L, 3L), USER_ID);

        ArgumentCaptor<List<MessageLogDO>> captor = forMessageLogs();
        verify(messageLogMapper).insert(captor.capture());
        List<Long> messageIds = captor.getValue().stream().map(MessageLogDO::getMessageId).toList();
        assertEquals(List.of(1L, 2L, 3L), messageIds);
        captor.getValue().forEach(log -> {
            assertEquals(USER_ID, log.getUserId());
            assertTrue(log.getReadTime() instanceof LocalDateTime, "已读时间必须写入");
        });
    }

    @Test
    void shouldSkipInsertWhenMessageIdsEmpty() {
        messageLogService.addWithUserId(List.of(), USER_ID);

        verify(messageLogMapper, never()).insert(anyCollection());
    }

    @Test
    void shouldSkipDeleteWhenMessageIdsEmpty() {
        messageLogService.deleteByMessageIds(List.of());

        verify(messageLogMapper, never()).lambdaUpdate();
    }

    @Test
    void shouldDeleteByMessageIdCondition() {
        LambdaUpdateChainWrapper<MessageLogDO> updateChain = realUpdateChain();
        doReturn(updateChain).when(messageLogMapper).lambdaUpdate();
        when(messageLogMapper.delete(any())).thenReturn(1);

        messageLogService.deleteByMessageIds(List.of(7L, 8L));

        verify(messageLogMapper).delete(any());
        assertEquals("(message_id IN (?,?))", updateChain.getWrapper().getTargetSql());
    }

    private LambdaUpdateChainWrapper<MessageLogDO> realUpdateChain() {
        when(messageLogMapper.lambdaUpdate()).thenCallRealMethod();
        return messageLogMapper.lambdaUpdate();
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<MessageLogDO>> forMessageLogs() {
        return ArgumentCaptor.forClass((Class) List.class);
    }
}
