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

import org.dromara.sms4j.provider.config.BaseConfig;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AlibabaV3Config extends BaseConfig {

    public static final String SUPPLIER = "alibaba-v3";

    /**
     * 模板变量名称
     */
    private String templateName;

    /**
     * 请求地址
     */
    private String requestUrl = "dysmsapi.aliyuncs.com";

    /**
     * 接口名称
     */
    private String action = "SendSms";

    /**
     * 接口版本号
     */
    private String version = "2017-05-25";

    /**
     * 地域信息默认为 cn-hangzhou
     */
    private String regionId = "cn-hangzhou";

    /**
     * 获取供应商
     *
     * @since 3.0.0
     */
    @Override
    public String getSupplier() {
        return SUPPLIER;
    }

}
