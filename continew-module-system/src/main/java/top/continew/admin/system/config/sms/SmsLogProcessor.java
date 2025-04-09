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

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.api.proxy.CoreMethodProcessor;
import org.springframework.stereotype.Component;
import top.continew.admin.common.enums.SuccessFailureStatusEnum;
import top.continew.admin.system.model.req.SmsLogReq;
import top.continew.admin.system.service.SmsLogService;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 短信日志处理器
 *
 * @author luoqiz
 * @author Charles7c
 * @since 2025/03/15 22:15
 */
@Component
@RequiredArgsConstructor
public class SmsLogProcessor implements CoreMethodProcessor {

    private final SmsLogService smsLogService;

    @Override
    public Object postProcessor(SmsResponse result, Object[] param) {
        if (NumberUtil.isNumber(result.getConfigId())) {
            SmsLogReq req = new SmsLogReq();
            req.setConfigId(Long.parseLong(result.getConfigId()));
            req.setPhone(param[0].toString());
            req.setParams(JSONUtil.toJsonStr(param[1]));
            req.setStatus(result.isSuccess() ? SuccessFailureStatusEnum.SUCCESS : SuccessFailureStatusEnum.FAILURE);
            req.setResMsg(JSONUtil.toJsonStr(result.getData()));
            smsLogService.create(req);
        }
        return CoreMethodProcessor.super.postProcessor(result, param);
    }

    @Override
    public void sendMessagePreProcess(String phone, Object message) {
        // do nothing
    }

    @Override
    public void sendMessageByTemplatePreProcess(String phone,
                                                String templateId,
                                                LinkedHashMap<String, String> messages) {
        // do nothing
    }

    @Override
    public void massTextingPreProcess(List<String> phones, String message) {
        // do nothing
    }

    @Override
    public void massTextingByTemplatePreProcess(List<String> phones,
                                                String templateId,
                                                LinkedHashMap<String, String> messages) {
        // do nothing
    }
}