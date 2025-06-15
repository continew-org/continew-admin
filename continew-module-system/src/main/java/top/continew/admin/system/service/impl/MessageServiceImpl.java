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

import cn.crane4j.annotation.AutoOperate;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.system.enums.MessageTypeEnum;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.mapper.MessageMapper;
import top.continew.admin.system.model.entity.MessageDO;
import top.continew.admin.system.model.query.MessageQuery;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.resp.message.MessageDetailResp;
import top.continew.admin.system.model.resp.message.MessageResp;
import top.continew.admin.system.model.resp.message.MessageTypeUnreadResp;
import top.continew.admin.system.model.resp.message.MessageUnreadResp;
import top.continew.admin.system.service.MessageLogService;
import top.continew.admin.system.service.MessageService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.messaging.websocket.util.WebSocketUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息业务实现
 *
 * @author Bull-BCLS
 * @author Charles7c
 * @since 2023/10/15 19:05
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper baseMapper;
    private final MessageLogService messageLogService;

    @Override
    @AutoOperate(type = MessageResp.class, on = "list")
    public PageResp<MessageResp> page(MessageQuery query, PageQuery pageQuery) {
        IPage<MessageResp> page = baseMapper.selectMessagePage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), query);
        return PageResp.build(page);
    }

    @Override
    public MessageDetailResp get(Long id) {
        return baseMapper.selectMessageById(id);
    }

    @Override
    public void readMessage(List<Long> ids, Long userId) {
        // 查询当前用户的未读消息
        List<MessageDO> list = baseMapper.selectUnreadListByUserId(userId);
        List<Long> unreadIds = list.stream().map(MessageDO::getId).toList();
        messageLogService.addWithUserId(CollUtil.isNotEmpty(ids)
            ? CollUtil.intersection(unreadIds, ids).stream().toList()
            : unreadIds, userId);
        WebSocketUtils.sendMessage(StpUtil.getTokenValueByLoginId(userId), String.valueOf(baseMapper
            .selectUnreadListByUserId(userId)
            .size()));
    }

    @Override
    public MessageUnreadResp countUnreadByUserId(Long userId, Boolean isDetail) {
        MessageUnreadResp result = new MessageUnreadResp();
        Long total = 0L;
        if (Boolean.TRUE.equals(isDetail)) {
            List<MessageTypeUnreadResp> detailList = new ArrayList<>();
            for (MessageTypeEnum messageType : MessageTypeEnum.values()) {
                MessageTypeUnreadResp resp = new MessageTypeUnreadResp();
                resp.setType(messageType);
                Long count = baseMapper.selectUnreadCountByUserIdAndType(userId, messageType.getValue());
                resp.setCount(count);
                detailList.add(resp);
                total += count;
            }
            result.setDetails(detailList);
        } else {
            total = baseMapper.selectUnreadCountByUserIdAndType(userId, null);
        }
        result.setTotal(total);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MessageReq req, List<String> userIdList) {
        MessageDO message = BeanUtil.copyProperties(req, MessageDO.class);
        message.setScope(CollUtil.isEmpty(userIdList) ? NoticeScopeEnum.ALL : NoticeScopeEnum.USER);
        message.setUsers(userIdList);
        baseMapper.insert(message);
        // 发送消息给指定在线用户
        if (CollUtil.isNotEmpty(userIdList)) {
            userIdList.parallelStream().forEach(userId -> {
                List<String> tokenList = StpUtil.getTokenValueListByLoginId(userId);
                tokenList.parallelStream().forEach(token -> WebSocketUtils.sendMessage(token, "1"));
            });
            return;
        }
        // 发送消息给所有在线用户
        WebSocketUtils.sendMessage("1");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
        messageLogService.deleteByMessageIds(ids);
    }
}