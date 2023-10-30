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

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.hutool.core.collection.CollUtil;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.query.SortQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.helper.QueryHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.MessageMapper;
import top.charles7c.cnadmin.system.model.entity.MessageDO;
import top.charles7c.cnadmin.system.model.query.MessageQuery;
import top.charles7c.cnadmin.system.model.request.MessageRequest;
import top.charles7c.cnadmin.system.model.vo.MessageVO;
import top.charles7c.cnadmin.system.service.MessageService;
import top.charles7c.cnadmin.system.service.MessageUserService;

/**
 * 消息业务实现
 *
 * @author BULL_BCLS
 * @since 2023/10/15 19:05
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl
    extends BaseServiceImpl<MessageMapper, MessageDO, MessageVO, MessageVO, MessageQuery, MessageRequest>
    implements MessageService {

    private final MessageUserService messageUserService;

    @Override
    public PageDataVO<MessageVO> page(MessageQuery query, PageQuery pageQuery) {
        QueryWrapper<MessageDO> queryWrapper = QueryHelper.build(query);
        queryWrapper.apply(null != query.getUid(), "msgUser.user_id={0}", query.getUid());
        queryWrapper.apply(null != query.getReadStatus(), "msgUser.read_status={0}", query.getReadStatus());
        IPage<MessageVO> page = baseMapper.selectVoPage(pageQuery.toPage(), queryWrapper);
        page.getRecords().forEach(this::fill);
        return PageDataVO.build(page);
    }

    @Override
    public List<MessageVO> list(MessageQuery query, SortQuery sortQuery) {
        QueryWrapper<MessageDO> queryWrapper = QueryHelper.build(query);
        queryWrapper.apply("msgUser.user_id={0}", LoginHelper.getUserId());
        queryWrapper.apply(null != query.getReadStatus(), "msgUser.read_status={0}", query.getReadStatus());
        // 设置排序
        this.sort(queryWrapper, sortQuery);
        return baseMapper.selectVoList(queryWrapper);
    }

    @Override
    public MessageVO get(Long id) {
        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setId(id);
        PageDataVO<MessageVO> page = this.page(messageQuery, new PageQuery());
        List<MessageVO> messageVOList = page.getList();
        if (CollUtil.isEmpty(messageVOList)) {
            return new MessageVO();
        }
        MessageVO messageVO = messageVOList.get(0);
        messageUserService.readMessage(Collections.singletonList(messageVO.getId()));
        return messageVO;
    }

    @Override
    public void add(MessageRequest request, List<Long> userIdList) {
        CheckUtils.throwIf(() -> CollUtil.isEmpty(userIdList), "消息接收人不能为空");
        Long messageId = super.add(request);
        messageUserService.add(messageId, userIdList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<Long> ids) {
        super.delete(ids);
        messageUserService.delete(ids);
    }
}