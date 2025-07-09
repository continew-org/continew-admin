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

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.stereotype.Component;

import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.query.SmsConfigQuery;
import top.continew.admin.system.model.resp.SmsConfigResp;
import top.continew.admin.system.service.SmsConfigService;

import java.util.List;

/**
 * 短信配置读取-数据源实现
 *
 * @author luoqiz
 * @author Charles7c
 * @since 2025/03/15 22:15
 */
@Component
@RequiredArgsConstructor
public class SmsReadConfigDatabaseImpl implements SmsReadConfig {

    private final SmsConfigService smsConfigService;

    @Override
    public BaseConfig getSupplierConfig(String configId) {
        Long id = Long.parseLong(configId);
        SmsConfigResp smsConfig = smsConfigService.get(id);
        if (DisEnableStatusEnum.DISABLE.equals(smsConfig.getStatus())) {
            return null;
        }
        return SmsConfigUtil.from(smsConfig);
    }

    @Override
    public List<BaseConfig> getSupplierConfigList() {
        SmsConfigQuery query = new SmsConfigQuery();
        query.setStatus(DisEnableStatusEnum.ENABLE);
        List<SmsConfigResp> list = smsConfigService.list(query, null);
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        return list.stream().map(SmsConfigUtil::from).toList();
    }
}