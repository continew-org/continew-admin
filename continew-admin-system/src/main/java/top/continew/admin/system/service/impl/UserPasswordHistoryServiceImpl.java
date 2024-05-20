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
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.system.mapper.UserPasswordHistoryMapper;
import top.continew.admin.system.model.entity.UserPasswordHistoryDO;
import top.continew.admin.system.service.UserPasswordHistoryService;

import java.util.List;

/**
 * 用户历史密码业务实现
 *
 * @author Charles7c
 * @since 2024/5/16 21:58
 */
@Service
@RequiredArgsConstructor
public class UserPasswordHistoryServiceImpl implements UserPasswordHistoryService {

    private final UserPasswordHistoryMapper baseMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, String password, int count) {
        if (StrUtil.isBlank(password)) {
            return;
        }
        baseMapper.insert(new UserPasswordHistoryDO(userId, password));
        // 删除过期历史密码
        baseMapper.deleteExpired(userId, count);
    }

    @Override
    public boolean isPasswordReused(Long userId, String password, int count) {
        // 查询近 N 个历史密码
        List<UserPasswordHistoryDO> list = baseMapper.lambdaQuery()
            .select(UserPasswordHistoryDO::getPassword)
            .eq(UserPasswordHistoryDO::getUserId, userId)
            .orderByDesc(UserPasswordHistoryDO::getCreateTime)
            .last("LIMIT %s".formatted(count))
            .list();
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        // 校验是否重复使用历史密码
        List<String> passwordList = list.stream().map(UserPasswordHistoryDO::getPassword).toList();
        return passwordList.stream().anyMatch(p -> passwordEncoder.matches(password, p));
    }
}