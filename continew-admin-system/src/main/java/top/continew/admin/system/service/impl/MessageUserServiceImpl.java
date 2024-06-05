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

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.enums.MessageTypeEnum;
import top.continew.admin.system.mapper.MessageUserMapper;
import top.continew.admin.system.model.entity.MessageUserDO;
import top.continew.admin.system.model.resp.MessageTypeUnreadResp;
import top.continew.admin.system.model.resp.MessageUnreadResp;
import top.continew.admin.system.service.MessageUserService;
import top.continew.starter.core.util.validate.CheckUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息和用户关联业务实现
 *
 * @author Bull-BCLS
 * @since 2023/10/15 19:05
 */
@Service
@RequiredArgsConstructor
public class MessageUserServiceImpl implements MessageUserService {

    private final MessageUserMapper baseMapper;

    @Override
    public MessageUnreadResp countUnreadMessageByUserId(Long userId, Boolean isDetail) {
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
    public void add(Long messageId, List<Long> userIdList) {
        CheckUtils.throwIfEmpty(userIdList, "消息接收人不能为空");
        List<MessageUserDO> messageUserList = userIdList.stream().map(userId -> {
            MessageUserDO messageUser = new MessageUserDO();
            messageUser.setUserId(userId);
            messageUser.setMessageId(messageId);
            messageUser.setIsRead(false);
            return messageUser;
        }).toList();
        baseMapper.insertBatch(messageUserList);
    }

    @Override
    public void readMessage(List<Long> ids) {
        baseMapper.lambdaUpdate()
            .set(MessageUserDO::getIsRead, true)
            .set(MessageUserDO::getReadTime, LocalDateTime.now())
            .eq(MessageUserDO::getIsRead, false)
            .in(CollUtil.isNotEmpty(ids), MessageUserDO::getMessageId, ids)
            .update();
    }

    @Override
    public void deleteByMessageIds(List<Long> messageIds) {
        baseMapper.lambdaUpdate().in(MessageUserDO::getMessageId, messageIds).remove();
    }
}