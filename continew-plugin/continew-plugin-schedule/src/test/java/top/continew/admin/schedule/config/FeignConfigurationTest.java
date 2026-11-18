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

import static org.junit.jupiter.api.Assertions.assertEquals;

import feign.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.schedule.api.JobClient;
import top.continew.starter.core.autoconfigure.application.ApplicationProperties;

/**
 * Feign 配置测试。
 *
 * <p>覆盖调度客户端 Bean 以配置的 URL/用户名/密码构造，以及 Feign 日志级别
 * 随应用运行环境（生产 BASIC、非生产 FULL）切换的两个真实分支。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class FeignConfigurationTest {

    private ApplicationProperties applicationProperties;
    private FeignConfiguration configuration;

    @BeforeEach
    void setUp() {
        applicationProperties = new ApplicationProperties();
        configuration = new FeignConfiguration(applicationProperties);
        ReflectionTestUtils.setField(configuration, "baseUrl", "http://localhost:8800");
        ReflectionTestUtils.setField(configuration, "username", "snail-job");
        ReflectionTestUtils.setField(configuration, "password", "snail-job");
    }

    @Test
    void shouldBuildJobClientWithConfiguredCredentials() {
        JobClient jobClient = configuration.jobClient();

        assertEquals("http://localhost:8800", jobClient.getUrl());
        assertEquals("snail-job", jobClient.getUsername());
        assertEquals("snail-job", jobClient.getPassword());
    }

    @Test
    void shouldUseBasicLogLevelInProduction() {
        applicationProperties.setProduction(true);

        assertEquals(Logger.Level.BASIC, configuration.feignLoggerLevel());
    }

    @Test
    void shouldUseFullLogLevelOutsideProduction() {
        applicationProperties.setProduction(false);

        assertEquals(Logger.Level.FULL, configuration.feignLoggerLevel());
    }
}
