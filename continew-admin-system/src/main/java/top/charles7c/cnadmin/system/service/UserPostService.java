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
 * 用户和岗位业务接口
 *
 * @author Charles7c
 * @since 2023/2/25 22:40
 */
public interface UserPostService {

    /**
     * 保存
     *
     * @param postIds
     *            岗位 ID 列表
     * @param userId
     *            用户 ID
     */
    void save(List<Long> postIds, Long userId);

    /**
     * 根据岗位 ID 列表查询
     *
     * @param postIds
     *            岗位 ID 列表
     * @return 总记录数
     */
    Long countByPostIds(List<Long> postIds);

    /**
     * 根据用户 ID 查询
     *
     * @param userId
     *            用户 ID
     * @return 岗位 ID 列表
     */
    List<Long> listPostIdsByUserId(Long userId);
}