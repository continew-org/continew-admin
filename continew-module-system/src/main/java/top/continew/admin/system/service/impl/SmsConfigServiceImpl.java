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

package top.continew.admin.system.service.impl;

import lombok.RequiredArgsConstructor;

import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.stereotype.Service;

import top.continew.admin.system.config.sms.SmsConfigUtil;
import top.continew.admin.system.mapper.SmsConfigMapper;
import top.continew.admin.system.model.entity.SmsConfigDO;
import top.continew.admin.system.model.query.SmsConfigQuery;
import top.continew.admin.system.model.req.SmsConfigReq;
import top.continew.admin.system.model.resp.SmsConfigResp;
import top.continew.admin.system.service.SmsConfigService;
import top.continew.starter.extension.crud.service.BaseServiceImpl;

import java.util.List;
import java.util.Objects;

/**
 * 短信配置业务实现
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Service
@RequiredArgsConstructor
public class SmsConfigServiceImpl extends BaseServiceImpl<SmsConfigMapper, SmsConfigDO, SmsConfigResp, SmsConfigResp, SmsConfigQuery, SmsConfigReq> implements SmsConfigService {

    @Override
    public void afterCreate(SmsConfigReq req, SmsConfigDO entity) {
        this.load(entity);
    }

    @Override
    public void afterUpdate(SmsConfigReq req, SmsConfigDO entity) {
        // 重新加载配置
        // 先卸载
        this.unload(entity.getId().toString());
        // 再加载
        this.load(entity);
    }

    @Override
    public void afterDelete(List<Long> ids) {
        for (Long id : ids) {
            this.unload(id.toString());
        }
    }

    /**
     * 加载配置
     *
     * @param entity 配置信息
     */
    private void load(SmsConfigDO entity) {
        SmsConfigResp smsConfig = this.get(entity.getId());

        BaseConfig config = SmsConfigUtil.from(smsConfig);
        if (Objects.nonNull(config))
            SmsFactory.createSmsBlend(config);
    }

    /**
     * 卸载配置
     *
     * @param configId 配置 ID
     */
    private void unload(String configId) {
        if (SmsFactory.getSmsBlend(configId) != null) {
            SmsFactory.unregister(configId);
        }
    }
}