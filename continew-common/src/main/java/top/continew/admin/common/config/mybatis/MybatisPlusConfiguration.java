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

package top.continew.admin.common.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.continew.starter.extension.datapermission.provider.DataPermissionUserDataProvider;

import java.util.concurrent.TimeUnit;

/**
 * MyBatis Plus 配置
 *
 * @author Charles7c
 * @since 2022/12/22 19:51
 */
@Configuration
public class MybatisPlusConfiguration {

    // SQL 解析本地缓存
    static {
        JsqlParserGlobal
            .setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(cache -> cache.maximumSize(1024)
                .expireAfterWrite(5, TimeUnit.SECONDS)));
    }

    /**
     * 元对象处理器配置（插入或修改时自动填充）
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyBatisPlusMetaObjectHandler();
    }

    /**
     * 数据权限用户数据提供者
     */
    @Bean
    public DataPermissionUserDataProvider dataPermissionUserDataProvider() {
        return new DefaultDataPermissionUserDataProvider();
    }
}
