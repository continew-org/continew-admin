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
import org.springframework.transaction.annotation.Transactional;

import top.charles7c.continew.admin.common.constant.CacheConstants;
import top.charles7c.continew.admin.common.model.resp.LabelValueResp;
import top.charles7c.continew.admin.system.mapper.DictItemMapper;
import top.charles7c.continew.admin.system.model.entity.DictItemDO;
import top.charles7c.continew.admin.system.model.query.DictItemQuery;
import top.charles7c.continew.admin.system.model.req.DictItemReq;
import top.charles7c.continew.admin.system.model.resp.DictItemDetailResp;
import top.charles7c.continew.admin.system.model.resp.DictItemResp;
import top.charles7c.continew.admin.system.service.DictItemService;
import top.charles7c.continew.starter.core.util.validate.CheckUtils;
import top.charles7c.continew.starter.extension.crud.base.BaseServiceImpl;
import top.charles7c.continew.starter.extension.crud.model.query.SortQuery;

/**
 * 字典项业务实现
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheConstants.DICT_KEY_PREFIX)
public class DictItemServiceImpl
    extends BaseServiceImpl<DictItemMapper, DictItemDO, DictItemResp, DictItemDetailResp, DictItemQuery, DictItemReq>
    implements DictItemService {

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Long add(DictItemReq req) {
        String value = req.getValue();
        CheckUtils.throwIf(this.isValueExists(value, null, req.getDictId()), "新增失败，字典值 [{}] 已存在", value);
        return super.add(req);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void update(DictItemReq req, Long id) {
        String value = req.getValue();
        CheckUtils.throwIf(this.isValueExists(value, id, req.getDictId()), "修改失败，字典值 [{}] 已存在", value);
        super.update(req, id);
    }

    @Override
    public List<DictItemDetailResp> listByDictId(Long dictId) {
        DictItemQuery dictItemQuery = new DictItemQuery();
        dictItemQuery.setDictId(dictId);
        SortQuery sortQuery = new SortQuery();
        sortQuery.setSort(new String[] {"sort,asc"});
        List<DictItemDetailResp> detailList = super.list(dictItemQuery, sortQuery, DictItemDetailResp.class);
        detailList.forEach(super::fillDetail);
        return detailList;
    }

    @Override
    public List<LabelValueResp> listByDictCode(String dictCode) {
        return baseMapper.listByDictCode(dictCode);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteByDictIds(List<Long> dictIds) {
        baseMapper.lambdaUpdate().in(DictItemDO::getDictId, dictIds).remove();
    }

    /**
     * 字典值是否存在
     *
     * @param value
     *            字典值
     * @param id
     *            ID
     * @param dictId
     *            字典 ID
     * @return 是否存在
     */
    private boolean isValueExists(String value, Long id, Long dictId) {
        return baseMapper.lambdaQuery().eq(DictItemDO::getValue, value).eq(DictItemDO::getDictId, dictId)
            .ne(null != id, DictItemDO::getId, id).exists();
    }
}