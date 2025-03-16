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

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.enums.MethodTypeEnum;
import top.continew.admin.system.config.sms.event.SmsEventMessage;
import top.continew.admin.system.config.sms.event.SmsRedisConfig;
import top.continew.admin.system.config.sms.event.SmsRedisMessagePublisher;
import top.continew.admin.system.mapper.SmsConfigMapper;
import top.continew.admin.system.model.entity.SmsConfigDO;
import top.continew.admin.system.model.query.SmsConfigQuery;
import top.continew.admin.system.model.req.SmsConfigReq;
import top.continew.admin.system.model.resp.SmsConfigDetailResp;
import top.continew.admin.system.model.resp.SmsConfigResp;
import top.continew.admin.system.service.SmsConfigService;
import top.continew.starter.extension.crud.service.BaseServiceImpl;

import java.util.List;

/**
 * 短信服务配置业务实现
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Service
@RequiredArgsConstructor
public class SmsConfigServiceImpl extends BaseServiceImpl<SmsConfigMapper, SmsConfigDO, SmsConfigResp, SmsConfigDetailResp, SmsConfigQuery, SmsConfigReq> implements SmsConfigService {
    @Resource
    private SmsRedisMessagePublisher smsRedisMessagePublisher;

    @Override
    protected void afterAdd(SmsConfigReq req, SmsConfigDO entity) {
        super.afterAdd(req, entity);
        smsRedisMessagePublisher.publish(SmsRedisConfig.SysSmsChannel, new SmsEventMessage(MethodTypeEnum.ADD, entity
            .getId()
            .toString()));
    }

    @Override
    protected void afterUpdate(SmsConfigReq req, SmsConfigDO entity) {
        super.afterUpdate(req, entity);
        smsRedisMessagePublisher.publish(SmsRedisConfig.SysSmsChannel, new SmsEventMessage(MethodTypeEnum.UPDATE, entity
            .getId()
            .toString()));
    }

    @Override
    protected void afterDelete(List<Long> ids) {
        super.afterDelete(ids);
        for (Long id : ids) {
            smsRedisMessagePublisher.publish(SmsRedisConfig.SysSmsChannel, new SmsEventMessage(MethodTypeEnum.DELETE, id
                .toString()));
        }
    }
}