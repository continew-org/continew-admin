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

import top.continew.admin.system.model.entity.UserSocialDO;

import me.zhyd.oauth.model.AuthUser;

/**
 * 用户社会化关联业务接口
 *
 * @author Charles7c
 * @since 2023/10/11 22:10
 */
public interface UserSocialService {

    /**
     * 根据来源和开放 ID 查询
     *
     * @param source 来源
     * @param openId 开放 ID
     * @return 用户社会化关联信息
     */
    UserSocialDO getBySourceAndOpenId(String source, String openId);

    /**
     * 保存
     *
     * @param userSocial 用户社会化关联信息
     */
    void saveOrUpdate(UserSocialDO userSocial);

    /**
     * 根据用户 ID 查询
     *
     * @param userId 用户 ID
     * @return 用户社会化关联信息
     */
    List<UserSocialDO> listByUserId(Long userId);

    /**
     * 绑定
     *
     * @param authUser 三方账号信息
     * @param userId   用户 ID
     */
    void bind(AuthUser authUser, Long userId);

    /**
     * 根据来源和用户 ID 删除
     *
     * @param source 来源
     * @param userId 用户 ID
     */
    void deleteBySourceAndUserId(String source, Long userId);
}