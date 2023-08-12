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
import org.springframework.web.bind.annotation.*;

import cn.hutool.extra.spring.SpringUtil;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.validate.ValidationUtils;
import top.charles7c.cnadmin.tool.model.entity.FieldConfigDO;
import top.charles7c.cnadmin.tool.model.entity.GenConfigDO;
import top.charles7c.cnadmin.tool.model.query.TableQuery;
import top.charles7c.cnadmin.tool.model.request.GenConfigRequest;
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

    @Operation(summary = "查询字段配置列表", description = "查询字段配置列表")
    @GetMapping("/field/{tableName}")
    public R<List<FieldConfigDO>> listFieldConfig(@PathVariable String tableName,
        @RequestParam(required = false, defaultValue = "false") Boolean requireSync) {
        return R.ok(generatorService.listFieldConfig(tableName, requireSync));
    }

    @Operation(summary = "查询生成配置信息", description = "查询生成配置信息")
    @GetMapping("/config/{tableName}")
    public R<GenConfigDO> getGenConfig(@PathVariable String tableName) throws SQLException {
        return R.ok(generatorService.getGenConfig(tableName));
    }

    @Operation(summary = "保存配置信息", description = "保存配置信息")
    @PostMapping("/config/{tableName}")
    public R saveConfig(@Validated @RequestBody GenConfigRequest request, @PathVariable String tableName) {
        generatorService.saveConfig(request, tableName);
        return R.ok("保存成功");
    }

    @Operation(summary = "生成代码", description = "生成代码")
    @PostMapping("/{tableName}")
    public R generate(@PathVariable String tableName) {
        ValidationUtils.throwIf("prod".equals(SpringUtil.getActiveProfile()), "仅支持在开发环境生成代码");
        generatorService.generate(tableName);
        return R.ok("生成成功，请查看生成代码是否正确");
    }
}
