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

package top.continew.admin.system.service;

import top.continew.admin.system.model.entity.SmsConfigDO;
import top.continew.admin.system.model.query.SmsConfigQuery;
import top.continew.admin.system.model.req.SmsConfigReq;
import top.continew.admin.system.model.resp.SmsConfigResp;
import top.continew.starter.extension.crud.service.BaseService;

/**
 * 短信配置业务接口
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
public interface SmsConfigService extends BaseService<SmsConfigResp, SmsConfigResp, SmsConfigQuery, SmsConfigReq> {

    /**
     * 设置默认配置
     *
     * @param id ID
     */
    void setDefaultConfig(Long id);

    /**
     * 获取默认短信配置
     *
     * @return 默认短信配置
     */
    SmsConfigDO getDefaultConfig();
}