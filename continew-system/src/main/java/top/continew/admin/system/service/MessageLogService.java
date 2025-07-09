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

/**
 * 消息日志业务接口
 *
 * @author Bull-BCLS
 * @author Charles7c
 * @since 2023/10/15 19:05
 */
public interface MessageLogService {

    /**
     * 新增
     *
     * @param messageIds 消息 ID 列表
     * @param userId     用户 ID
     */
    void addWithUserId(List<Long> messageIds, Long userId);

    /**
     * 根据消息 ID 删除
     *
     * @param messageIds 消息 ID 列表
     */
    void deleteByMessageIds(List<Long> messageIds);
}