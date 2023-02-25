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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.collection.CollUtil;

import top.charles7c.cnadmin.system.mapper.UserPostMapper;
import top.charles7c.cnadmin.system.model.entity.UserPostDO;
import top.charles7c.cnadmin.system.service.UserPostService;

/**
 * 用户和岗位业务实现类
 *
 * @author Charles7c
 * @since 2023/2/25 22:40
 */
@Service
@RequiredArgsConstructor
public class UserPostServiceImpl implements UserPostService {

    private final UserPostMapper userPostMapper;

    @Override
    public void save(List<Long> postIds, Long userId) {
        if (CollUtil.isEmpty(postIds)) {
            return;
        }
        // 删除原有关联
        userPostMapper.delete(Wrappers.<UserPostDO>lambdaQuery().eq(UserPostDO::getUserId, userId));
        // 保存最新关联
        List<UserPostDO> userPostList =
            postIds.stream().map(postId -> new UserPostDO(userId, postId)).collect(Collectors.toList());
        userPostMapper.insertBatch(userPostList);
    }

    @Override
    public Long countByPostIds(List<Long> postIds) {
        return userPostMapper.selectCount(Wrappers.<UserPostDO>lambdaQuery().in(UserPostDO::getPostId, postIds));
    }

    @Override
    public List<Long> listPostIdsByUserId(Long userId) {
        List<UserPostDO> userPostList = userPostMapper.selectList(
            Wrappers.<UserPostDO>lambdaQuery().select(UserPostDO::getPostId).eq(UserPostDO::getUserId, userId));
        if (CollUtil.isEmpty(userPostList)) {
            return Collections.emptyList();
        }
        return userPostList.stream().map(UserPostDO::getPostId).collect(Collectors.toList());
    }
}
