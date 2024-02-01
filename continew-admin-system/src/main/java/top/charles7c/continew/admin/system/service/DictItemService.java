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

package top.charles7c.continew.admin.system.service;

import top.charles7c.continew.admin.common.model.resp.LabelValueResp;
import top.charles7c.continew.admin.system.model.entity.DictItemDO;
import top.charles7c.continew.admin.system.model.query.DictItemQuery;
import top.charles7c.continew.admin.system.model.req.DictItemReq;
import top.charles7c.continew.admin.system.model.resp.DictItemDetailResp;
import top.charles7c.continew.admin.system.model.resp.DictItemResp;
import top.charles7c.continew.starter.extension.crud.service.BaseService;
import top.charles7c.continew.starter.extension.crud.service.IService;

import java.util.List;

/**
 * 字典项业务接口
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
public interface DictItemService extends BaseService<DictItemResp, DictItemDetailResp, DictItemQuery, DictItemReq>, IService<DictItemDO> {

    /**
     * 根据字典 ID 查询
     *
     * @param dictId 字典 ID
     * @return 字典项列表
     */
    List<DictItemDetailResp> listByDictId(Long dictId);

    /**
     * 根据字典编码查询
     * 
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    List<LabelValueResp> listByDictCode(String dictCode);

    /**
     * 根据字典 ID 列表删除
     *
     * @param dictIds 字典 ID 列表
     */
    void deleteByDictIds(List<Long> dictIds);
}