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

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.charles7c.cnadmin.system.mapper.UserSocialMapper;
import top.charles7c.cnadmin.system.model.entity.UserSocialDO;
import top.charles7c.cnadmin.system.service.UserSocialService;

/**
 * 用户社会化关联业务实现
 *
 * @author Charles7c
 * @since 2023/10/11 22:10
 */
@Service
@RequiredArgsConstructor
public class UserSocialServiceImpl implements UserSocialService {

    private final UserSocialMapper baseMapper;

    @Override
    public UserSocialDO getBySourceAndOpenId(String source, String openId) {
        return baseMapper.selectBySourceAndOpenId(source, openId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(UserSocialDO userSocial) {
        if (null == userSocial.getCreateTime()) {
            baseMapper.insert(userSocial);
        } else {
            baseMapper.lambdaUpdate().set(UserSocialDO::getMetaJson, userSocial.getMetaJson())
                .set(UserSocialDO::getLastLoginTime, userSocial.getLastLoginTime())
                .eq(UserSocialDO::getSource, userSocial.getSource()).eq(UserSocialDO::getOpenId, userSocial.getOpenId())
                .update();
        }
    }
}
