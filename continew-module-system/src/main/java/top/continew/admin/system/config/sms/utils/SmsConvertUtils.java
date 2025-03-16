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

package top.continew.admin.system.config.sms.utils;

import org.dromara.sms4j.aliyun.config.AlibabaConfig;
import org.dromara.sms4j.cloopen.config.CloopenConfig;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.ctyun.config.CtyunConfig;
import org.dromara.sms4j.provider.config.BaseConfig;
import top.continew.admin.system.model.resp.SmsConfigDetailResp;
import top.continew.admin.system.model.resp.SmsConfigResp;

public class SmsConvertUtils {

    public static BaseConfig smsConfig2BaseConfig(SmsConfigResp smsConfig) {
        switch (smsConfig.getSupplier()) {
            case SupplierConstant.ALIBABA: {
                AlibabaConfig alibabaConfig = new AlibabaConfig();
                alibabaConfig.setConfigId(smsConfig.getId().toString());
                alibabaConfig.setAccessKeyId(smsConfig.getAccessKeyId());
                alibabaConfig.setAccessKeySecret(smsConfig.getAccessKeySecret());
                alibabaConfig.setSignature(smsConfig.getSignature());
                alibabaConfig.setTemplateId(smsConfig.getTemplateId());
                return alibabaConfig;
            }
            case SupplierConstant.CLOOPEN: {
                CloopenConfig config = new CloopenConfig();
                config.setConfigId(smsConfig.getId().toString());
                config.setAccessKeyId(smsConfig.getAccessKeyId());
                config.setAccessKeySecret(smsConfig.getAccessKeySecret());
                config.setSignature(smsConfig.getSignature());
                config.setTemplateId(smsConfig.getTemplateId());
                return config;
            }
            case SupplierConstant.CTYUN: {
                CtyunConfig config = new CtyunConfig();
                config.setConfigId(smsConfig.getId().toString());
                config.setAccessKeyId(smsConfig.getAccessKeyId());
                config.setAccessKeySecret(smsConfig.getAccessKeySecret());
                config.setSignature(smsConfig.getSignature());
                config.setTemplateId(smsConfig.getTemplateId());
                return config;
            }
        }
        throw new RuntimeException("短信配置有错误，未知的供应商");
    }

    public static BaseConfig smsConfig2BaseConfig(SmsConfigDetailResp smsConfig) {
        switch (smsConfig.getSupplier()) {
            case SupplierConstant.ALIBABA: {
                AlibabaConfig alibabaConfig = new AlibabaConfig();
                alibabaConfig.setConfigId(smsConfig.getId().toString());
                alibabaConfig.setAccessKeyId(smsConfig.getAccessKeyId());
                alibabaConfig.setAccessKeySecret(smsConfig.getAccessKeySecret());
                alibabaConfig.setSignature(smsConfig.getSignature());
                alibabaConfig.setTemplateId(smsConfig.getTemplateId());
                return alibabaConfig;
            }
            case SupplierConstant.CLOOPEN: {
                CloopenConfig config = new CloopenConfig();
                config.setConfigId(smsConfig.getId().toString());
                config.setAccessKeyId(smsConfig.getAccessKeyId());
                config.setAccessKeySecret(smsConfig.getAccessKeySecret());
                config.setSignature(smsConfig.getSignature());
                config.setTemplateId(smsConfig.getTemplateId());
                return config;
            }
            case SupplierConstant.CTYUN: {
                CtyunConfig config = new CtyunConfig();
                config.setConfigId(smsConfig.getId().toString());
                config.setAccessKeyId(smsConfig.getAccessKeyId());
                config.setAccessKeySecret(smsConfig.getAccessKeySecret());
                config.setSignature(smsConfig.getSignature());
                config.setTemplateId(smsConfig.getTemplateId());
                return config;
            }
        }
        throw new RuntimeException("短信配置有错误，未知的供应商");
    }
}
