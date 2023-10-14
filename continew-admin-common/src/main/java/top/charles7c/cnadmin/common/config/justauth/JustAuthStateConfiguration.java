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

package top.charles7c.cnadmin.common.config.justauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import me.zhyd.oauth.cache.AuthStateCache;

/**
 * Just Auth State 缓存配置
 *
 * @author Charles7c
 * @since 2023/10/8 22:17
 */
@Configuration
public class JustAuthStateConfiguration {

    /**
     * Just Auth State 缓存 Redis 适配
     */
    @Bean
    public AuthStateCache authStateCache() {
        return new JustAuthRedisStateCache();
    }
}