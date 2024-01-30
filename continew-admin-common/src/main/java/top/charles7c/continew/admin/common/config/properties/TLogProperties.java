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

package top.charles7c.continew.admin.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * TLog 配置属性
 *
 * @author Jasmine
 * @since 2024/01/30 11:39
 */
@Data
@Component
@ConfigurationProperties(prefix = "tlog")
public class TLogProperties {
    /** 日志标签模板 */
    private String pattern;
    /** 自动打印调用参数和时间 */
    private Boolean enableInvokeTimePrint;
    /** 自定义TraceId生成器 */
    private String idGenerator;
    /** MDC模式 */
    private Boolean mdcEnable;
}
