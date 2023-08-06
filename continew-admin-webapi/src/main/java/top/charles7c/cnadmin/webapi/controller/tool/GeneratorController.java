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

package top.charles7c.cnadmin.webapi.controller.tool;

import java.sql.SQLException;
import java.util.List;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.tool.model.entity.ColumnMappingDO;
import top.charles7c.cnadmin.tool.model.query.TableQuery;
import top.charles7c.cnadmin.tool.model.vo.TableVO;
import top.charles7c.cnadmin.tool.service.GeneratorService;

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
@RequestMapping("/tool/generator")
public class GeneratorController {

    private final GeneratorService generatorService;

    @Operation(summary = "分页查询数据表", description = "分页查询数据表")
    @GetMapping("/table")
    public R<PageDataVO<TableVO>> pageTable(TableQuery query, @Validated PageQuery pageQuery) throws SQLException {
        return R.ok(generatorService.pageTable(query, pageQuery));
    }

    @Operation(summary = "查询列映射信息列表", description = "查询列映射信息列表")
    @GetMapping("/column/{tableName}")
    public R<List<ColumnMappingDO>> listColumnMapping(@PathVariable String tableName) {
        return R.ok(generatorService.listColumnMapping(tableName));
    }
}
