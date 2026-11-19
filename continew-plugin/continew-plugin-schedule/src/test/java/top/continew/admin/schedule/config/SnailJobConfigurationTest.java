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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.aizuda.snailjob.client.common.appender.SnailLogbackAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;

/**
 * Snail Job 配置测试。
 *
 * <p>验证客户端启动事件触发后，日志上报 Appender 完成启动并挂载到根 Logger。
 * LoggerFactory 经静态模拟返回受控的 LoggerContext，Appender 的 start()
 * 无外部副作用，按真实调用执行。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class SnailJobConfigurationTest {

    private MockedStatic<LoggerFactory> loggerFactoryMock;

    @BeforeEach
    void setUp() {
        loggerFactoryMock = mockStatic(LoggerFactory.class);
    }

    @AfterEach
    void tearDown() {
        loggerFactoryMock.close();
    }

    @Test
    void shouldAttachStartedAppenderToRootLoggerOnStarting() {
        LoggerContext loggerContext = mock(LoggerContext.class);
        Logger rootLogger = mock(Logger.class);
        when(loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)).thenReturn(rootLogger);
        loggerFactoryMock.when(LoggerFactory::getILoggerFactory).thenReturn(loggerContext);

        new SnailJobConfiguration().onStarting();

        ArgumentCaptor<SnailLogbackAppender> appenderCaptor = ArgumentCaptor
            .forClass(SnailLogbackAppender.class);
        verify(rootLogger).addAppender(appenderCaptor.capture());
        assertTrue(appenderCaptor.getValue().isStarted());
    }
}
