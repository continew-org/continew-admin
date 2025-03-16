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

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.api.proxy.CoreMethodProcessor;
import org.springframework.stereotype.Component;
import top.continew.admin.system.model.req.SmsRecordReq;
import top.continew.admin.system.service.SmsRecordService;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Component
public class SmsRecordProcessor implements CoreMethodProcessor {

    @Resource
    private SmsRecordService smsRecordService;

    @Override
    public Object[] preProcessor(Method method, Object source, Object[] param) {
        return CoreMethodProcessor.super.preProcessor(method, source, param);
    }

    @Override
    public void sendMessagePreProcess(String phone, Object message) {
        System.out.println("发送短信前处理" + phone + message);
    }

    @Override
    public void sendMessageByTemplatePreProcess(String phone,
                                                String templateId,
                                                LinkedHashMap<String, String> messages) {
        log.debug("发送短信前处理 sendMessageByTemplatePreProcess " + phone + templateId + JSONUtil.toJsonPrettyStr(messages));
    }

    @Override
    public void massTextingPreProcess(List<String> phones, String message) {
        log.debug("发送短信前处理 massTextingPreProcess " + JSONUtil.toJsonPrettyStr(phones) + message);
    }

    @Override
    public void massTextingByTemplatePreProcess(List<String> phones,
                                                String templateId,
                                                LinkedHashMap<String, String> messages) {
        log.debug("发送短信前处理 massTextingByTemplatePreProcess " + JSONUtil.toJsonPrettyStr(phones) + JSONUtil
            .toJsonPrettyStr(messages));
    }

    @Override
    public Object postProcessor(SmsResponse result, Object[] param) {
        SmsRecordReq record = new SmsRecordReq();
        record.setConfigId(Long.parseLong(result.getConfigId()));
        record.setPhone(param[0].toString());
        record.setParams(JSONUtil.toJsonPrettyStr(param[1]));
        record.setStatus(result.isSuccess());
        record.setResMsg(JSONUtil.toJsonPrettyStr(result.getData()));
        smsRecordService.add(record);
        return CoreMethodProcessor.super.postProcessor(result, param);
    }

    @Override
    public void exceptionHandleProcessor(Method method,
                                         Object source,
                                         Object[] param,
                                         Exception exception) throws RuntimeException {
        CoreMethodProcessor.super.exceptionHandleProcessor(method, source, param, exception);
    }
}