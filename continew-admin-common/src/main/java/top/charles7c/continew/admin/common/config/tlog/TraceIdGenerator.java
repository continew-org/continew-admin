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

import com.yomahub.tlog.id.TLogIdGenerator;

/**
 * TLog 配置
 *
 * @see TraceIdGenerator
 * @author Jasmine
 * @since 2024/01/30 11:39
 */
public class TraceIdGenerator extends TLogIdGenerator {
    @Override
    public String generateTraceId() {
        String traceId = String.valueOf(System.nanoTime());
        return traceId;
    }
}