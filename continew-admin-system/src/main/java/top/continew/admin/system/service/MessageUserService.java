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

package top.continew.admin.system.service;

import java.util.List;

import top.continew.admin.system.model.resp.MessageUnreadResp;

/**
 * 消息和用户关联业务接口
 *
 * @author Bull-BCLS
 * @since 2023/10/15 19:05
 */
public interface MessageUserService {

    /**
     * 根据用户 ID 查询未读消息数量
     *
     * @param userId   用户 ID
     * @param isDetail 是否查询详情
     * @return 未读消息信息
     */
    MessageUnreadResp countUnreadMessageByUserId(Long userId, Boolean isDetail);

    /**
     * 新增
     *
     * @param messageId  消息 ID
     * @param userIdList 用户 ID 列表
     */
    void add(Long messageId, List<Long> userIdList);

    /**
     * 将消息标记已读
     *
     * @param ids 消息ID（为空则将所有消息标记已读）
     */
    void readMessage(List<Long> ids);

    /**
     * 根据消息 ID 删除
     *
     * @param messageIds 消息 ID 列表
     */
    void deleteByMessageIds(List<Long> messageIds);
}