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

package top.charles7c.cnadmin.webapi.controller.common;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ClassUtil;

import top.charles7c.cnadmin.common.base.BaseEnum;
import top.charles7c.cnadmin.common.config.properties.ContiNewAdminProperties;
import top.charles7c.cnadmin.common.model.query.SortQuery;
import top.charles7c.cnadmin.common.model.vo.LabelValueVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.monitor.annotation.Log;
import top.charles7c.cnadmin.system.model.query.DeptQuery;
import top.charles7c.cnadmin.system.model.query.MenuQuery;
import top.charles7c.cnadmin.system.model.query.RoleQuery;
import top.charles7c.cnadmin.system.model.vo.RoleVO;
import top.charles7c.cnadmin.system.service.DeptService;
import top.charles7c.cnadmin.system.service.MenuService;
import top.charles7c.cnadmin.system.service.RoleService;

/**
 * 公共 API
 *
 * @author Charles7c
 * @since 2023/1/22 21:48
 */
@Tag(name = "公共 API")
@Log(ignore = true)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/common")
public class CommonController {

    private final DeptService deptService;
    private final MenuService menuService;
    private final RoleService roleService;
    private final ContiNewAdminProperties properties;

    @Operation(summary = "查询部门树", description = "查询树结构的部门列表")
    @GetMapping("/tree/dept")
    public R<List<Tree<Long>>> listDeptTree(@Validated DeptQuery query, @Validated SortQuery sortQuery) {
        List<Tree<Long>> treeList = deptService.tree(query, sortQuery, true);
        return R.ok(treeList);
    }

    @Operation(summary = "查询菜单树", description = "查询树结构的菜单列表")
    @GetMapping("/tree/menu")
    public R<List<Tree<Long>>> listMenuTree(@Validated MenuQuery query, @Validated SortQuery sortQuery) {
        List<Tree<Long>> treeList = menuService.tree(query, sortQuery, true);
        return R.ok(treeList);
    }

    @Operation(summary = "查询角色字典", description = "查询角色字典列表")
    @GetMapping("/dict/role")
    public R<List<LabelValueVO<Long>>> listRoleDict(@Validated RoleQuery query, @Validated SortQuery sortQuery) {
        List<RoleVO> list = roleService.list(query, sortQuery);
        List<LabelValueVO<Long>> dictList = roleService.buildDict(list);
        return R.ok(dictList);
    }

    @Operation(summary = "查询枚举字典", description = "查询枚举字典列表")
    @GetMapping("/dict/enum/{enumTypeName}")
    public R<List<LabelValueVO>> listEnumDict(@PathVariable String enumTypeName) {
        // 扫描所有 BaseEnum 枚举基类的子类
        Set<Class<?>> classSet = ClassUtil.scanPackageBySuper(properties.getBasePackage(), BaseEnum.class);
        Optional<Class<?>> first =
            classSet.stream().filter(c -> c.getSimpleName().equalsIgnoreCase(enumTypeName)).findFirst();
        if (!first.isPresent()) {
            return R.fail("枚举字典不存在");
        }
        // 转换枚举为字典列表
        Class<?> enumClass = first.get();
        Object[] enumConstants = enumClass.getEnumConstants();
        List<LabelValueVO> dictList = Arrays.stream(enumConstants).map(e -> {
            BaseEnum<Integer, String> baseEnum = (BaseEnum<Integer, String>)e;
            return new LabelValueVO(baseEnum.getDescription(), baseEnum.getValue());
        }).collect(Collectors.toList());
        return R.ok(dictList);
    }
}
