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

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.core.proxy.SmsProxyFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsBlendConfig {

    @Resource
    SmsJdbcReadConfig config;

    @Resource
    private SmsRecordProcessor smsRecordProcessor;

    @EventListener
    public void init(ContextRefreshedEvent event) {
        log.info("初始化短信配置");
        // 创建SmsBlend 短信实例
        SmsFactory.createSmsBlend(config);
        SmsProxyFactory.addPreProcessor(smsRecordProcessor);
        log.info("初始化短信配置完成");
    }
}
