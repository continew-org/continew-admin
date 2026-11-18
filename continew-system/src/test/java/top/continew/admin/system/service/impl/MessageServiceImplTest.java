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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.mapper.MessageMapper;
import top.continew.admin.system.model.entity.MessageDO;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.resp.message.MessageDetailResp;
import top.continew.admin.system.model.resp.message.MessageUnreadResp;
import top.continew.admin.system.service.MessageLogService;
import top.continew.starter.messaging.websocket.util.WebSocketUtils;

import java.util.List;

/**
 * 消息业务单元测试。
 *
 * <p>覆盖未读数统计（总数与分类型明细）、消息创建的范围判定与推送、
 * 指定用户已读联动与越权删除防护。Mapper、消息日志服务与 WebSocket/Token
 * 静态依赖均以 Mockito 模拟。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class MessageServiceImplTest {

    private MessageMapper messageMapper;
    private MessageLogService messageLogService;
    private MessageServiceImpl messageService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<StpUtil> stpUtilMock;
    private MockedStatic<WebSocketUtils> webSocketUtilsMock;

    @BeforeEach
    void setUp() {
        // 初始化 MessageDO 的 MyBatis-Plus Lambda 列缓存，避免构造 LambdaQueryWrapper 时找不到实体元数据
        MapperBuilderAssistant assistant =
            new MapperBuilderAssistant(new MybatisConfiguration(), MessageDO.class.getName());
        TableInfoHelper.initTableInfo(assistant, MessageDO.class);
        messageMapper = mock(MessageMapper.class);
        messageLogService = mock(MessageLogService.class);
        messageService = new MessageServiceImpl(messageMapper, messageLogService);
        // StpUtil 与 WebSocketUtils 的静态初始化经 SpringUtil 取容器 Bean，先桩 SpringUtil 再模拟二者
        springUtilMock = mockStatic(SpringUtil.class);
        stpUtilMock = mockStatic(StpUtil.class);
        webSocketUtilsMock = mockStatic(WebSocketUtils.class);
    }

    @AfterEach
    void tearDown() {
        webSocketUtilsMock.close();
        stpUtilMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldGetMessageById() {
        MessageDetailResp detail = new MessageDetailResp();
        when(messageMapper.selectMessageById(9L)).thenReturn(detail);

        assertEquals(detail, messageService.get(9L));
    }

    @Test
    void shouldCountUnreadTotalWithoutDetail() {
        when(messageMapper.selectUnreadCountByUserIdAndType(1L, null)).thenReturn(7L);

        MessageUnreadResp resp = messageService.countUnreadByUserId(1L, false);

        assertEquals(7L, resp.getTotal());
        // 非明细模式不逐类型查询
        verify(messageMapper).selectUnreadCountByUserIdAndType(1L, null);
        verify(messageMapper, never()).selectUnreadCountByUserIdAndType(1L, 1);
    }

    @Test
    void shouldCountUnreadDetailPerType() {
        when(messageMapper.selectUnreadCountByUserIdAndType(1L, 1)).thenReturn(2L);
        when(messageMapper.selectUnreadCountByUserIdAndType(1L, 2)).thenReturn(3L);

        MessageUnreadResp resp = messageService.countUnreadByUserId(1L, true);

        assertEquals(5L, resp.getTotal());
        assertEquals(2, resp.getDetails().size());
    }

    @Test
    void shouldReadAllUnreadWhenIdsEmpty() {
        MessageDO unread = new MessageDO();
        unread.setId(5L);
        when(messageMapper.selectUnreadListByUserId(1L)).thenReturn(List.of(unread), List.of());
        stpUtilMock.when(() -> StpUtil.getTokenValueListByLoginId(1L)).thenReturn(List.of("t1"));

        messageService.readMessage(List.of(), 1L);

        verify(messageLogService).addWithUserId(List.of(5L), 1L);
        webSocketUtilsMock.verify(() -> WebSocketUtils.sendMessageToAll("t1", "0"));
    }

    @Test
    void shouldReadIntersectionOfRequestedAndUnread() {
        MessageDO unreadFirst = new MessageDO();
        unreadFirst.setId(5L);
        MessageDO unreadSecond = new MessageDO();
        unreadSecond.setId(6L);
        when(messageMapper.selectUnreadListByUserId(1L)).thenReturn(List.of(unreadFirst,
            unreadSecond), List.of());
        stpUtilMock.when(() -> StpUtil.getTokenValueListByLoginId(1L)).thenReturn(List.of("t1"));

        // 请求已读 6/7：7 不在未读清单中不落日志，6 命中
        messageService.readMessage(List.of(6L, 7L), 1L);

        verify(messageLogService).addWithUserId(List.of(6L), 1L);
    }

    @Test
    void shouldSaveUserScopedMessageAndNotifyRecipients() {
        MessageReq req = new MessageReq(top.continew.admin.system.enums.MessageTypeEnum.SYSTEM);
        req.setTitle("标题");
        req.setContent("内容");
        stpUtilMock.when(() -> StpUtil.getTokenValueListByLoginId("u1"))
            .thenReturn(List.of("t1", "t2"));

        messageService.add(req, List.of("u1"));

        ArgumentCaptor<MessageDO> captor = ArgumentCaptor.forClass(MessageDO.class);
        verify(messageMapper).insert(captor.capture());
        assertEquals(NoticeScopeEnum.USER, captor.getValue().getScope());
        assertEquals(List.of("u1"), captor.getValue().getUsers());
        webSocketUtilsMock.verify(() -> WebSocketUtils.sendMessageToAll("t1", "1"));
        webSocketUtilsMock.verify(() -> WebSocketUtils.sendMessageToAll("t2", "1"));
        webSocketUtilsMock.verify(() -> WebSocketUtils.sendMessage("1"), never());
    }

    @Test
    void shouldBroadcastToAllWhenNoRecipients() {
        MessageReq req = new MessageReq(top.continew.admin.system.enums.MessageTypeEnum.SYSTEM);
        req.setTitle("全员公告");
        req.setContent("内容");

        messageService.add(req, List.of());

        ArgumentCaptor<MessageDO> captor = ArgumentCaptor.forClass(MessageDO.class);
        verify(messageMapper).insert(captor.capture());
        assertEquals(NoticeScopeEnum.ALL, captor.getValue().getScope());
        webSocketUtilsMock.verify(() -> WebSocketUtils.sendMessage("1"));
    }

    @Test
    void shouldDeleteOnlyOwnMessages() {
        MessageDO allScope = new MessageDO();
        allScope.setId(1L);
        allScope.setScope(NoticeScopeEnum.ALL);
        MessageDO otherUser = new MessageDO();
        otherUser.setId(2L);
        otherUser.setScope(NoticeScopeEnum.USER);
        otherUser.setUsers(List.of("999"));
        MessageDO ownScoped = new MessageDO();
        ownScoped.setId(3L);
        ownScoped.setScope(NoticeScopeEnum.USER);
        ownScoped.setUsers(List.of("7"));
        when(messageMapper.selectList(any())).thenReturn(List.of(allScope, otherUser, ownScoped));

        messageService.delete(List.of(1L, 2L, 3L), 7L);

        verify(messageMapper).deleteByIds(List.of(1L, 3L));
        verify(messageLogService).deleteByMessageIds(List.of(1L, 3L));
    }

    @Test
    void shouldSkipDeleteWhenNoOwnMessages() {
        MessageDO otherUser = new MessageDO();
        otherUser.setId(2L);
        otherUser.setScope(NoticeScopeEnum.USER);
        otherUser.setUsers(List.of("999"));
        when(messageMapper.selectList(any())).thenReturn(List.of(otherUser));

        messageService.delete(List.of(2L), 7L);

        verify(messageMapper, never()).deleteByIds(anyList());
        Mockito.verifyNoInteractions(messageLogService);
    }

    @Test
    void shouldSkipDeleteWhenIdsEmpty() {
        messageService.delete(List.of(), 7L);

        verify(messageMapper, never()).selectList(any());
    }

    @Test
    void shouldCopyPropertiesFromRequest() {
        MessageReq req = new MessageReq(top.continew.admin.system.enums.MessageTypeEnum.SYSTEM);
        req.setTitle("复制校验");
        req.setContent("内容主体");

        messageService.add(req, List.of());

        ArgumentCaptor<MessageDO> captor = ArgumentCaptor.forClass(MessageDO.class);
        verify(messageMapper).insert(captor.capture());
        assertEquals("复制校验", captor.getValue().getTitle());
        assertEquals("内容主体", captor.getValue().getContent());
        webSocketUtilsMock.verify(() -> WebSocketUtils.sendMessage("1"));
        stpUtilMock.verify(() -> StpUtil.getTokenValueListByLoginId(any()), never());
    }
}
