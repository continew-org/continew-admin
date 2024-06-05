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

package top.continew.admin.system.config.mail;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.system.service.OptionService;
import top.continew.starter.messaging.mail.core.MailConfig;
import top.continew.starter.messaging.mail.core.MailConfigService;

import java.util.Map;

/**
 * 邮件配置实现
 *
 * @author Charles7c
 * @since 2024/5/30 22:32
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailConfigServiceImpl implements MailConfigService {

    private final OptionService optionService;

    @Override
    public MailConfig getMailConfig() {
        // 查询邮件配置
        Map<String, String> map = optionService.getByCategory("MAIL");
        // 封装邮件配置
        MailConfig mailConfig = new MailConfig();
        mailConfig.setProtocol(MapUtil.getStr(map, "MAIL_PROTOCOL"));
        mailConfig.setHost(MapUtil.getStr(map, "MAIL_HOST"));
        mailConfig.setPort(MapUtil.getInt(map, "MAIL_PORT"));
        mailConfig.setUsername(MapUtil.getStr(map, "MAIL_USERNAME"));
        mailConfig.setPassword(MapUtil.getStr(map, "MAIL_PASSWORD"));
        mailConfig.setSslEnabled(SysConstants.YES.equals(MapUtil.getInt(map, "MAIL_SSL_ENABLED")));
        if (mailConfig.isSslEnabled()) {
            mailConfig.setSslPort(MapUtil.getInt(map, "MAIL_SSL_PORT"));
        }
        return mailConfig;
    }
}
