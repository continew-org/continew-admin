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

package top.charles7c.cnadmin.system.service;

import java.util.List;

/**
 * 消息和用户关联业务接口
 *
 * @author BULL_BCLS
 * @since 2023/10/15 19:05
 */
public interface MessageUserService {

    /**
     * 发送消息
     *
     * @param messageId
     *            消息ID
     * @param userIdList
     *            接收人
     */
    void add(Long messageId, List<Long> userIdList);

    /**
     * 将消息标记已读
     *
     * @param ids
     *            消息ID（为空则将所有消息标记已读）
     */
    void readMessage(List<Long> ids);

    /**
     * 删除消息
     *
     * @param ids
     *            消息ID
     */
    void delete(List<Long> ids);
}