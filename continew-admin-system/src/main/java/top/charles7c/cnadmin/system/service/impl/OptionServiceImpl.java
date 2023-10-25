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

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import cn.hutool.core.bean.BeanUtil;

import top.charles7c.cnadmin.common.constant.CacheConsts;
import top.charles7c.cnadmin.common.util.helper.QueryHelper;
import top.charles7c.cnadmin.system.mapper.OptionMapper;
import top.charles7c.cnadmin.system.model.entity.OptionDO;
import top.charles7c.cnadmin.system.model.query.OptionQuery;
import top.charles7c.cnadmin.system.model.request.OptionRequest;
import top.charles7c.cnadmin.system.model.request.OptionResetValueRequest;
import top.charles7c.cnadmin.system.model.vo.OptionVO;
import top.charles7c.cnadmin.system.service.OptionService;

/**
 * 参数业务实现
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheConsts.OPTION_KEY_PREFIX)
public class OptionServiceImpl implements OptionService {

    private final OptionMapper baseMapper;

    @Override
    public List<OptionVO> list(OptionQuery query) {
        return BeanUtil.copyToList(baseMapper.selectList(QueryHelper.build(query)), OptionVO.class);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void update(List<OptionRequest> request) {
        baseMapper.updateBatchById(BeanUtil.copyToList(request, OptionDO.class));
    }

    @Override
    @CacheEvict(allEntries = true)
    public void resetValue(OptionResetValueRequest request) {
        baseMapper.lambdaUpdate().set(OptionDO::getValue, null).in(OptionDO::getCode, request.getCode()).update();
    }
}