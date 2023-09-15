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

package top.charles7c.cnadmin.system.service;

import java.util.List;

import top.charles7c.cnadmin.common.base.BaseService;
import top.charles7c.cnadmin.system.model.query.DictItemQuery;
import top.charles7c.cnadmin.system.model.request.DictItemRequest;
import top.charles7c.cnadmin.system.model.vo.DictItemDetailVO;
import top.charles7c.cnadmin.system.model.vo.DictItemVO;

/**
 * 字典项业务接口
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
public interface DictItemService extends BaseService<DictItemVO, DictItemDetailVO, DictItemQuery, DictItemRequest> {

    /**
     * 根据字典 ID 列表删除
     *
     * @param dictIds
     *            字典 ID 列表
     */
    void deleteByDictIds(List<Long> dictIds);
}