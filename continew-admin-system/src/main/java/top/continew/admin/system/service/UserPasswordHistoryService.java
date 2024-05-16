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

/**
 * 用户历史密码业务接口
 *
 * @author Charles7c
 * @since 2024/5/16 21:58
 */
public interface UserPasswordHistoryService {

    /**
     * 新增
     *
     * @param userId   用户 ID
     * @param password 密码
     * @param count    保留 N 个历史
     */
    void add(Long userId, String password, int count);

    /**
     * 密码是否为重复使用
     *
     * @param userId   用户 ID
     * @param password 密码
     * @param count    最近 N 次
     * @return 是否为重复使用
     */
    boolean isPasswordReused(Long userId, String password, int count);
}