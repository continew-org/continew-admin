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
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.stereotype.Component;
import top.continew.admin.system.config.sms.utils.SmsConvertUtils;
import top.continew.admin.system.model.query.SmsConfigQuery;
import top.continew.admin.system.model.resp.SmsConfigDetailResp;
import top.continew.admin.system.model.resp.SmsConfigResp;
import top.continew.admin.system.service.SmsConfigService;
import top.continew.starter.extension.crud.model.query.SortQuery;

import java.util.ArrayList;
import java.util.List;

@Component
public class SmsJdbcReadConfig implements SmsReadConfig {

    @Resource
    private SmsConfigService smsConfigService;

    @Override
    public BaseConfig getSupplierConfig(String configId) {
        Long id = Long.parseLong(configId);
        SmsConfigDetailResp smsConfig = smsConfigService.get(id);
        if (smsConfig == null || !smsConfig.getIsEnable()) {
            return null;
        }
        return SmsConvertUtils.smsConfig2BaseConfig(smsConfig);
    }

    @Override
    public List<BaseConfig> getSupplierConfigList() {
        SmsConfigQuery smsConfigQuery = new SmsConfigQuery();
        smsConfigQuery.setIsEnable(true);
        SortQuery sortQuery = new SortQuery();
        sortQuery.setSort(new String[] {"id", "desc"});
        List<SmsConfigResp> smsConfigList = smsConfigService.list(smsConfigQuery, sortQuery);
        List<BaseConfig> baseConfigList = new ArrayList<>();
        for (SmsConfigResp smsConfigResp : smsConfigList) {
            baseConfigList.add(SmsConvertUtils.smsConfig2BaseConfig(smsConfigResp));
        }
        return baseConfigList;
    }

}