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

package top.charles7c.cnadmin.common.util.holder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import top.charles7c.cnadmin.common.model.dto.OperationLog;

/**
 * 操作日志上下文持有者
 *
 * @author Charles7c
 * @since 2022/12/25 8:55
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogContextHolder {

    private static final ThreadLocal<OperationLog> LOG_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 存储操作日志
     *
     * @param operationLog
     *            操作日志信息
     */
    public static void set(OperationLog operationLog) {
        LOG_THREAD_LOCAL.set(operationLog);
    }

    /**
     * 获取操作日志
     *
     * @return 操作日志信息
     */
    public static OperationLog get() {
        return LOG_THREAD_LOCAL.get();
    }

    /**
     * 移除操作日志
     */
    public static void remove() {
        LOG_THREAD_LOCAL.remove();
    }
}
