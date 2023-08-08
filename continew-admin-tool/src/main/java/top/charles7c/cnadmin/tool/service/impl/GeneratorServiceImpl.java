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

package top.charles7c.cnadmin.tool.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.meta.Column;

import top.charles7c.cnadmin.common.constant.StringConsts;
import top.charles7c.cnadmin.common.enums.QueryTypeEnum;
import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.tool.config.properties.GeneratorProperties;
import top.charles7c.cnadmin.tool.enums.FormTypeEnum;
import top.charles7c.cnadmin.tool.mapper.ColumnMappingMapper;
import top.charles7c.cnadmin.tool.mapper.GenConfigMapper;
import top.charles7c.cnadmin.tool.model.entity.ColumnMappingDO;
import top.charles7c.cnadmin.tool.model.entity.GenConfigDO;
import top.charles7c.cnadmin.tool.model.query.TableQuery;
import top.charles7c.cnadmin.tool.model.request.GenConfigRequest;
import top.charles7c.cnadmin.tool.model.vo.TableVO;
import top.charles7c.cnadmin.tool.service.GeneratorService;
import top.charles7c.cnadmin.tool.util.MetaUtils;
import top.charles7c.cnadmin.tool.util.Table;

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
    private final ColumnMappingMapper columnMappingMapper;
    private final GenConfigMapper genConfigMapper;

    @Override
    public PageDataVO<TableVO> pageTable(TableQuery query, PageQuery pageQuery) throws SQLException {
        List<Table> tableList = MetaUtils.getTables(dataSource);
        String tableName = query.getTableName();
        if (StrUtil.isNotBlank(tableName)) {
            tableList.removeIf(table -> !StrUtil.containsAny(table.getTableName(), tableName));
        }
        tableList.removeIf(table -> StrUtil.equalsAny(table.getTableName(), generatorProperties.getExcludeTables()));
        List<TableVO> tableVOList = BeanUtil.copyToList(tableList, TableVO.class);
        return PageDataVO.build(pageQuery.getPage(), pageQuery.getSize(), tableVOList);
    }

    @Override
    public GenConfigDO getGenConfig(String tableName) throws SQLException {
        GenConfigDO genConfig = genConfigMapper.selectById(tableName);
        if (null == genConfig) {
            genConfig = new GenConfigDO(tableName);
            // 默认包名（当前包名）
            String packageName = ClassUtil.getPackage(GeneratorService.class);
            genConfig.setPackageName(StrUtil.subBefore(packageName, StringConsts.DOT, true));
            // 默认业务名（表注释）
            List<Table> tableList = MetaUtils.getTables(dataSource, tableName);
            if (CollUtil.isNotEmpty(tableList)) {
                Table table = tableList.get(0);
                genConfig.setBusinessName(StrUtil.replace(table.getComment(), "表", StringConsts.EMPTY));
            }
            // 默认作者名称（上次保存使用的作者名称）
            GenConfigDO lastGenConfig = genConfigMapper.selectOne(
                Wrappers.lambdaQuery(GenConfigDO.class).orderByDesc(GenConfigDO::getCreateTime).last("LIMIT 1"));
            if (null != lastGenConfig) {
                genConfig.setAuthor(lastGenConfig.getAuthor());
            }
            // 默认表前缀（sys_user -> sys_）
            int underLineIndex = StrUtil.indexOf(tableName, StringConsts.C_UNDERLINE);
            if (-1 != underLineIndex) {
                genConfig.setTablePrefix(StrUtil.subPre(tableName, underLineIndex + 1));
            }
        }
        return genConfig;
    }

    @Override
    public List<ColumnMappingDO> listColumnMapping(String tableName) {
        List<ColumnMappingDO> columnMappingList = columnMappingMapper
            .selectList(Wrappers.lambdaQuery(ColumnMappingDO.class).eq(ColumnMappingDO::getTableName, tableName));
        if (CollUtil.isEmpty(columnMappingList)) {
            Collection<Column> columnList = MetaUtils.getColumns(dataSource, tableName);
            columnMappingList = new ArrayList<>(columnList.size());
            for (Column column : columnList) {
                String columnType = StrUtil.splitToArray(column.getTypeName(), StringConsts.SPACE)[0];
                boolean isRequired = !column.isPk() && !column.isNullable();
                ColumnMappingDO columnMapping = new ColumnMappingDO(tableName).setColumnName(column.getName())
                    .setColumnType(columnType.toLowerCase()).setComment(column.getComment()).setIsRequired(isRequired)
                    .setShowInList(true).setShowInForm(isRequired).setShowInQuery(isRequired)
                    .setFormType(FormTypeEnum.TEXT);
                columnMapping.setQueryType(
                    "String".equals(columnMapping.getFieldType()) ? QueryTypeEnum.INNER_LIKE : QueryTypeEnum.EQUAL);
                columnMappingList.add(columnMapping);
            }
        }
        return columnMappingList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(GenConfigRequest request, String tableName) {
        // 保存列映射信息
        columnMappingMapper
            .delete(Wrappers.lambdaQuery(ColumnMappingDO.class).eq(ColumnMappingDO::getTableName, tableName));
        List<ColumnMappingDO> columnMappingList = request.getColumnMappings();
        for (ColumnMappingDO columnMapping : columnMappingList) {
            if (columnMapping.getShowInForm()) {
                CheckUtils.throwIfNull(columnMapping.getFormType(), "字段 [{}] 的表单类型不能为空", columnMapping.getFieldName());
            } else {
                // 在表单中不显示，不需要设置必填
                columnMapping.setIsRequired(false);
            }
            if (columnMapping.getShowInQuery()) {
                CheckUtils.throwIfNull(columnMapping.getFormType(), "字段 [{}] 的表单类型不能为空", columnMapping.getFieldName());
                CheckUtils.throwIfNull(columnMapping.getQueryType(), "字段 [{}] 的查询方式不能为空", columnMapping.getFieldName());
            } else {
                // 在查询中不显示，不需要设置查询方式
                columnMapping.setQueryType(null);
            }
            // 既不在表单也不在查询中显示，不需要设置表单类型
            if (!columnMapping.getShowInForm() && !columnMapping.getShowInQuery()) {
                columnMapping.setFormType(null);
            }
            columnMapping.setTableName(tableName);
        }
        columnMappingMapper.insertBatch(columnMappingList);

        // 保存或更新生成配置信息
        GenConfigDO newGenConfig = request.getGenConfig();
        GenConfigDO oldGenConfig = genConfigMapper.selectById(tableName);
        if (null != oldGenConfig) {
            BeanUtil.copyProperties(newGenConfig, oldGenConfig);
            genConfigMapper.updateById(oldGenConfig);
        } else {
            genConfigMapper.insert(newGenConfig);
        }
    }
}
