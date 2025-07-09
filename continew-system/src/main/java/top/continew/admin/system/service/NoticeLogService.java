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
 * 公告日志业务接口
 *
 * @author Charles7c
 * @since 2025/5/18 19:12
 */
public interface NoticeLogService {

    /**
     * 新增
     *
     * @param userIds  用户 ID 列表
     * @param noticeId 公告 ID
     * @return 是否新增成功（true：成功；false：无变更/失败）
     */
    boolean add(List<Long> userIds, Long noticeId);

    /**
     * 根据公告 ID 列表删除
     *
     * @param noticeIds 公告 ID 列表
     */
    void deleteByNoticeIds(List<Long> noticeIds);

    /**
     * 根据公告 ID 查询用户 ID 列表
     *
     * @param noticeId 公告 ID
     * @return 用户 ID 列表
     */
    List<Long> listUserIdByNoticeId(Long noticeId);
}