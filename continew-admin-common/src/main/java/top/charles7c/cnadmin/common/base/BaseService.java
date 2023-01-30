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

import java.util.Collections;
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
 *            创建信息
 * @param <U>
 *            修改信息
 * @author Charles7c
 * @since 2023/1/26 16:54
 */
public interface BaseService<V, D, Q, C, U> {

    /**
     * 分页查询列表
     *
     * @param query
     *            查询条件
     * @param pageQuery
     *            分页查询条件
     * @return 分页列表信息
     */
    default PageDataVO<V> page(Q query, PageQuery pageQuery) {
        return new PageDataVO<>();
    }

    /**
     * 查询列表
     *
     * @param query
     *            查询条件
     * @return 列表信息
     */
    default List<V> list(Q query) {
        return Collections.emptyList();
    }

    /**
     * 查看详情
     *
     * @param id
     *            ID
     * @return 详情信息
     */
    default D detail(Long id) {
        return null;
    }

    /**
     * 新增
     *
     * @param request
     *            创建信息
     * @return 自增 ID
     */
    default Long create(C request) {
        return null;
    }

    /**
     * 修改
     *
     * @param id
     *            ID
     * @param request
     *            修改信息
     */
    default void update(Long id, U request) {}

    /**
     * 删除
     *
     * @param ids
     *            ID 列表
     */
    default void delete(List<Long> ids) {}
}
