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

package top.continew.admin.config.satoken;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 密码配置属性
 *
 * @author Charles7c
 * @since 3.1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth.password")
public class LoginPasswordProperties {

    /**
     * 过期与闲置两类判定共用的自助通道路径
     *
     * <p>密码过期拦截（SaTokenConfiguration）与闲置失效判定（SaExtensionInterceptor）
     * 共用本清单：自助请求要求完整凭据，放行不放大风险。增删路径时两类用户同时生效；
     * 闲置豁免需覆盖全部密码豁免路径，否则密码过期且闲置的用户无法完成自助操作。</p>
     */
    private String[] excludes = new String[0];
}
