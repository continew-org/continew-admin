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
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.system.mapper.NoticeLogMapper;
import top.continew.admin.system.model.entity.NoticeLogDO;
import top.continew.admin.system.service.NoticeLogService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 公告日志业务实现
 *
 * @author Charles7c
 * @since 2025/5/18 19:15
 */
@Service
@RequiredArgsConstructor
public class NoticeLogServiceImpl implements NoticeLogService {

    private final NoticeLogMapper baseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(List<Long> userIds, Long noticeId) {
        // 检查是否有变更
        List<Long> oldUserIdList = baseMapper.lambdaQuery()
            .select(NoticeLogDO::getUserId)
            .eq(NoticeLogDO::getNoticeId, noticeId)
            .list()
            .stream()
            .map(NoticeLogDO::getUserId)
            .toList();
        Collection<Long> subtract = CollUtil.subtract(userIds, oldUserIdList);
        if (CollUtil.isEmpty(subtract)) {
            return false;
        }
        // 新增没有关联的
        LocalDateTime now = LocalDateTime.now();
        List<NoticeLogDO> list = subtract.stream().map(userId -> new NoticeLogDO(noticeId, userId, now)).toList();
        return baseMapper.insertBatch(list);
    }

    @Override
    public void deleteByNoticeIds(List<Long> noticeIds) {
        if (CollUtil.isEmpty(noticeIds)) {
            return;
        }
        baseMapper.lambdaUpdate().in(NoticeLogDO::getNoticeId, noticeIds).remove();
    }

    @Override
    public List<Long> listUserIdByNoticeId(Long noticeId) {
        return baseMapper.lambdaQuery()
            .select(NoticeLogDO::getUserId)
            .eq(NoticeLogDO::getNoticeId, noticeId)
            .list()
            .stream()
            .map(NoticeLogDO::getUserId)
            .toList();
    }
}
