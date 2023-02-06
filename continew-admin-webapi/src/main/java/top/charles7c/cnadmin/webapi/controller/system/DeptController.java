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

package top.charles7c.cnadmin.webapi.controller.system;

import static top.charles7c.cnadmin.common.annotation.CrudRequestMapping.Api;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import top.charles7c.cnadmin.common.annotation.CrudRequestMapping;
import top.charles7c.cnadmin.common.base.BaseController;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.system.model.query.DeptQuery;
import top.charles7c.cnadmin.system.model.request.DeptRequest;
import top.charles7c.cnadmin.system.model.vo.DeptDetailVO;
import top.charles7c.cnadmin.system.model.vo.DeptVO;
import top.charles7c.cnadmin.system.service.DeptService;

/**
 * 部门管理 API
 *
 * @author Charles7c
 * @since 2023/1/22 17:50
 */
@Tag(name = "部门管理 API")
@RestController
@CrudRequestMapping(value = "/system/dept", api = {Api.LIST, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE})
public class DeptController extends BaseController<DeptService, DeptVO, DeptDetailVO, DeptQuery, DeptRequest> {

    @Override
    @Operation(summary = "查询列表树")
    public R<List<DeptVO>> list(@Validated DeptQuery query) {
        List<DeptVO> list = baseService.list(query);
        return R.ok(baseService.buildListTree(list));
    }

    @Operation(summary = "导出部门数据")
    @GetMapping("/export")
    public void export(@Validated DeptQuery query, HttpServletResponse response) {
        baseService.export(query, response);
    }
}
