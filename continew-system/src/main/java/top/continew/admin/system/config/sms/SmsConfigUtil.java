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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.provider.factory.BaseProviderFactory;
import org.dromara.sms4j.provider.factory.ProviderFactoryHolder;
import top.continew.admin.system.model.resp.SmsConfigResp;
import top.continew.starter.json.jackson.util.JSONUtils;

import java.util.Map;

/**
 * 短信配置工具类
 *
 * @author Top2Hub
 * @since 2025/04/21 14:00
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsConfigUtil {

    /**
     * 将本地配置转换为 SMS4J 配置
     * 
     * @param smsConfig 本地配置对象
     * @return SMS4J 配置基类
     */
    public static BaseConfig from(SmsConfigResp smsConfig) {
        if (smsConfig == null) {
            return null;
        }
        String supplier = smsConfig.getSupplier();
        BaseProviderFactory<?, ?> providerFactory = ProviderFactoryHolder.requireForSupplier(supplier);
        if (providerFactory == null) {
            return null;
        }
        // 构建配置
        Map<String, Object> configInfo = MapUtil.newHashMap();
        configInfo.put("configId", smsConfig.getId().toString());
        configInfo.put("accessKeyId", smsConfig.getAccessKey());
        configInfo.put("accessKeySecret", smsConfig.getSecretKey());
        configInfo.put("signature", smsConfig.getSignature());
        configInfo.put("templateId", smsConfig.getTemplateId());
        if (smsConfig.getWeight() != null) {
            configInfo.put("weight", smsConfig.getWeight());
        }
        if (smsConfig.getRetryInterval() != null) {
            configInfo.put("retryInterval", smsConfig.getRetryInterval());
        }
        if (smsConfig.getMaxRetries() != null) {
            configInfo.put("maxRetries", smsConfig.getMaxRetries());
        }
        if (StrUtil.isNotBlank(smsConfig.getSupplierConfig())) {
            configInfo.putAll(JSONUtils.toBean(smsConfig.getSupplierConfig(), Map.class));
        }
        return (BaseConfig)BeanUtil.toBean(configInfo, providerFactory.getConfigClass());
    }

}
