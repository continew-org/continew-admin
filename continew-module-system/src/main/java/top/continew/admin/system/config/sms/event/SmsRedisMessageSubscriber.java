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

package top.continew.admin.system.config.sms.event;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.continew.admin.system.config.sms.SmsJdbcReadConfig;

@Slf4j
@Component
public class SmsRedisMessageSubscriber implements MessageListener {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SmsJdbcReadConfig smsJdbcReadConfig;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();
        SmsEventMessage msg = (SmsEventMessage)redisTemplate.getValueSerializer().deserialize(body);
        switch (msg.getType()) {
            case ADD:
                BaseConfig config = smsJdbcReadConfig.getSupplierConfig(msg.getConfigId());
                if (config != null) {
                    SmsFactory.createSmsBlend(config);
                }
                break;
            case UPDATE:
                BaseConfig updateConfig = smsJdbcReadConfig.getSupplierConfig(msg.getConfigId());
                // 若是存在该配置，则先删除
                if (SmsFactory.getSmsBlend(msg.getConfigId()) != null) {
                    SmsFactory.unregister(msg.getConfigId());
                }

                if (updateConfig != null) {
                    SmsFactory.createSmsBlend(updateConfig);
                }
                break;
            case DELETE:
                SmsFactory.unregister(msg.getConfigId());
                break;
            default:
                break;
        }
    }
}
