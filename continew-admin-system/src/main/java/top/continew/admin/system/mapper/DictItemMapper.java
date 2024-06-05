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

package top.continew.admin.system.mapper;

import org.apache.ibatis.annotations.Param;
import top.continew.admin.system.model.entity.DictItemDO;
import top.continew.starter.data.mybatis.plus.base.BaseMapper;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.List;

/**
 * 字典项 Mapper
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
public interface DictItemMapper extends BaseMapper<DictItemDO> {

    /**
     * 根据字典编码查询
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    List<LabelValueResp> listByDictCode(@Param("dictCode") String dictCode);
}