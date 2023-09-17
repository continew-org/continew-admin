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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.model.query.SortQuery;
import top.charles7c.cnadmin.common.model.vo.LabelValueVO;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.DictItemMapper;
import top.charles7c.cnadmin.system.model.entity.DictItemDO;
import top.charles7c.cnadmin.system.model.query.DictItemQuery;
import top.charles7c.cnadmin.system.model.request.DictItemRequest;
import top.charles7c.cnadmin.system.model.vo.DictItemDetailVO;
import top.charles7c.cnadmin.system.model.vo.DictItemVO;
import top.charles7c.cnadmin.system.service.DictItemService;

/**
 * 字典项业务实现
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Service
@RequiredArgsConstructor
public class DictItemServiceImpl
    extends BaseServiceImpl<DictItemMapper, DictItemDO, DictItemVO, DictItemDetailVO, DictItemQuery, DictItemRequest>
    implements DictItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(DictItemRequest request) {
        String value = request.getValue();
        CheckUtils.throwIf(this.checkValueExists(value, null, request.getDictId()), "新增失败，字典值 [{}] 已存在", value);
        return super.add(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictItemRequest request, Long id) {
        String value = request.getValue();
        CheckUtils.throwIf(this.checkValueExists(value, id, request.getDictId()), "修改失败，字典值 [{}] 已存在", value);
        super.update(request, id);
    }

    @Override
    public List<DictItemDetailVO> listByDictId(Long dictId) {
        DictItemQuery dictItemQuery = new DictItemQuery();
        dictItemQuery.setDictId(dictId);
        SortQuery sortQuery = new SortQuery();
        sortQuery.setSort(new String[] {"sort,asc"});
        List<DictItemDetailVO> detailList = super.list(dictItemQuery, sortQuery, DictItemDetailVO.class);
        detailList.forEach(super::fillDetail);
        return detailList;
    }

    @Override
    public List<LabelValueVO> listByDictCode(String dictCode) {
        return baseMapper.listByDictCode(dictCode);
    }

    @Override
    public void deleteByDictIds(List<Long> dictIds) {
        baseMapper.lambdaUpdate().in(DictItemDO::getDictId, dictIds).remove();
    }

    /**
     * 检查字典值是否存在
     *
     * @param value
     *            字典值
     * @param id
     *            ID
     * @param dictId
     *            字典 ID
     * @return 是否存在
     */
    private boolean checkValueExists(String value, Long id, Long dictId) {
        return baseMapper.lambdaQuery().eq(DictItemDO::getValue, value).eq(DictItemDO::getDictId, dictId)
            .ne(null != id, DictItemDO::getId, id).exists();
    }
}