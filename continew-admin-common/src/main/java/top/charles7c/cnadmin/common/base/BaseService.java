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

import jakarta.servlet.http.HttpServletResponse;

import cn.hutool.core.lang.tree.Tree;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.query.SortQuery;
import top.charles7c.cnadmin.common.model.resp.PageDataResp;

/**
 * 业务接口基类
 *
 * @param <L>
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
public interface BaseService<L, D, Q, C extends BaseReq> {

    /**
     * 分页查询列表
     *
     * @param query
     *            查询条件
     * @param pageQuery
     *            分页查询条件
     * @return 分页列表信息
     */
    PageDataResp<L> page(Q query, PageQuery pageQuery);

    /**
     * 查询树列表
     *
     * @param query
     *            查询条件
     * @param sortQuery
     *            排序查询条件
     * @param isSimple
     *            是否为简单树结构（不包含基本树结构之外的扩展字段）
     * @return 树列表信息
     */
    List<Tree<Long>> tree(Q query, SortQuery sortQuery, boolean isSimple);

    /**
     * 查询列表
     *
     * @param query
     *            查询条件
     * @param sortQuery
     *            排序查询条件
     * @return 列表信息
     */
    List<L> list(Q query, SortQuery sortQuery);

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
     * @param req
     *            创建信息
     * @return 自增 ID
     */
    Long add(C req);

    /**
     * 修改
     *
     * @param req
     *            修改信息
     * @param id
     *            ID
     */
    void update(C req, Long id);

    /**
     * 删除
     *
     * @param ids
     *            ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 导出
     *
     * @param query
     *            查询条件
     * @param sortQuery
     *            排序查询条件
     * @param response
     *            响应对象
     */
    void export(Q query, SortQuery sortQuery, HttpServletResponse response);
}
