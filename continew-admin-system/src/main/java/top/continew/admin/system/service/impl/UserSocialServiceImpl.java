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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import cn.hutool.json.JSONUtil;

import top.continew.admin.common.enums.SocialSourceEnum;
import top.continew.admin.system.mapper.UserSocialMapper;
import top.continew.admin.system.model.entity.UserSocialDO;
import top.continew.admin.system.service.UserSocialService;
import top.continew.starter.core.util.validate.CheckUtils;

import me.zhyd.oauth.model.AuthUser;

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
    public void saveOrUpdate(UserSocialDO userSocial) {
        if (null == userSocial.getCreateTime()) {
            baseMapper.insert(userSocial);
        } else {
            baseMapper.lambdaUpdate()
                .set(UserSocialDO::getMetaJson, userSocial.getMetaJson())
                .set(UserSocialDO::getLastLoginTime, userSocial.getLastLoginTime())
                .eq(UserSocialDO::getSource, userSocial.getSource())
                .eq(UserSocialDO::getOpenId, userSocial.getOpenId())
                .update();
        }
    }

    @Override
    public List<UserSocialDO> listByUserId(Long userId) {
        return baseMapper.lambdaQuery().eq(UserSocialDO::getUserId, userId).list();
    }

    @Override
    public void bind(AuthUser authUser, Long userId) {
        String source = authUser.getSource();
        String openId = authUser.getUuid();
        List<UserSocialDO> userSocialList = this.listByUserId(userId);
        Set<String> boundSocialSet = userSocialList.stream().map(UserSocialDO::getSource).collect(Collectors.toSet());
        String description = SocialSourceEnum.valueOf(source).getDescription();
        CheckUtils.throwIf(boundSocialSet.contains(source), "您已经绑定过了 [{}] 平台，请先解绑", description);
        UserSocialDO userSocial = this.getBySourceAndOpenId(source, openId);
        CheckUtils.throwIfNotNull(userSocial, "[{}] 平台账号 [{}] 已被其他用户绑定", description, authUser.getUsername());
        userSocial = new UserSocialDO();
        userSocial.setUserId(userId);
        userSocial.setSource(source);
        userSocial.setOpenId(openId);
        userSocial.setMetaJson(JSONUtil.toJsonStr(authUser));
        userSocial.setLastLoginTime(LocalDateTime.now());
        baseMapper.insert(userSocial);
    }

    @Override
    public void deleteBySourceAndUserId(String source, Long userId) {
        baseMapper.lambdaUpdate().eq(UserSocialDO::getSource, source).eq(UserSocialDO::getUserId, userId).remove();
    }
}
