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

package top.continew.admin.tenant.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import top.continew.starter.core.exception.BusinessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 数据连接工具类
 * @author: 小熊
 * @create: 2024-12-15 18:54
 */
public class DbConnectUtil {

    /**
     * 格式化HikariConfig
     */
    public static HikariConfig formatHikariConfig(String host,
                                                  Integer port,
                                                  String username,
                                                  String password,
                                                  String dbName,
                                                  Map<String, String> parameter) {
        String activeProfile = SpringUtil.getActiveProfile();
        String jdbcUrl = StrUtil.format("jdbc:mysql://{}:{}", host, port);
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        if ("dev".equals(activeProfile)) {
            jdbcUrl = StrUtil.format("jdbc:p6spy:mysql://{}:{}", host, port);
            driverClassName = "com.p6spy.engine.spy.P6SpyDriver";
        }
        if (StrUtil.isNotEmpty(dbName)) {
            jdbcUrl = StrUtil.format("{}/{}", jdbcUrl, dbName);
        }
        if (parameter != null) {
            jdbcUrl = StrUtil.format("{}?{}", jdbcUrl, MapUtil.join(parameter, "&", "="));
        }
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(jdbcUrl);
        configuration.setDriverClassName(driverClassName);
        configuration.setUsername(username);
        configuration.setPassword(password);
        configuration.setConnectionTimeout(3000L);
        return configuration;
    }

    /**
     * 验证mysql连接有效性并返回数据源
     */
    public static DataSource getMysqlDataSource(String host,
                                                Integer port,
                                                String username,
                                                String password,
                                                String dbName,
                                                Map<String, String> parameter) {
        try {
            DataSource dataSource = new HikariDataSource(formatHikariConfig(host, port, username, password, dbName, parameter));
            Connection connection = dataSource.getConnection();
            connection.close();
            return dataSource;
        } catch (Exception e) {
            throw new BusinessException("数据库连接失败,请检查基础配置信息");
        }
    }

    /**
     * 默认的mysql连接参数
     *
     * @return
     */
    public static Map<String, String> getDefaultMysqlConnectParameter() {
        Map<String, String> parameter = new HashMap<>();
        parameter.put("serverTimezone", "Asia/Shanghai");
        parameter.put("useUnicode", "true");
        parameter.put("characterEncoding", "utf8");
        parameter.put("useSSL", "false");
        parameter.put("allowMultiQueries", "true");
        parameter.put("autoReconnect", "true");
        parameter.put("maxReconnects", "10");
        parameter.put("failOverReadOnly", "false");
        return parameter;
    }

}
