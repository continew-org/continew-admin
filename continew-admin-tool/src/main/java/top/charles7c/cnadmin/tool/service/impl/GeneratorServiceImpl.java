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
import java.util.List;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.tool.model.query.TableQuery;
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

    @Override
    public PageDataVO<TableVO> pageTable(TableQuery query, PageQuery pageQuery) throws SQLException {
        List<Table> tableList = MetaUtils.getTables(dataSource);
        String tableName = query.getTableName();
        if (StrUtil.isNotBlank(tableName)) {
            tableList.removeIf(table -> !StrUtil.containsAny(table.getTableName(), tableName));
        }
        tableList
            .removeIf(table -> StrUtil.equalsAny(table.getTableName(), "DATABASECHANGELOG", "DATABASECHANGELOGLOCK"));
        List<TableVO> tableVOList = BeanUtil.copyToList(tableList, TableVO.class);
        return PageDataVO.build(pageQuery.getPage(), pageQuery.getSize(), tableVOList);
    }
}
