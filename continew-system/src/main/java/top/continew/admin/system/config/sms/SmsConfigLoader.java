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

package top.continew.admin.system.config.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.core.proxy.SmsProxyFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 短信配置加载器
 *
 * @author luoqiz
 * @author Charles7c
 * @since 2025/03/15 22:15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsConfigLoader implements ApplicationRunner {

    private final SmsReadConfigDatabaseImpl smsReadConfig;
    private final SmsLogProcessor smsLogProcessor;

    @Override
    public void run(ApplicationArguments args) {
        SmsFactory.createSmsBlend(smsReadConfig);
        SmsProxyFactory.addPreProcessor(smsLogProcessor);
        log.debug("短信配置初始化完成");
    }
}
