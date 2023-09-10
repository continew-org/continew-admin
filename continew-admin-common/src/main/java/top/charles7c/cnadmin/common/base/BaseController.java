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

import static top.charles7c.cnadmin.common.annotation.CrudRequestMapping.Api;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.annotation.CrudRequestMapping;
import top.charles7c.cnadmin.common.constant.StringConsts;
import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.query.SortQuery;
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
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @ResponseBody
    @GetMapping
    public R<PageDataVO<V>> page(Q query, @Validated PageQuery pageQuery) {
        this.checkPermission(Api.LIST);
        PageDataVO<V> pageDataVO = baseService.page(query, pageQuery);
        return R.ok(pageDataVO);
    }

    /**
     * 查询树列表
     *
     * @param query
     *            查询条件
     * @param sortQuery
     *            排序查询条件
     * @return 树列表信息
     */
    @Operation(summary = "查询树列表", description = "查询树列表")
    @ResponseBody
    @GetMapping("/tree")
    public R<List<Tree<Long>>> tree(Q query, SortQuery sortQuery) {
        this.checkPermission(Api.LIST);
        List<Tree<Long>> list = baseService.tree(query, sortQuery, false);
        return R.ok(list);
    }

    /**
     * 查询列表
     *
     * @param query
     *            查询条件
     * @param sortQuery
     *            排序查询条件
     * @return 列表信息
     */
    @Operation(summary = "查询列表", description = "查询列表")
    @ResponseBody
    @GetMapping("/list")
    public R<List<V>> list(Q query, SortQuery sortQuery) {
        this.checkPermission(Api.LIST);
        List<V> list = baseService.list(query, sortQuery);
        return R.ok(list);
    }

    /**
     * 查看详情
     *
     * @param id
     *            ID
     * @return 详情信息
     */
    @Operation(summary = "查看详情", description = "查看详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @ResponseBody
    @GetMapping("/{id}")
    public R<D> get(@PathVariable Long id) {
        this.checkPermission(Api.LIST);
        D detail = baseService.get(id);
        return R.ok(detail);
    }

    /**
     * 新增
     *
     * @param request
     *            创建信息
     * @return 自增 ID
     */
    @Operation(summary = "新增数据", description = "新增数据")
    @ResponseBody
    @PostMapping
    public R<Long> add(@Validated(ValidateGroup.Crud.Add.class) @RequestBody C request) {
        this.checkPermission(Api.ADD);
        Long id = baseService.add(request);
        return R.ok("新增成功", id);
    }

    /**
     * 修改
     *
     * @param request
     *            修改信息
     * @param id
     *            ID
     * @return /
     */
    @Operation(summary = "修改数据", description = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @ResponseBody
    @PutMapping("/{id}")
    public R update(@Validated(ValidateGroup.Crud.Update.class) @RequestBody C request, @PathVariable Long id) {
        this.checkPermission(Api.UPDATE);
        baseService.update(request, id);
        return R.ok("修改成功");
    }

    /**
     * 删除
     *
     * @param ids
     *            ID 列表
     * @return /
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "ids", description = "ID 列表", example = "1,2", in = ParameterIn.PATH)
    @ResponseBody
    @DeleteMapping("/{ids}")
    public R delete(@PathVariable List<Long> ids) {
        this.checkPermission(Api.DELETE);
        baseService.delete(ids);
        return R.ok("删除成功");
    }

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
    @Operation(summary = "导出数据", description = "导出数据")
    @GetMapping("/export")
    public void export(Q query, SortQuery sortQuery, HttpServletResponse response) {
        this.checkPermission(Api.EXPORT);
        baseService.export(query, sortQuery, response);
    }

    /**
     * 根据 API 类型进行权限验证
     *
     * @param api
     *            API 类型
     */
    private void checkPermission(Api api) {
        CrudRequestMapping crudRequestMapping = this.getClass().getDeclaredAnnotation(CrudRequestMapping.class);
        String path = crudRequestMapping.value();
        String permissionPrefix = String.join(StringConsts.COLON, StrUtil.splitTrim(path, StringConsts.SLASH));
        StpUtil.checkPermission(String.format("%s:%s", permissionPrefix, api.name().toLowerCase()));
    }
}
