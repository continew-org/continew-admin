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

package top.continew.admin.job.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.aizuda.snailjob.client.common.appender.SnailLogbackAppender;
import com.aizuda.snailjob.client.common.event.SnailClientStartingEvent;
import com.aizuda.snailjob.client.starter.EnableSnailJob;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Snail Job 配置
 *
 * @author KAI
 * @since 2024/6/26 9:19
 */
@Configuration
@EnableSnailJob
@ConditionalOnProperty(prefix = "snail-job", value = "enabled", havingValue = "true", matchIfMissing = true)
public class SnailJobConfiguration {

    /**
     * 日志上报
     */
    @EventListener(SnailClientStartingEvent.class)
    public void onStarting() {
        SnailLogbackAppender<ILoggingEvent> appender = new SnailLogbackAppender<>();
        appender.start();
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
    }
}