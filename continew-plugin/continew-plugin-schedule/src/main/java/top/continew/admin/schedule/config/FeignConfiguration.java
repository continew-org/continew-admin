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

package top.continew.admin.schedule.config;

import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.continew.admin.schedule.api.JobClient;
import top.continew.starter.core.autoconfigure.project.ProjectProperties;

/**
 * Feign 配置
 *
 * @author Charles7c
 * @since 2025/3/28 21:17
 */
@Configuration
@RequiredArgsConstructor
public class FeignConfiguration {

    private final ProjectProperties projectProperties;

    @Value("${snail-job.server.api.url}")
    private String baseUrl;

    @Value("${snail-job.server.api.username}")
    private String username;

    @Value("${snail-job.server.api.password}")
    private String password;

    /**
     * 调度客户端
     */
    @Bean
    public JobClient jobClient() {
        return new JobClient(baseUrl, username, password);
    }

    /**
     * Feign 日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return projectProperties.isProduction() ? Logger.Level.BASIC : Logger.Level.FULL;
    }
}