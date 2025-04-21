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

package top.continew.admin.system.sms;

import org.dromara.sms4j.provider.factory.AbstractProviderFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlibabaV3Factory extends AbstractProviderFactory<AlibabaV3SmsImpl, AlibabaV3Config> {

    private static final AlibabaV3Factory INSTANCE = new AlibabaV3Factory();

    /**
     * 获取建造者实例
     * 
     * @return 建造者实例
     */
    public static AlibabaV3Factory instance() {
        return INSTANCE;
    }

    /**
     * 创建短信实现对象
     * 
     * @param alibabaConfig 短信配置对象
     * @return 短信实现对象
     */
    @Override
    public AlibabaV3SmsImpl createSms(AlibabaV3Config alibabaConfig) {
        return new AlibabaV3SmsImpl(alibabaConfig);
    }

    /**
     * 获取供应商
     * 
     * @return 供应商
     */
    @Override
    public String getSupplier() {
        return AlibabaV3Config.SUPPLIER;
    }

}