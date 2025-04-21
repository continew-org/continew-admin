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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.provider.factory.BaseProviderFactory;
import org.dromara.sms4j.provider.factory.ProviderFactoryHolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import top.continew.admin.system.model.resp.SmsConfigResp;

/**
 * 短信配置工具类
 *
 * @author Top2Hub
 * @since 2025/04/21 14:00
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsConfigUtil {

    private static final TypeReference<Map<String, Object>> CONFIG_MAP_TYPE = new TypeReference<Map<String, Object>>() {};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 将本地配置对象转换为 BaseConfig
     * 
     * @param smsConfig 本地配置对象
     * @return SMS4j 配置基类
     */ 
    public static BaseConfig from(SmsConfigResp smsConfig) {
        if (Objects.isNull(smsConfig))
            return null;

        String supplierName = smsConfig.getSupplier();
        BaseProviderFactory<?, ?> providerFactory = ProviderFactoryHolder.requireForSupplier(supplierName);
        if (Objects.isNull(providerFactory))
            return null;

        Map<String, Object> configInfo = new HashMap<>();
        configInfo.put("configId", smsConfig.getId().toString());
        configInfo.put("accessKeyId", smsConfig.getAccessKey());
        configInfo.put("accessKeySecret", smsConfig.getSecretKey());
        configInfo.put("signature", smsConfig.getSignature());
        configInfo.put("templateId", smsConfig.getTemplateId());
        if (Objects.nonNull(smsConfig.getWeight()))
            configInfo.put("weight", smsConfig.getWeight());
        if (Objects.nonNull(smsConfig.getRetryInterval()))
            configInfo.put("retryInterval", smsConfig.getRetryInterval());
        if (Objects.nonNull(smsConfig.getMaxRetries()))
            configInfo.put("maxRetries", smsConfig.getMaxRetries());

        if (Objects.nonNull(smsConfig.getSupplierConfig())) {
            Map<String, Object> supplierInfo = OBJECT_MAPPER.convertValue(smsConfig
                .getSupplierConfig(), CONFIG_MAP_TYPE);
            configInfo.putAll(supplierInfo);
        }

        BaseConfig config = (BaseConfig)OBJECT_MAPPER.convertValue(configInfo, providerFactory.getConfigClass());
        return config;
    }

}
