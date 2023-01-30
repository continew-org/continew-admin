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

import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.model.vo.R;

/**
 * 控制器基类
 *
 * @param <S>
 *            业务接口
 * @param <V>
 *            列表信息
 * @param <D>
 *            详情信息
 * @param <Q>
 *            查询条件
 * @param <C>
 *            创建或修改信息
 * @author Charles7c
 * @since 2023/1/26 10:45
 */
@NoArgsConstructor
public abstract class BaseController<S extends BaseService<V, D, Q, C>, V, D, Q, C extends BaseRequest> {

    @Autowired
    protected S baseService;

    /**
     * 分页查询列表
     *
     * @param query
     *            查询条件
     * @param pageQuery
     *            分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表")
    @ResponseBody
    @GetMapping
    protected R<PageDataVO<V>> page(@Validated Q query, @Validated PageQuery pageQuery) {
        PageDataVO<V> pageDataVO = baseService.page(query, pageQuery);
        return R.ok(pageDataVO);
    }

    /**
     * 查询列表
     *
     * @param query
     *            查询条件
     * @return 列表信息
     */
    @Operation(summary = "查询列表")
    @ResponseBody
    @GetMapping("/all")
    protected R<List<V>> list(@Validated Q query) {
        List<V> list = baseService.list(query);
        return R.ok(list);
    }

    /**
     * 查看详情
     *
     * @param id
     *            ID
     * @return 详情信息
     */
    @Operation(summary = "查看详情")
    @Parameter(name = "id", description = "ID", in = ParameterIn.PATH)
    @ResponseBody
    @GetMapping("/{id}")
    protected R<D> detail(@PathVariable Long id) {
        D detail = baseService.detail(id);
        return R.ok(detail);
    }

    /**
     * 新增
     *
     * @param request
     *            创建信息
     * @return 自增 ID
     */
    @Operation(summary = "新增")
    @ResponseBody
    @PostMapping
    protected R<Long> create(@Validated(BaseRequest.Create.class) @RequestBody C request) {
        Long id = baseService.create(request);
        return R.ok("新增成功", id);
    }

    /**
     * 修改
     *
     * @param request
     *            修改信息
     * @return /
     */
    @Operation(summary = "修改")
    @ResponseBody
    @PutMapping
    protected R update(@Validated(BaseRequest.Update.class) @RequestBody C request) {
        baseService.update(request);
        return R.ok("修改成功");
    }

    /**
     * 删除
     *
     * @param ids
     *            ID 列表
     * @return /
     */
    @Operation(summary = "删除")
    @Parameter(name = "ids", description = "ID 列表", in = ParameterIn.PATH)
    @ResponseBody
    @DeleteMapping("/{ids}")
    protected R delete(@PathVariable List<Long> ids) {
        baseService.delete(ids);
        return R.ok("删除成功");
    }
}
