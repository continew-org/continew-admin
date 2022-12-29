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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import top.charles7c.cnadmin.common.config.properties.CorsProperties;

/**
 * Web MVC 配置
 *
 * @author Charles7c
 * @since 2022/12/11 19:40
 */
@EnableWebMvc
@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final CorsProperties corsProperties;
    private final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    /**
     * 静态资源处理器配置
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
            .setCacheControl(CacheControl.maxAge(5, TimeUnit.HOURS).cachePublic());
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 配置为 true 后则必须配置允许跨域的域名，且不允许配置为 *
        config.setAllowCredentials(true);
        // 设置跨域允许时间
        config.setMaxAge(1800L);

        // 配置允许跨域的域名
        corsProperties.getAllowedOrigins().forEach(config::addAllowedOrigin);
        // 配置允许跨域的请求方式
        corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);
        // 配置允许跨域的请求头
        corsProperties.getAllowedHeaders().forEach(config::addAllowedHeader);

        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 解决 Jackson2ObjectMapperBuilderCustomizer 配置不生效的问题
     * <p>
     * MappingJackson2HttpMessageConverter 对象在程序启动时创建了多个，移除多余的，保证只有一个
     * </p>
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
        if (Objects.isNull(mappingJackson2HttpMessageConverter)) {
            converters.add(0, new MappingJackson2HttpMessageConverter());
        } else {
            converters.add(0, mappingJackson2HttpMessageConverter);
        }
    }
}
