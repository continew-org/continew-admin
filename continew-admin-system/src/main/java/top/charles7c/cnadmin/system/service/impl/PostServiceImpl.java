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
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.model.vo.LabelValueVO;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.PostMapper;
import top.charles7c.cnadmin.system.model.entity.PostDO;
import top.charles7c.cnadmin.system.model.query.PostQuery;
import top.charles7c.cnadmin.system.model.request.PostRequest;
import top.charles7c.cnadmin.system.model.vo.*;
import top.charles7c.cnadmin.system.service.*;

/**
 * 岗位业务实现类
 *
 * @author Charles7c
 * @since 2023/2/25 22:38
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends BaseServiceImpl<PostMapper, PostDO, PostVO, PostDetailVO, PostQuery, PostRequest>
    implements PostService {

    private final UserPostService userPostService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(PostRequest request) {
        String postName = request.getPostName();
        boolean isExists = this.checkNameExists(postName, request.getPostId());
        CheckUtils.throwIf(() -> isExists, String.format("新增失败，'%s'已存在", postName));

        // 新增岗位
        request.setStatus(DisEnableStatusEnum.ENABLE);
        return super.add(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PostRequest request) {
        String postName = request.getPostName();
        boolean isExists = this.checkNameExists(postName, request.getPostId());
        CheckUtils.throwIf(() -> isExists, String.format("修改失败，'%s'已存在", postName));

        // 更新岗位
        super.update(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        CheckUtils.throwIf(() -> userPostService.countByPostIds(ids) > 0, "所选岗位存在用户关联，请解除关联后重试");
        super.delete(ids);
    }

    /**
     * 检查名称是否存在
     *
     * @param name
     *            名称
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean checkNameExists(String name, Long id) {
        return super.lambdaQuery().eq(PostDO::getPostName, name).ne(id != null, PostDO::getPostId, id).exists();
    }

    @Override
    public List<LabelValueVO<Long>> buildDict(List<PostVO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(p -> new LabelValueVO<>(p.getPostName(), p.getPostId())).collect(Collectors.toList());
    }

    @Override
    public List<String> listPostNamesByPostIds(List<Long> postIds) {
        List<PostDO> postList = super.lambdaQuery().select(PostDO::getPostName).in(PostDO::getPostId, postIds).list();
        if (CollUtil.isEmpty(postList)) {
            return Collections.emptyList();
        }
        return postList.stream().map(PostDO::getPostName).collect(Collectors.toList());
    }
}
