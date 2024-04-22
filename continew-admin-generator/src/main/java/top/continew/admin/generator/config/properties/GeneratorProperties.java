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

package top.continew.admin.generator.config.properties;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.map.MapUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.continew.starter.data.core.enums.DatabaseType;

import java.util.List;
import java.util.Map;

/**
 * 代码生成器配置属性
 *
 * @author Charles7c
 * @since 2023/8/5 11:08
 */
@Data
@Component
@ConfigurationProperties(prefix = "generator")
public class GeneratorProperties {

    /**
     * 排除数据表（哪些数据表不展示在代码生成中）
     */
    private String[] excludeTables = new String[0];

    /**
     * 类型映射
     */
    private Map<DatabaseType, Map<String, List<String>>> typeMappings = MapUtil.newHashMap();

    /**
     * 模板配置
     */
    private Map<String, TemplateConfig> templateConfigs = MapUtil.newHashMap(true);

    /**
     * 模板配置
     */
    @Data
    public static class TemplateConfig {

        /**
         * 模板路径
         */
        private String templatePath;

        /**
         * 包名称
         */
        private String packageName;

        /**
         * 排除字段
         */
        private String[] excludeFields;

        /**
         * 扩展名
         */
        private String extension = FileNameUtil.EXT_JAVA;

        /**
         * 是否为后端模板
         */
        private boolean backend = true;
    }
}
