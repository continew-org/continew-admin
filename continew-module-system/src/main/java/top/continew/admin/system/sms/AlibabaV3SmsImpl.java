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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.api.utils.SmsRespUtils;
import org.dromara.sms4j.comm.delayedTime.DelayedTime;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.dromara.sms4j.comm.utils.SmsUtils;
import org.dromara.sms4j.provider.service.AbstractSmsBlend;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlibabaV3SmsImpl extends AbstractSmsBlend<AlibabaV3Config> {

    private int retry = 0;

    /**
     * AlibabaSmsImpl
     * <p>构造器，用于构造短信实现模块
     *
     * @author :Wind
     */
    public AlibabaV3SmsImpl(AlibabaV3Config config, Executor pool, DelayedTime delayedTime) {
        super(config, pool, delayedTime);
    }

    /**
     * AlibabaSmsImpl
     * <p>构造器，用于构造短信实现模块
     */
    public AlibabaV3SmsImpl(AlibabaV3Config config) {
        super(config);
    }

    @Override
    public String getSupplier() {
        return AlibabaV3Config.SUPPLIER;
    }

    @Override
    public SmsResponse sendMessage(String phone, String message) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put(getConfig().getTemplateName(), message);
        return sendMessage(phone, getConfig().getTemplateId(), map);
    }

    @Override
    public SmsResponse sendMessage(String phone, LinkedHashMap<String, String> messages) {
        if (Objects.isNull(messages)) {
            messages = new LinkedHashMap<>();
        }
        return sendMessage(phone, getConfig().getTemplateId(), messages);
    }

    @Override
    public SmsResponse sendMessage(String phone, String templateId, LinkedHashMap<String, String> messages) {
        if (Objects.isNull(messages)) {
            messages = new LinkedHashMap<>();
        }
        String messageStr = JSONUtil.toJsonStr(messages);
        return getSmsResponse(phone, messageStr, templateId);
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String message) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(getConfig().getTemplateName(), message);
        return massTexting(phones, getConfig().getTemplateId(), map);
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String templateId, LinkedHashMap<String, String> messages) {
        if (Objects.isNull(messages)) {
            messages = new LinkedHashMap<>();
        }
        String messageStr = JSONUtil.toJsonStr(messages);
        return getSmsResponse(SmsUtils.addCodePrefixIfNot(phones), messageStr, templateId);
    }

    private SmsResponse getSmsResponse(String phone, String message, String templateId) {
        String queryString;
        String paramStr;
        try {
            queryString = AliyunV3Utils.generateQueryString(getConfig(), message, phone, templateId);
            paramStr = AliyunV3Utils.generateParamBody(getConfig(), null);
        } catch (Exception e) {
            log.error("aliyun send message error", e);
            throw new SmsBlendException(e.getMessage());
        }
        log.debug("requestUrl {}", queryString);

        Map<String, String> headers;
        try {
            headers = AliyunV3Utils.generateHeaders(getConfig(), paramStr, queryString);
        } catch (Exception e) {
            log.error("aliyun send message error", e);
            throw new SmsBlendException(e.getMessage());
        }
        log.debug("headers {}", headers);

        SmsResponse smsResponse;
        try {
            String requestUrl = AliyunV3Utils.generateSendSmsRequestUrl(getConfig(), queryString);

            smsResponse = getResponse(http.postJson(requestUrl, headers, paramStr));
        } catch (SmsBlendException e) {
            smsResponse = errorResp(e.message);
        }
        if (smsResponse.isSuccess() || retry >= getConfig().getMaxRetries()) {
            retry = 0;
            return smsResponse;
        }
        return requestRetry(phone, message, templateId);
    }

    private SmsResponse requestRetry(String phone, String message, String templateId) {
        http.safeSleep(getConfig().getRetryInterval());
        retry++;
        log.warn("短信第 {} 次重新发送", retry);
        return getSmsResponse(phone, message, templateId);
    }

    private SmsResponse getResponse(JSONObject resJson) {
        return SmsRespUtils.resp(resJson, "OK".equals(resJson.getStr("Code")), getConfigId());
    }

}
