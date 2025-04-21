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

package top.continew.admin.config.sms;

import org.dromara.sms4j.provider.factory.ProviderFactoryHolder;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import top.continew.admin.system.sms.AlibabaV3Factory;

@Configuration
@RequiredArgsConstructor
public class SmsPatchConfig {

    @PostConstruct
    private void initPatch() {
        ProviderFactoryHolder.registerFactory(AlibabaV3Factory.instance());
    }

}
