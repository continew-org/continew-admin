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

package top.charles7c.cnadmin.common.config.threadpool;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import top.charles7c.cnadmin.common.util.ExceptionUtils;

/**
 * 线程池配置
 *
 * @author Charles7c
 * @since 2022/12/23 23:13
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ThreadPoolConfiguration {

    private final ThreadPoolProperties threadPoolProperties;
    /** 核心（最小）线程数 = CPU 核心数 + 1 */
    private final int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * Spring 内置线程池：ThreadPoolTaskExecutor
     */
    @Bean
    @ConditionalOnProperty(prefix = "thread-pool", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心（最小）线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(corePoolSize * 2);
        // 队列容量
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        // 活跃时间
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        // 配置当池内线程数已达到上限的时候，该如何处理新任务：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * Java 内置线程池：ScheduledExecutorService（适用于执行周期性或定时任务）
     */
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(corePoolSize,
            new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy()) {

            @Override
            protected void afterExecute(Runnable runnable, Throwable throwable) {
                super.afterExecute(runnable, throwable);
                ExceptionUtils.printException(runnable, throwable);
            }
        };
    }
}
