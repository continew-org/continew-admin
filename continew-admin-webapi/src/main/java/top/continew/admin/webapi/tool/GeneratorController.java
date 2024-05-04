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

package top.continew.admin.webapi.tool;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.continew.admin.generator.model.entity.FieldConfigDO;
import top.continew.admin.generator.model.entity.GenConfigDO;
import top.continew.admin.generator.model.query.TableQuery;
import top.continew.admin.generator.model.req.GenConfigReq;
import top.continew.admin.generator.model.resp.GeneratePreviewResp;
import top.continew.admin.generator.model.resp.TableResp;
import top.continew.admin.generator.service.GeneratorService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.web.model.R;

import java.sql.SQLException;
import java.util.List;

/**
 * 代码生成 API
 *
 * @author Charles7c
 * @since 2023/8/3 22:58
 */
@Tag(name = "代码生成 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/generator")
public class GeneratorController {

    private final GeneratorService baseService;

    @Operation(summary = "分页查询数据表", description = "分页查询数据表")
    @SaCheckPermission("tool:generator:list")
    @GetMapping("/table")
    public R<PageResp<TableResp>> pageTable(TableQuery query, @Validated PageQuery pageQuery) throws SQLException {
        return R.ok(baseService.pageTable(query, pageQuery));
    }

    @Operation(summary = "查询字段配置列表", description = "查询字段配置列表")
    @Parameter(name = "tableName", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @Parameter(name = "requireSync", description = "是否需要同步", example = "false", in = ParameterIn.QUERY)
    @SaCheckPermission("tool:generator:list")
    @GetMapping("/field/{tableName}")
    public R<List<FieldConfigDO>> listFieldConfig(@PathVariable String tableName,
                                                  @RequestParam(required = false, defaultValue = "false") Boolean requireSync) {
        return R.ok(baseService.listFieldConfig(tableName, requireSync));
    }

    @Operation(summary = "查询生成配置信息", description = "查询生成配置信息")
    @Parameter(name = "tableName", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("tool:generator:list")
    @GetMapping("/config/{tableName}")
    public R<GenConfigDO> getGenConfig(@PathVariable String tableName) throws SQLException {
        return R.ok(baseService.getGenConfig(tableName));
    }

    @Operation(summary = "保存配置信息", description = "保存配置信息")
    @Parameter(name = "tableName", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("tool:generator:list")
    @PostMapping("/config/{tableName}")
    public R<Void> saveConfig(@Validated @RequestBody GenConfigReq req, @PathVariable String tableName) {
        baseService.saveConfig(req, tableName);
        return R.ok("保存成功");
    }

    @Operation(summary = "生成预览", description = "预览生成代码")
    @Parameter(name = "tableName", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("tool:generator:list")
    @GetMapping("/preview/{tableName}")
    public R<List<GeneratePreviewResp>> preview(@PathVariable String tableName) {
        return R.ok(baseService.preview(tableName));
    }

    @Operation(summary = "生成代码", description = "生成代码")
    @Parameter(name = "tableName", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("tool:generator:list")
    @PostMapping("/{tableNames}")
    public void generate(@PathVariable List<String> tableNames,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        baseService.generate(tableNames, request, response);
    }
}
