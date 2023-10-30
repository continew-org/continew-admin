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

package top.charles7c.cnadmin.system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.collection.CollUtil;

import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.MessageUserMapper;
import top.charles7c.cnadmin.system.model.entity.MessageUserDO;
import top.charles7c.cnadmin.system.service.MessageUserService;

/**
 * 消息和用户关联业务实现
 *
 * @author BULL_BCLS
 * @since 2023/10/15 19:05
 */
@Service
@RequiredArgsConstructor
public class MessageUserServiceImpl implements MessageUserService {

    private final MessageUserMapper messageUserMapper;

    @Override
    public void add(Long messageId, List<Long> userIdList) {
        CheckUtils.throwIf(() -> CollUtil.isEmpty(userIdList), "消息接收人不能为空");
        List<MessageUserDO> messageUserDOList = userIdList.stream().map(userId -> {
            MessageUserDO messageUserDO = new MessageUserDO();
            messageUserDO.setUserId(userId);
            messageUserDO.setMessageId(messageId);
            messageUserDO.setReadStatus(false);
            return messageUserDO;
        }).collect(Collectors.toList());
        messageUserMapper.insertBatch(messageUserDOList);
    }

    @Override
    public void readMessage(List<Long> ids) {
        messageUserMapper.lambdaUpdate().set(MessageUserDO::getReadStatus, true)
            .set(MessageUserDO::getReadTime, LocalDateTime.now()).eq(MessageUserDO::getReadStatus, false)
            .in(CollUtil.isNotEmpty(ids), MessageUserDO::getMessageId, ids).update();
    }

    @Override
    public void delete(List<Long> ids) {
        if (CollUtil.isNotEmpty(ids)) {
            messageUserMapper.delete(Wrappers.<MessageUserDO>lambdaQuery().in(MessageUserDO::getMessageId, ids));
        }
    }
}