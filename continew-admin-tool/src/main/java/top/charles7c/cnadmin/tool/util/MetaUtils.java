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

package top.charles7c.cnadmin.tool.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.MetaUtil;

/**
 * 数据库元数据信息工具类
 *
 * @author Charles7c
 * @since 2023/4/26 21:39
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetaUtils {

    /**
     * 获取所有表信息
     *
     * @param dataSource
     *            数据源
     * @return 表信息列表
     */
    public static List<Table> getTables(DataSource dataSource) throws SQLException {
        return getTables(dataSource, null);
    }

    /**
     * 获取所有表信息
     *
     * @param dataSource
     *            数据源
     * @param tableName
     *            表名称
     * @return 表信息列表
     */
    public static List<Table> getTables(DataSource dataSource, String tableName) throws SQLException {
        String querySql = "SHOW TABLE STATUS";
        List<Entity> tableEntityList;
        Db db = Db.use(dataSource);
        if (StrUtil.isNotBlank(tableName)) {
            tableEntityList = db.query(String.format("%s WHERE NAME = ?", querySql), tableName);
        } else {
            tableEntityList = db.query(querySql);
        }
        List<Table> tableList = new ArrayList<>(tableEntityList.size());
        for (Entity tableEntity : tableEntityList) {
            Table table = new Table(tableEntity.getStr("NAME"));
            table.setComment(tableEntity.getStr("COMMENT"));
            table.setEngine(tableEntity.getStr("ENGINE"));
            table.setCharset(tableEntity.getStr("COLLATION"));
            table.setCreateTime(DateUtil.toLocalDateTime(tableEntity.getDate("CREATE_TIME")));
            table.setUpdateTime(DateUtil.toLocalDateTime(tableEntity.getDate("UPDATE_TIME")));
            tableList.add(table);
        }
        return tableList;
    }

    /**
     * 获取所有列信息
     *
     * @param dataSource
     *            数据源
     * @param tableName
     *            表名称
     * @return 列信息列表
     */
    public static Collection<Column> getColumns(DataSource dataSource, String tableName) {
        cn.hutool.db.meta.Table table = MetaUtil.getTableMeta(dataSource, tableName);
        return table.getColumns();
    }
}
