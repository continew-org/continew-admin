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

package top.charles7c.cnadmin.common.base;

import java.util.List;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;

/**
 * 业务接口基类
 *
 * @param <V>
 *            列表信息
 * @param <D>
 *            详情信息
 * @param <Q>
 *            查询条件
 * @param <C>
 *            创建或修改信息
 * @author Charles7c
 * @since 2023/1/26 16:54
 */
public interface BaseService<V, D, Q, C extends BaseRequest> {

    /**
     * 分页查询列表
     *
     * @param query
     *            查询条件
     * @param pageQuery
     *            分页查询条件
     * @return 分页列表信息
     */
    PageDataVO<V> page(Q query, PageQuery pageQuery);

    /**
     * 查询列表
     *
     * @param query
     *            查询条件
     * @return 列表信息
     */
    List<V> list(Q query);

    /**
     * 查看详情
     *
     * @param id
     *            ID
     * @return 详情信息
     */
    D get(Long id);

    /**
     * 新增
     *
     * @param request
     *            创建信息
     * @return 自增 ID
     */
    Long create(C request);

    /**
     * 修改
     *
     * @param request
     *            修改信息
     */
    void update(C request);

    /**
     * 删除
     *
     * @param ids
     *            ID 列表
     */
    void delete(List<Long> ids);
}
