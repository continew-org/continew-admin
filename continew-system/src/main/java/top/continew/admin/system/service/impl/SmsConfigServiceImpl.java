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
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.config.sms.SmsConfigUtil;
import top.continew.admin.system.mapper.SmsConfigMapper;
import top.continew.admin.system.model.entity.SmsConfigDO;
import top.continew.admin.system.model.query.SmsConfigQuery;
import top.continew.admin.system.model.req.SmsConfigReq;
import top.continew.admin.system.model.resp.SmsConfigResp;
import top.continew.admin.system.service.SmsConfigService;
import top.continew.starter.core.validation.CheckUtils;
import top.continew.starter.extension.crud.service.BaseServiceImpl;

import java.util.List;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultConfig(Long id) {
        SmsConfigDO smsConfig = super.getById(id);
        if (Boolean.TRUE.equals(smsConfig.getIsDefault())) {
            return;
        }
        // 启用状态才能设为默认配置
        CheckUtils.throwIfNotEqual(DisEnableStatusEnum.ENABLE, smsConfig.getStatus(), "请先启用所选配置");
        baseMapper.lambdaUpdate().eq(SmsConfigDO::getIsDefault, true).set(SmsConfigDO::getIsDefault, false).update();
        baseMapper.lambdaUpdate().eq(SmsConfigDO::getId, id).set(SmsConfigDO::getIsDefault, true).update();
    }

    @Override
    public SmsConfigDO getDefaultConfig() {
        return baseMapper.lambdaQuery()
            .eq(SmsConfigDO::getIsDefault, true)
            .eq(SmsConfigDO::getStatus, DisEnableStatusEnum.ENABLE)
            .one();
    }

    /**
     * 加载配置
     *
     * @param entity 配置信息
     */
    private void load(SmsConfigDO entity) {
        SmsConfigResp smsConfig = this.get(entity.getId());
        BaseConfig config = SmsConfigUtil.from(smsConfig);
        if (config != null) {
            SmsFactory.createSmsBlend(config);
        }
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