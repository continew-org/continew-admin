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

package top.charles7c.continew.admin.system.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import cn.hutool.core.bean.BeanUtil;

import top.charles7c.continew.admin.common.constant.CacheConstants;
import top.charles7c.continew.admin.system.mapper.OptionMapper;
import top.charles7c.continew.admin.system.model.entity.OptionDO;
import top.charles7c.continew.admin.system.model.query.OptionQuery;
import top.charles7c.continew.admin.system.model.req.OptionReq;
import top.charles7c.continew.admin.system.model.req.OptionResetValueReq;
import top.charles7c.continew.admin.system.model.resp.OptionResp;
import top.charles7c.continew.admin.system.service.OptionService;
import top.charles7c.continew.starter.data.mybatis.plus.util.QueryHelper;

/**
 * 参数业务实现
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheConstants.OPTION_KEY_PREFIX)
public class OptionServiceImpl implements OptionService {

    private final OptionMapper baseMapper;

    @Override
    public List<OptionResp> list(OptionQuery query) {
        return BeanUtil.copyToList(baseMapper.selectList(QueryHelper.build(query)), OptionResp.class);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void update(List<OptionReq> req) {
        baseMapper.updateBatchById(BeanUtil.copyToList(req, OptionDO.class));
    }

    @Override
    @CacheEvict(allEntries = true)
    public void resetValue(OptionResetValueReq req) {
        baseMapper.lambdaUpdate().set(OptionDO::getValue, null).in(OptionDO::getCode, req.getCode()).update();
    }
}