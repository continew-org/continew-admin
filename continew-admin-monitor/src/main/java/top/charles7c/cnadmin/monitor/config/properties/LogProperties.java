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

package top.charles7c.cnadmin.monitor.config.properties;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 系统日志配置属性
 *
 * @author Charles7c
 * @since 2022/12/24 23:04
 */
@Data
@Component
@ConfigurationProperties(prefix = "logging.system")
public class LogProperties {

    /**
     * 是否启用系统日志
     */
    private Boolean enabled;

    /**
     * 是否记录内网 IP 操作
     */
    private Boolean includeInnerIp;

    /**
     * 哪些请求方式不记录系统日志
     */
    private List<String> excludeMethods = new ArrayList<>();

    /**
     * 脱敏字段
     */
    private List<String> desensitize = new ArrayList<>();
}
