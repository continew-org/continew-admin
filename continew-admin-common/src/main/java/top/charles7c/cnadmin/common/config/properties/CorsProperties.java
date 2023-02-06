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

package top.charles7c.cnadmin.common.config.properties;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 跨域配置属性
 *
 * @author Charles7c
 * @since 2022/12/26 22:56
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * 允许跨域的域名
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * 允许跨域的请求方式
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * 允许跨域的请求头
     */
    private List<String> allowedHeaders = new ArrayList<>();

    /**
     * 允许跨域的响应头
     */
    private List<String> exposedHeaders = new ArrayList<>();
}
