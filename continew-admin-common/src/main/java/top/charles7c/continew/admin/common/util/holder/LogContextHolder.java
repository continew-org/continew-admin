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

package top.charles7c.continew.admin.common.util.holder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import top.charles7c.continew.admin.common.model.dto.LogContext;

/**
 * 系统日志上下文持有者
 *
 * @author Charles7c
 * @since 2022/12/25 8:55
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogContextHolder {

    private static final ThreadLocal<LogContext> LOG_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 存储系统日志上下文
     *
     * @param logContext
     *            系统日志上下文信息
     */
    public static void set(LogContext logContext) {
        LOG_THREAD_LOCAL.set(logContext);
    }

    /**
     * 获取系统日志上下文
     *
     * @return 系统日志上下文信息
     */
    public static LogContext get() {
        return LOG_THREAD_LOCAL.get();
    }

    /**
     * 移除系统日志上下文
     */
    public static void remove() {
        LOG_THREAD_LOCAL.remove();
    }

    /**
     * 在系统日志上下文中保存异常信息
     *
     * @param e
     *            异常信息
     */
    public static void setException(Throwable e) {
        LogContext logContext = get();
        if (null != logContext) {
            logContext.setErrorMsg(e.getMessage());
            logContext.setException(e);
        }
    }

    /**
     * 在系统日志上下文中保存错误信息（非未知异常不记录异常信息，只记录错误信息）
     *
     * @param errorMsg
     *            错误信息
     */
    public static void setErrorMsg(String errorMsg) {
        LogContext logContext = get();
        if (null != logContext) {
            logContext.setErrorMsg(errorMsg);
        }
    }
}
