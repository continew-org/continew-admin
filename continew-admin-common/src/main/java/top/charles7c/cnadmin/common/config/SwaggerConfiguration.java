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

package top.charles7c.cnadmin.common.config;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;

import top.charles7c.cnadmin.common.config.properties.ProjectProperties;

/**
 * 接口文档配置
 *
 * @author Charles7c
 * @since 2022/12/11 19:14
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration {

    private final ProjectProperties projectProperties;

    /**
     * 接口文档配置
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info().title(projectProperties.getName() + " 接口文档")
            .version(String.format("v%s", projectProperties.getVersion()))
            .description(projectProperties.getDescription()).termsOfService(projectProperties.getUrl())
            .contact(projectProperties.getAuthor()).license(projectProperties.getLicense()));
    }

    /**
     * 根据 @Tag 上的排序，写入 x-order
     *
     * @return the global open api customizer
     */
    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            if (null != openApi.getTags()) {
                openApi.getTags()
                    .forEach(tag -> tag.setExtensions(MapUtil.of("x-order", RandomUtil.randomInt(0, 100))));
            }
        };
    }
}
