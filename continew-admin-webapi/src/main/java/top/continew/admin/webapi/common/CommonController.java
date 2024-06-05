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

package top.continew.admin.webapi.common;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.Cached;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.model.query.DeptQuery;
import top.continew.admin.system.model.query.MenuQuery;
import top.continew.admin.system.model.query.OptionQuery;
import top.continew.admin.system.model.query.RoleQuery;
import top.continew.admin.system.model.resp.FileUploadResp;
import top.continew.admin.system.service.*;
import top.continew.starter.core.autoconfigure.project.ProjectProperties;
import top.continew.starter.core.util.validate.ValidationUtils;
import top.continew.starter.data.mybatis.plus.base.IBaseEnum;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.log.core.annotation.Log;
import top.continew.starter.web.model.R;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private final ProjectProperties projectProperties;
    private final FileService fileService;
    private final DeptService deptService;
    private final MenuService menuService;
    private final RoleService roleService;
    private final DictItemService dictItemService;
    private final OptionService optionService;

    @Operation(summary = "上传文件", description = "上传文件")
    @PostMapping("/file")
    public R<FileUploadResp> upload(@NotNull(message = "文件不能为空") MultipartFile file) {
        ValidationUtils.throwIf(projectProperties.isProduction(), "演示环境不支持上传文件");
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        FileInfo fileInfo = fileService.upload(file);
        return R.ok(FileUploadResp.builder().url(fileInfo.getUrl()).build());
    }

    @Operation(summary = "查询部门树", description = "查询树结构的部门列表")
    @GetMapping("/tree/dept")
    public R<List<Tree<Long>>> listDeptTree(DeptQuery query, SortQuery sortQuery) {
        return R.ok(deptService.tree(query, sortQuery, true));
    }

    @Operation(summary = "查询菜单树", description = "查询树结构的菜单列表")
    @GetMapping("/tree/menu")
    public R<List<Tree<Long>>> listMenuTree(MenuQuery query, SortQuery sortQuery) {
        return R.ok(menuService.tree(query, sortQuery, true));
    }

    @Operation(summary = "查询角色字典", description = "查询角色字典列表")
    @GetMapping("/dict/role")
    public R<List<LabelValueResp>> listRoleDict(RoleQuery query, SortQuery sortQuery) {
        return R.ok(roleService.listDict(query, sortQuery));
    }

    @Operation(summary = "查询字典", description = "查询字典列表")
    @Parameter(name = "code", description = "字典编码", example = "notice_type", in = ParameterIn.PATH)
    @GetMapping("/dict/{code}")
    public R<List<LabelValueResp>> listDict(@PathVariable String code) {
        Optional<Class<?>> enumClassOptional = this.getEnumClassByName(code);
        return R.ok(enumClassOptional.map(this::listEnumDict).orElseGet(() -> dictItemService.listByDictCode(code)));
    }

    @SaIgnore
    @Operation(summary = "查询参数字典", description = "查询参数字典")
    @GetMapping("/dict/option")
    @Cached(key = "#category", name = CacheConstants.OPTION_KEY_PREFIX)
    public R<List<LabelValueResp<String>>> listOptionDict(@NotBlank(message = "类别不能为空") @RequestParam String category) {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setCategory(category);
        return R.ok(optionService.list(optionQuery)
            .stream()
            .map(option -> new LabelValueResp<>(option.getCode(), StrUtil.nullToDefault(option.getValue(), option
                .getDefaultValue())))
            .toList());
    }

    /**
     * 根据枚举类名查询
     *
     * @param enumClassName 枚举类名
     * @return 枚举类型
     */
    private Optional<Class<?>> getEnumClassByName(String enumClassName) {
        Set<Class<?>> classSet = ClassUtil.scanPackageBySuper(projectProperties.getBasePackage(), IBaseEnum.class);
        return classSet.stream()
            .filter(c -> StrUtil.equalsAnyIgnoreCase(c.getSimpleName(), enumClassName, StrUtil
                .toCamelCase(enumClassName)))
            .findFirst();
    }

    /**
     * 查询枚举字典
     *
     * @param enumClass 枚举类型
     * @return 枚举字典
     */
    private List<LabelValueResp> listEnumDict(Class<?> enumClass) {
        Object[] enumConstants = enumClass.getEnumConstants();
        return Arrays.stream(enumConstants).map(e -> {
            IBaseEnum baseEnum = (IBaseEnum)e;
            return new LabelValueResp(baseEnum.getDescription(), baseEnum.getValue(), baseEnum.getColor());
        }).toList();
    }
}
