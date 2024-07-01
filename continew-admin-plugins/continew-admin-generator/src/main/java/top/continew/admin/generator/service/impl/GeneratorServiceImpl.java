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

package top.continew.admin.generator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.generator.config.properties.GeneratorProperties;
import top.continew.admin.generator.enums.QueryTypeEnum;
import top.continew.admin.generator.mapper.FieldConfigMapper;
import top.continew.admin.generator.mapper.GenConfigMapper;
import top.continew.admin.generator.model.entity.FieldConfigDO;
import top.continew.admin.generator.model.entity.GenConfigDO;
import top.continew.admin.generator.model.query.TableQuery;
import top.continew.admin.generator.model.req.GenConfigReq;
import top.continew.admin.generator.model.resp.GeneratePreviewResp;
import top.continew.admin.generator.model.resp.TableResp;
import top.continew.admin.generator.service.GeneratorService;
import top.continew.starter.core.autoconfigure.project.ProjectProperties;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.core.util.TemplateUtils;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.data.core.enums.DatabaseType;
import top.continew.starter.data.core.util.MetaUtils;
import top.continew.starter.data.core.util.Table;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.web.util.FileUploadUtils;

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
    private final ProjectProperties projectProperties;
    private final FieldConfigMapper fieldConfigMapper;
    private final GenConfigMapper genConfigMapper;

    @Override
    public PageResp<TableResp> pageTable(TableQuery query, PageQuery pageQuery) throws SQLException {
        List<Table> tableList = MetaUtils.getTables(dataSource);
        String tableName = query.getTableName();
        if (StrUtil.isNotBlank(tableName)) {
            tableList.removeIf(table -> !StrUtil.containsAnyIgnoreCase(table.getTableName(), tableName));
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
        Map<String, List<String>> typeMappingMap = generatorProperties.getTypeMappings().get(databaseType);
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
        // 渲染代码
        String classNamePrefix = genConfig.getClassNamePrefix();
        Map<String, GeneratorProperties.TemplateConfig> templateConfigMap = generatorProperties.getTemplateConfigs();
        for (Map.Entry<String, GeneratorProperties.TemplateConfig> templateConfigEntry : templateConfigMap.entrySet()) {
            this.pretreatment(genConfigMap, fieldConfigList, templateConfigEntry);
            String className = classNamePrefix + StrUtil.nullToEmpty(templateConfigEntry.getKey());
            genConfigMap.put("className", className);
            GeneratorProperties.TemplateConfig templateConfig = templateConfigEntry.getValue();
            boolean isBackend = templateConfig.isBackend();
            String extension = templateConfig.getExtension();
            GeneratePreviewResp generatePreview = new GeneratePreviewResp();
            generatePreview.setBackend(isBackend);
            generatePreviewList.add(generatePreview);
            if (isBackend) {
                generatePreview.setFileName(className + extension);
                generatePreview.setContent(TemplateUtils.render(templateConfig.getTemplatePath(), genConfigMap));
            } else {
                generatePreview.setFileName(".vue".equals(extension) && "index".equals(templateConfigEntry.getKey())
                    ? "index.vue"
                    : this.getFrontendFileName(classNamePrefix, className, extension));
                genConfigMap.put("fieldConfigs", fieldConfigList);
                generatePreview.setContent(TemplateUtils.render(templateConfig.getTemplatePath(), genConfigMap));
            }
            setPreviewPath(generatePreview, genConfig, templateConfig);
        }
        return generatePreviewList;
    }

    private void setPreviewPath(GeneratePreviewResp generatePreview,
                                GenConfigDO genConfig,
                                GeneratorProperties.TemplateConfig templateConfig) {
        // 获取前后端基础路径
        String backendBasicPackagePath = this.buildBackendBasicPackagePath(genConfig);
        String frontendBasicPackagePath = String.join(File.separator, projectProperties.getAppName(), projectProperties
            .getAppName() + "-ui");
        String packageName = genConfig.getPackageName();
        String moduleName = StrUtil.subSuf(packageName, StrUtil
            .lastIndexOfIgnoreCase(packageName, StringConstants.DOT) + 1);
        String packagePath;
        if (generatePreview.isBackend()) {
            // 例如：continew-admin/continew-system/src/main/java/top/continew/admin/system/service/impl
            packagePath = String.join(File.separator, backendBasicPackagePath, templateConfig.getPackageName()
                .replace(StringConstants.DOT, File.separator));
        } else {
            // 例如：continew-admin/continew-admin-ui/src/views/system
            packagePath = String.join(File.separator, frontendBasicPackagePath, templateConfig.getPackageName()
                .replace(StringConstants.SLASH, File.separator), moduleName);
            // 例如：continew-admin/continew-admin-ui/src/views/system/user
            packagePath = ".vue".equals(templateConfig.getExtension())
                ? packagePath + File.separator + StrUtil.lowerFirst(genConfig.getClassNamePrefix())
                : packagePath;
        }
        generatePreview.setPath(packagePath);
    }

    @Override
    public void generate(List<String> tableNames, HttpServletRequest request, HttpServletResponse response) {
        try {
            String tempDir = SystemUtil.getUserInfo().getTempDir();
            // 删除旧代码
            FileUtil.del(tempDir + projectProperties.getAppName());
            tableNames.forEach(tableName -> {
                // 初始化配置及数据
                List<GeneratePreviewResp> generatePreviewList = this.preview(tableName);
                // 生成代码
                this.generateCode(generatePreviewList, genConfigMapper.selectById(tableName));
            });
            // 打包下载
            File tempDirFile = new File(tempDir, projectProperties.getAppName());
            String zipFilePath = tempDirFile.getPath() + jodd.io.ZipUtil.ZIP_EXT;
            ZipUtil.zip(tempDirFile.getPath(), zipFilePath);
            FileUploadUtils.download(request, response, new File(zipFilePath), true);
        } catch (Exception e) {
            log.error("Generate code of table '{}' occurred an error. {}", tableNames, e.getMessage(), e);
            throw new BusinessException("代码生成失败，请手动清理生成文件");
        }
    }

    /**
     * 生成代码
     *
     * @param generatePreviewList 生成预览列表
     * @param genConfig           生成配置
     */
    private void generateCode(List<GeneratePreviewResp> generatePreviewList, GenConfigDO genConfig) {
        for (GeneratePreviewResp generatePreview : generatePreviewList) {
            // 后端：continew-admin/continew-system/src/main/java/top/continew/admin/system/service/impl/XxxServiceImpl.java
            // 前端：continew-admin/continew-admin-ui/src/views/system/user/index.vue
            File file = new File(SystemUtil.getUserInfo().getTempDir() + generatePreview.getPath(), generatePreview
                .getFileName());
            // 如果已经存在，且不允许覆盖，则跳过
            if (!file.exists() || Boolean.TRUE.equals(genConfig.getIsOverride())) {
                FileUtil.writeUtf8String(generatePreview.getContent(), file);
            }
        }
    }

    /**
     * 构建后端包路径
     *
     * @param genConfig 生成配置
     * @return 后端包路径
     */
    private String buildBackendBasicPackagePath(GenConfigDO genConfig) {
        // 例如：continew-admin/continew-system/src/main/java/top/continew/admin/system
        return String.join(File.separator, projectProperties.getAppName(), projectProperties.getAppName(), genConfig
            .getModuleName(), "src", "main", "java", genConfig.getPackageName()
                .replace(StringConstants.DOT, File.separator));
    }

    /**
     * 获取前端文件名
     *
     * @param classNamePrefix 类名前缀
     * @param className       类名
     * @param extension       扩展名
     * @return 前端文件名
     */
    private String getFrontendFileName(String classNamePrefix, String className, String extension) {
        return (".ts".equals(extension) ? StrUtil.lowerFirst(classNamePrefix) : className) + extension;
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
                              Map.Entry<String, GeneratorProperties.TemplateConfig> templateConfigEntry) {
        GeneratorProperties.TemplateConfig templateConfig = templateConfigEntry.getValue();
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
