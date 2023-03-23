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

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import cn.hutool.core.util.ArrayUtil;

import top.charles7c.cnadmin.common.exception.ServiceException;

/**
 * 异步任务执行配置
 *
 * @author Lion Li（RuoYi-Vue-Plus）
 * @author Charles7c
 * @since 2022/12/23 22:33
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableAsync(proxyTargetClass = true)
public class AsyncConfiguration implements AsyncConfigurer {

    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * 异步任务 @Async 执行时，使用 Java 内置线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        return scheduledExecutorService;
    }

    /**
     * 异步任务执行时的异常处理
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            throwable.printStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("Exception message - ").append(throwable.getMessage()).append(", Method name - ")
                .append(method.getName());
            if (ArrayUtil.isNotEmpty(objects)) {
                sb.append(", Parameter value - ").append(Arrays.toString(objects));
            }
            throw new ServiceException(sb.toString());
        };
    }
}
