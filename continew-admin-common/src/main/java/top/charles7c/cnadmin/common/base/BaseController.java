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
    @Operation(summary = "分页查询列表")
    @ResponseBody
    @GetMapping
    public R<PageDataVO<V>> page(Q query, @Validated PageQuery pageQuery) {
        this.checkPermission("list");
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
    @Operation(summary = "查询树列表")
    @ResponseBody
    @GetMapping("/tree")
    public R<List<Tree<Long>>> tree(Q query, SortQuery sortQuery) {
        this.checkPermission("list");
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
    @Operation(summary = "查询列表")
    @ResponseBody
    @GetMapping("/list")
    public R<List<V>> list(Q query, SortQuery sortQuery) {
        this.checkPermission("list");
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
    @Operation(summary = "查看详情")
    @Parameter(name = "id", description = "ID", in = ParameterIn.PATH)
    @ResponseBody
    @GetMapping("/{id}")
    public R<D> get(@PathVariable Long id) {
        this.checkPermission("list");
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
    @Operation(summary = "新增数据")
    @ResponseBody
    @PostMapping
    public R<Long> add(@Validated(BaseRequest.Add.class) @RequestBody C request) {
        this.checkPermission("add");
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
    @Operation(summary = "修改数据")
    @ResponseBody
    @PutMapping("/{id}")
    public R update(@Validated(BaseRequest.Update.class) @RequestBody C request, @PathVariable Long id) {
        this.checkPermission("update");
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
    @Operation(summary = "删除数据")
    @Parameter(name = "ids", description = "ID 列表", in = ParameterIn.PATH)
    @ResponseBody
    @DeleteMapping("/{ids}")
    public R delete(@PathVariable List<Long> ids) {
        this.checkPermission("delete");
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
    @Operation(summary = "导出数据")
    @GetMapping("/export")
    public void export(Q query, SortQuery sortQuery, HttpServletResponse response) {
        this.checkPermission("export");
        baseService.export(query, sortQuery, response);
    }

    /**
     * 权限认证
     *
     * @param subPermission
     *            部分权限码
     */
    private void checkPermission(String subPermission) {
        CrudRequestMapping crudRequestMapping = this.getClass().getDeclaredAnnotation(CrudRequestMapping.class);
        String path = crudRequestMapping.value();
        String permissionPrefix = String.join(StringConsts.COLON, StrUtil.splitTrim(path, StringConsts.SLASH));
        StpUtil.checkPermission(String.format("%s:%s", permissionPrefix, subPermission));
    }
}
