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
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.system.mapper.MessageMapper;
import top.continew.admin.system.model.entity.MessageDO;
import top.continew.admin.system.model.query.MessageQuery;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.resp.MessageResp;
import top.continew.admin.system.service.MessageService;
import top.continew.admin.system.service.MessageUserService;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.data.mybatis.plus.query.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 消息业务实现
 *
 * @author Bull-BCLS
 * @since 2023/10/15 19:05
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper baseMapper;
    private final MessageUserService messageUserService;

    @Override
    @AutoOperate(type = MessageResp.class, on = "list")
    public PageResp<MessageResp> page(MessageQuery query, PageQuery pageQuery) {
        QueryWrapper<MessageDO> queryWrapper = QueryWrapperHelper.build(query);
        queryWrapper.apply(null != query.getUserId(), "t2.user_id={0}", query.getUserId())
            .apply(null != query.getIsRead(), "t2.is_read={0}", query.getIsRead());
        IPage<MessageResp> page = baseMapper.selectPageByUserId(pageQuery.toPage(), queryWrapper);
        return PageResp.build(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MessageReq req, List<Long> userIdList) {
        CheckUtils.throwIf(() -> CollUtil.isEmpty(userIdList), "消息接收人不能为空");
        MessageDO message = BeanUtil.copyProperties(req, MessageDO.class);
        baseMapper.insert(message);
        messageUserService.add(message.getId(), userIdList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteBatchIds(ids);
        messageUserService.deleteByMessageIds(ids);
    }
}