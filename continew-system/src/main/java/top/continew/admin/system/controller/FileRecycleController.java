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

package top.continew.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.system.model.query.FileQuery;
import top.continew.admin.system.model.resp.file.FileResp;
import top.continew.admin.system.service.FileRecycleService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

/**
 * 文件回收站管理 API
 *
 * @author Charles7c
 * @since 2025/11/11 21:28
 */
@Tag(name = "文件回收站管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/file/recycle")
public class FileRecycleController {

    private final FileRecycleService baseService;

    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("system:fileRecycle:list")
    @GetMapping
    public PageResp<FileResp> page(@Valid FileQuery query, @Valid PageQuery pageQuery) {
        return baseService.page(query, pageQuery);
    }

    @Operation(summary = "还原文件", description = "还原文件")
    @SaCheckPermission("system:fileRecycle:restore")
    @PutMapping("/restore/{id}")
    public void restore(@PathVariable Long id) {
        baseService.restore(id);
    }

    @Operation(summary = "删除文件", description = "删除文件")
    @SaCheckPermission("system:fileRecycle:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        baseService.delete(id);
    }

    @Operation(summary = "清空回收站", description = "清空回收站")
    @SaCheckPermission("system:fileRecycle:clean")
    @DeleteMapping("/clean")
    public void clean() {
        baseService.clean();
    }
}
