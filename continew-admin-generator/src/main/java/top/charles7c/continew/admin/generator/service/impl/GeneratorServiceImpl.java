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

package top.charles7c.continew.admin.generator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.charles7c.continew.admin.generator.config.properties.GeneratorProperties;
import top.charles7c.continew.admin.generator.config.properties.GeneratorProperties.TemplateConfig;
import top.charles7c.continew.admin.generator.enums.QueryTypeEnum;
import top.charles7c.continew.admin.generator.mapper.FieldConfigMapper;
import top.charles7c.continew.admin.generator.mapper.GenConfigMapper;
import top.charles7c.continew.admin.generator.model.entity.FieldConfigDO;
import top.charles7c.continew.admin.generator.model.entity.GenConfigDO;
import top.charles7c.continew.admin.generator.model.query.TableQuery;
import top.charles7c.continew.admin.generator.model.req.GenConfigReq;
import top.charles7c.continew.admin.generator.model.resp.GeneratePreviewResp;
import top.charles7c.continew.admin.generator.model.resp.TableResp;
import top.charles7c.continew.admin.generator.service.GeneratorService;
import top.charles7c.continew.starter.core.constant.StringConstants;
import top.charles7c.continew.starter.core.exception.BusinessException;
import top.charles7c.continew.starter.core.util.TemplateUtils;
import top.charles7c.continew.starter.data.core.enums.DatabaseType;
import top.charles7c.continew.starter.data.core.util.MetaUtils;
import top.charles7c.continew.starter.data.core.util.Table;
import top.charles7c.continew.starter.core.util.validate.CheckUtils;
import top.charles7c.continew.starter.extension.crud.model.query.PageQuery;
import top.charles7c.continew.starter.extension.crud.model.resp.PageResp;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 代码生成业务实现
 *
 * @author Charles7c
 * @since 2023/4/12 23:58
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {

    private final DataSource dataSource;
    private final GeneratorProperties generatorProperties;
    private final FieldConfigMapper fieldConfigMapper;
    private final GenConfigMapper genConfigMapper;

    @Override
    public PageResp<TableResp> pageTable(TableQuery query, PageQuery pageQuery) throws SQLException {
        List<Table> tableList = MetaUtils.getTables(dataSource);
        String tableName = query.getTableName();
        if (StrUtil.isNotBlank(tableName)) {
            tableList.removeIf(table -> !StrUtil.containsAny(table.getTableName(), tableName));
        }
        tableList.removeIf(table -> StrUtil.equalsAnyIgnoreCase(table.getTableName(), generatorProperties
            .getExcludeTables()));
        CollUtil.sort(tableList, Comparator.comparing(Table::getCreateTime)
            .thenComparing(table -> Optional.ofNullable(table.getUpdateTime()).orElse(table.getCreateTime()))
            .reversed());
        List<TableResp> tableRespList = BeanUtil.copyToList(tableList, TableResp.class);
        PageResp<TableResp> pageResp = PageResp.build(pageQuery.getPage(), pageQuery.getSize(), tableRespList);
        for (TableResp tableResp : pageResp.getList()) {
            long count = genConfigMapper.selectCount(Wrappers.lambdaQuery(GenConfigDO.class)
                .eq(GenConfigDO::getTableName, tableResp.getTableName()));
            tableResp.setIsConfiged(count > 0);
        }
        return pageResp;
    }

    @Override
    public GenConfigDO getGenConfig(String tableName) throws SQLException {
        GenConfigDO genConfig = genConfigMapper.selectById(tableName);
        if (null == genConfig) {
            genConfig = new GenConfigDO(tableName);
            // 默认包名（当前包名）
            String packageName = ClassUtil.getPackage(GeneratorService.class);
            genConfig.setPackageName(StrUtil.subBefore(packageName, StringConstants.DOT, true));
            // 默认业务名（表注释）
            List<Table> tableList = MetaUtils.getTables(dataSource, tableName);
            if (CollUtil.isNotEmpty(tableList)) {
                Table table = tableList.get(0);
                genConfig.setBusinessName(StrUtil.replace(table.getComment(), "表", StringConstants.EMPTY));
            }
            // 默认作者名称（上次保存使用的作者名称）
            GenConfigDO lastGenConfig = genConfigMapper.selectOne(Wrappers.lambdaQuery(GenConfigDO.class)
                .orderByDesc(GenConfigDO::getCreateTime)
                .last("LIMIT 1"));
            if (null != lastGenConfig) {
                genConfig.setAuthor(lastGenConfig.getAuthor());
            }
            // 默认表前缀（sys_user -> sys_）
            int underLineIndex = StrUtil.indexOf(tableName, StringConstants.C_UNDERLINE);
            if (-1 != underLineIndex) {
                genConfig.setTablePrefix(StrUtil.subPre(tableName, underLineIndex + 1));
            }
        }
        return genConfig;
    }

    @Override
    public List<FieldConfigDO> listFieldConfig(String tableName, Boolean requireSync) {
        List<FieldConfigDO> fieldConfigList = fieldConfigMapper.selectListByTableName(tableName);
        if (CollUtil.isNotEmpty(fieldConfigList) && Boolean.FALSE.equals(requireSync)) {
            return fieldConfigList;
        }
        List<FieldConfigDO> latestFieldConfigList = new ArrayList<>();
        // 获取最新数据表列信息
        Collection<Column> columnList = MetaUtils.getColumns(dataSource, tableName);
        // 获取数据库对应的类型映射配置
        DatabaseType databaseType = MetaUtils.getDatabaseType(dataSource);
        Map<String, List<String>> typeMappingMap = generatorProperties.getTypeMappings()
            .get(databaseType.getDatabase());
        CheckUtils.throwIfEmpty(typeMappingMap, "请先配置对应数据库的类型映射");
        Set<Map.Entry<String, List<String>>> typeMappingEntrySet = typeMappingMap.entrySet();
        // 新增或更新字段配置
        Map<String, FieldConfigDO> fieldConfigMap = fieldConfigList.stream()
            .collect(Collectors.toMap(FieldConfigDO::getColumnName, Function.identity(), (key1, key2) -> key2));
        int i = 1;
        for (Column column : columnList) {
            FieldConfigDO fieldConfig = Optional.ofNullable(fieldConfigMap.get(column.getName()))
                .orElseGet(() -> new FieldConfigDO(column));
            // 更新已有字段配置
            if (null != fieldConfig.getCreateTime()) {
                String columnType = StrUtil.splitToArray(column.getTypeName(), StringConstants.SPACE)[0].toLowerCase();
                fieldConfig.setColumnType(columnType);
                fieldConfig.setColumnSize(Convert.toStr(column.getSize()));
            }
            String fieldType = typeMappingEntrySet.stream()
                .filter(entry -> entry.getValue().contains(fieldConfig.getColumnType()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
            fieldConfig.setFieldType(fieldType);
            fieldConfig.setFieldSort(i++);
            latestFieldConfigList.add(fieldConfig);
        }
        return latestFieldConfigList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(GenConfigReq req, String tableName) {
        // 保存字段配置
        fieldConfigMapper.delete(Wrappers.lambdaQuery(FieldConfigDO.class).eq(FieldConfigDO::getTableName, tableName));
        List<FieldConfigDO> fieldConfigList = req.getFieldConfigs();
        for (FieldConfigDO fieldConfig : fieldConfigList) {
            if (Boolean.TRUE.equals(fieldConfig.getShowInForm())) {
                CheckUtils.throwIfNull(fieldConfig.getFormType(), "字段 [{}] 的表单类型不能为空", fieldConfig.getFieldName());
            } else {
                // 在表单中不显示，不需要设置必填
                fieldConfig.setIsRequired(false);
            }
            if (Boolean.TRUE.equals(fieldConfig.getShowInQuery())) {
                CheckUtils.throwIfNull(fieldConfig.getFormType(), "字段 [{}] 的表单类型不能为空", fieldConfig.getFieldName());
                CheckUtils.throwIfNull(fieldConfig.getQueryType(), "字段 [{}] 的查询方式不能为空", fieldConfig.getFieldName());
            } else {
                // 在查询中不显示，不需要设置查询方式
                fieldConfig.setQueryType(null);
            }
            // 既不在表单也不在查询中显示，不需要设置表单类型
            if (Boolean.FALSE.equals(fieldConfig.getShowInForm()) && Boolean.FALSE.equals(fieldConfig
                .getShowInQuery())) {
                fieldConfig.setFormType(null);
            }
            fieldConfig.setTableName(tableName);
        }
        fieldConfigMapper.insertBatch(fieldConfigList);
        // 保存或更新生成配置信息
        GenConfigDO newGenConfig = req.getGenConfig();
        String frontendPath = newGenConfig.getFrontendPath();
        if (StrUtil.isNotBlank(frontendPath)) {
            CheckUtils.throwIf(!FileUtil.exist(frontendPath), "前端路径不存在");
            CheckUtils.throwIf(!StrUtil.containsAll(frontendPath, "src", "views"), "前端路径配置错误");
        }
        GenConfigDO oldGenConfig = genConfigMapper.selectById(tableName);
        if (null != oldGenConfig) {
            BeanUtil.copyProperties(newGenConfig, oldGenConfig);
            genConfigMapper.updateById(oldGenConfig);
        } else {
            genConfigMapper.insert(newGenConfig);
        }
    }

    @Override
    public List<GeneratePreviewResp> preview(String tableName) {
        List<GeneratePreviewResp> generatePreviewList = new ArrayList<>();
        // 初始化配置
        GenConfigDO genConfig = genConfigMapper.selectById(tableName);
        CheckUtils.throwIfNull(genConfig, "请先进行数据表 [{}] 生成配置", tableName);
        List<FieldConfigDO> fieldConfigList = fieldConfigMapper.selectListByTableName(tableName);
        CheckUtils.throwIfEmpty(fieldConfigList, "请先进行数据表 [{}] 字段配置", tableName);
        Map<String, Object> genConfigMap = BeanUtil.beanToMap(genConfig);
        genConfigMap.put("date", DateUtil.date().toString("yyyy/MM/dd HH:mm"));
        String packageName = genConfig.getPackageName();
        String apiModuleName = StrUtil.subSuf(packageName, StrUtil
            .lastIndexOfIgnoreCase(packageName, StringConstants.DOT) + 1);
        genConfigMap.put("apiModuleName", apiModuleName);
        genConfigMap.put("apiName", StrUtil.lowerFirst(genConfig.getClassNamePrefix()));
        // 渲染后端代码
        String classNamePrefix = genConfig.getClassNamePrefix();
        Map<String, TemplateConfig> templateConfigMap = generatorProperties.getTemplateConfigs();
        for (Map.Entry<String, TemplateConfig> templateConfigEntry : templateConfigMap.entrySet()) {
            this.pretreatment(genConfigMap, fieldConfigList, templateConfigEntry);
            String className = classNamePrefix + StrUtil.nullToEmpty(templateConfigEntry.getKey());
            genConfigMap.put("className", className);
            TemplateConfig templateConfig = templateConfigEntry.getValue();
            String content = TemplateUtils.render(templateConfig.getTemplatePath(), genConfigMap);
            GeneratePreviewResp codePreview = new GeneratePreviewResp();
            codePreview.setFileName(className + FileNameUtil.EXT_JAVA);
            codePreview.setContent(content);
            codePreview.setBackend(true);
            generatePreviewList.add(codePreview);
        }
        // 渲染前端代码
        // api 代码
        genConfigMap.put("fieldConfigs", fieldConfigList);
        String apiContent = TemplateUtils.render("api.ftl", genConfigMap);
        GeneratePreviewResp apiCodePreview = new GeneratePreviewResp();
        apiCodePreview.setFileName(classNamePrefix.toLowerCase() + ".ts");
        apiCodePreview.setContent(apiContent);
        generatePreviewList.add(apiCodePreview);
        // view 代码
        String viewContent = TemplateUtils.render("index.ftl", genConfigMap);
        GeneratePreviewResp viewCodePreview = new GeneratePreviewResp();
        viewCodePreview.setFileName("index.vue");
        viewCodePreview.setContent(viewContent);
        generatePreviewList.add(viewCodePreview);
        return generatePreviewList;
    }

    @Override
    public void generate(String tableName) {
        // 初始化配置及数据
        List<GeneratePreviewResp> generatePreviewList = this.preview(tableName);
        GenConfigDO genConfig = genConfigMapper.selectById(tableName);
        Boolean isOverride = genConfig.getIsOverride();
        // 生成代码
        String packageName = genConfig.getPackageName();
        try {
            // 生成后端代码
            String classNamePrefix = genConfig.getClassNamePrefix();
            // 1.确定后端代码基础路径
            // 例如：D:/continew-admin
            String projectPath = SystemUtil.getUserInfo().getCurrentDir();
            // 例如：D:/continew-admin/continew-admin-system
            File backendModuleFile = new File(projectPath, genConfig.getModuleName());
            // 例如：D:/continew-admin/continew-admin-system/src/main/java/top/charles7c/continew/admin/system
            List<String> backendModuleChildPathList = CollUtil.newArrayList("src", "main", "java");
            backendModuleChildPathList.addAll(StrUtil.split(packageName, StringConstants.DOT));
            File backendParentFile = FileUtil.file(backendModuleFile, backendModuleChildPathList
                .toArray(new String[0]));
            // 2.生成代码
            List<GeneratePreviewResp> backendCodePreviewList = generatePreviewList.stream()
                .filter(GeneratePreviewResp::isBackend)
                .toList();
            Map<String, TemplateConfig> templateConfigMap = generatorProperties.getTemplateConfigs();
            for (GeneratePreviewResp codePreview : backendCodePreviewList) {
                // 例如：D:/continew-admin/continew-admin-system/src/main/java/top/charles7c/continew/admin/system/service/impl/XxxServiceImpl.java
                TemplateConfig templateConfig = templateConfigMap.get(codePreview.getFileName()
                    .replace(classNamePrefix, StringConstants.EMPTY)
                    .replace(FileNameUtil.EXT_JAVA, StringConstants.EMPTY));
                File classParentFile = FileUtil.file(backendParentFile, StrUtil.splitToArray(templateConfig
                    .getPackageName(), StringConstants.DOT));
                File classFile = new File(classParentFile, codePreview.getFileName());
                // 如果已经存在，且不允许覆盖，则跳过
                if (classFile.exists() && Boolean.FALSE.equals(isOverride)) {
                    continue;
                }
                FileUtil.writeUtf8String(codePreview.getContent(), classFile);
            }
            // 生成前端代码
            String frontendPath = genConfig.getFrontendPath();
            if (StrUtil.isBlank(frontendPath)) {
                return;
            }
            List<GeneratePreviewResp> frontendCodePreviewList = generatePreviewList.stream()
                .filter(p -> !p.isBackend())
                .toList();
            // 1.生成 api 代码
            String apiModuleName = StrUtil.subSuf(packageName, StrUtil
                .lastIndexOfIgnoreCase(packageName, StringConstants.DOT) + 1);
            GeneratePreviewResp apiCodePreview = frontendCodePreviewList.get(0);
            // 例如：D:/continew-admin-ui
            List<String> frontendSubPathList = StrUtil.split(frontendPath, "src");
            String frontendModulePath = frontendSubPathList.get(0);
            // 例如：D:/continew-admin-ui/src/api/tool/xxx.ts
            File apiParentFile = FileUtil.file(frontendModulePath, "src", "api", apiModuleName);
            File apiFile = new File(apiParentFile, apiCodePreview.getFileName());
            if (apiFile.exists() && Boolean.FALSE.equals(isOverride)) {
                return;
            }
            FileUtil.writeUtf8String(apiCodePreview.getContent(), apiFile);
            // 2.生成 view 代码
            GeneratePreviewResp viewCodePreview = frontendCodePreviewList.get(1);
            // 例如：D:/continew-admin-ui/src/views/system/xxx/index.vue
            File indexFile = FileUtil.file(frontendPath, apiModuleName, StrUtil
                .lowerFirst(classNamePrefix), "index.vue");
            if (indexFile.exists() && Boolean.FALSE.equals(isOverride)) {
                return;
            }
            FileUtil.writeUtf8String(viewCodePreview.getContent(), indexFile);
        } catch (Exception e) {
            log.error("Generate code occurred an error: {}. tableName: {}.", e.getMessage(), tableName, e);
            throw new BusinessException("代码生成失败，请手动清理生成文件");
        }
    }

    /**
     * 预处理生成配置
     *
     * @param genConfigMap          生成配置
     * @param originFieldConfigList 原始字段配置列表
     * @param templateConfigEntry   模板配置
     */
    private void pretreatment(Map<String, Object> genConfigMap,
                              List<FieldConfigDO> originFieldConfigList,
                              Map.Entry<String, TemplateConfig> templateConfigEntry) {
        TemplateConfig templateConfig = templateConfigEntry.getValue();
        // 移除需要忽略的字段
        List<FieldConfigDO> fieldConfigList = originFieldConfigList.stream()
            .filter(fieldConfig -> !StrUtil.equalsAny(fieldConfig.getFieldName(), templateConfig.getExcludeFields()))
            .toList();
        genConfigMap.put("fieldConfigs", fieldConfigList);
        // 统计部分特殊字段特征
        genConfigMap.put("hasLocalDateTime", false);
        genConfigMap.put("hasBigDecimal", false);
        genConfigMap.put("hasRequiredField", false);
        genConfigMap.put("hasListQueryField", false);
        for (FieldConfigDO fieldConfig : fieldConfigList) {
            String fieldType = fieldConfig.getFieldType();
            if ("LocalDateTime".equals(fieldType)) {
                genConfigMap.put("hasLocalDateTime", true);
            }
            if ("BigDecimal".equals(fieldType)) {
                genConfigMap.put("hasBigDecimal", true);
            }
            if (Boolean.TRUE.equals(fieldConfig.getIsRequired())) {
                genConfigMap.put("hasRequiredField", true);
            }
            QueryTypeEnum queryType = fieldConfig.getQueryType();
            if (null != queryType && StrUtil.equalsAny(queryType.name(), QueryTypeEnum.IN.name(), QueryTypeEnum.NOT_IN
                .name(), QueryTypeEnum.BETWEEN.name())) {
                genConfigMap.put("hasListQueryField", true);
            }
        }
        String subPackageName = templateConfig.getPackageName();
        genConfigMap.put("subPackageName", subPackageName);
    }
}
