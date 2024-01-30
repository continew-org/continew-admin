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

package top.charles7c.continew.admin.common.config.tlog;

import com.yomahub.tlog.id.TLogIdGeneratorLoader;
import com.yomahub.tlog.spring.TLogPropertyInit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.charles7c.continew.admin.common.config.properties.TLogProperties;

/**
 * TLog 配置
 *
 * <p>
 * 重写 TLog 配置以适配 Spring Boot 3.x
 * </p>
 *
 * @see TLogConfiguration
 * @author Jasmine
 * @since 2024/1/30 11:39
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TLogProperties.class)
public class TLogConfiguration {

    private final TLogProperties tLogProperties;

    @Bean
    @Primary
    public TLogPropertyInit tLogPropertyInit() {
        TLogPropertyInit tLogPropertyInit = new TLogPropertyInit();
        tLogPropertyInit.setPattern(tLogProperties.getPattern());
        tLogPropertyInit.setEnableInvokeTimePrint(tLogProperties.getEnableInvokeTimePrint());
        tLogPropertyInit.setMdcEnable(tLogProperties.getMdcEnable());
        // 设置自定义 TraceId 生成器
        TLogIdGeneratorLoader.setIdGenerator(new TraceIdGenerator());
        return tLogPropertyInit;
    }
}
