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

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.Cached;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.enums.OptionCategoryEnum;
import top.continew.admin.system.model.query.OptionQuery;
import top.continew.admin.system.model.resp.file.FileUploadResp;
import top.continew.admin.system.service.DictItemService;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.OptionService;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.log.annotation.Log;
import top.continew.starter.storage.domain.model.resp.FileInfo;

import java.io.IOException;
import java.util.List;

/**
 * 公共 API
 *
 * @author Charles7c
 * @since 2023/1/22 21:48
 */
@Tag(name = "公共 API")
@Log(ignore = true)
@Validated
@RestController("systemCommonController")
@RequiredArgsConstructor
@RequestMapping("/system/common")
public class CommonController {

    private final FileService fileService;
    private final DictItemService dictItemService;
    private final OptionService optionService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param parentPath 上级目录
     * @return 上传结果
     */
    @Operation(summary = "上传文件", description = "上传文件")
    @Parameter(name = "parentPath", description = "上级目录", example = "/", in = ParameterIn.QUERY)
    @PostMapping("/file")
    public FileUploadResp upload(@RequestPart @NotNull(message = "文件不能为空") MultipartFile file,
        @RequestParam(required = false) String parentPath) throws IOException {
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        FileInfo fileInfo = fileService.upload(file, parentPath);
        return FileUploadResp.builder()
            .id(fileInfo.getFileId())
            .url(fileInfo.getUrl())
            .thUrl(fileInfo.getThumbnailPath())
            .metadata(fileInfo.getMetadata())
            .build();
    }

    @Operation(summary = "查询字典", description = "查询字典列表")
    @Parameter(name = "code", description = "字典编码", example = "notice_type", in = ParameterIn.PATH)
    @GetMapping("/dict/{code}")
    public List<LabelValueResp> listDict(@PathVariable String code) {
        return dictItemService.listByDictCode(code);
    }

    /**
     * 查询系统配置参数
     *
     * @return 系统配置参数列表
     */
    @TenantIgnore
    @SaIgnore
    @Operation(summary = "查询系统配置参数", description = "查询系统配置参数")
    @GetMapping("/dict/option/site")
    @Cached(key = "'SITE'", name = CacheConstants.OPTION_KEY_PREFIX)
    public List<LabelValueResp<String>> listSiteOptionDict() {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setCategory(OptionCategoryEnum.SITE.name());
        return optionService.list(optionQuery)
            .stream()
            .map(option -> new LabelValueResp<>(option.getCode(),
                StrUtil.nullToDefault(option.getValue(), option
                    .getDefaultValue())))
            .toList();
    }

    @TenantIgnore
    @SaIgnore
    @Operation(summary = "查询租户开启状态", description = "查询租户开启状态")
    @GetMapping("/dict/option/tenant")
    @Cached(key = "'TENANT'", name = CacheConstants.OPTION_KEY_PREFIX)
    public Boolean tenantEnabled() {
        return TenantContextHolder.isTenantEnabled();
    }
}
