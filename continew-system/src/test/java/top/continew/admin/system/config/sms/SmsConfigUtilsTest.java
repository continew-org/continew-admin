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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.provider.factory.BaseProviderFactory;
import org.dromara.sms4j.provider.factory.ProviderFactoryHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.system.model.resp.SmsConfigResp;

/**
 * 短信配置转换工具单元测试。
 *
 * <p>覆盖空配置与未知供应商的空返回、公共字段映射、可选字段的缺省省略
 * 与供应商自定义配置的合并。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class SmsConfigUtilsTest {

    private MockedStatic<ProviderFactoryHolder> holderMock;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        holderMock = mockStatic(ProviderFactoryHolder.class);
        // JsonUtils 的静态块经 SpringUtil 取 ObjectMapper，先桩 SpringUtil 并供给真实 Mapper
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(tools.jackson.databind.ObjectMapper.class))
            .thenReturn(new tools.jackson.databind.ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
        holderMock.close();
    }

    @Test
    void shouldReturnNullWhenConfigMissing() {
        assertNull(SmsConfigUtils.from(null));
    }

    @Test
    void shouldReturnNullWhenSupplierUnknown() {
        SmsConfigResp config = config("unknown");
        holderMock.when(() -> ProviderFactoryHolder.requireForSupplier("unknown")).thenReturn(null);

        assertNull(SmsConfigUtils.from(config));
    }

    @Test
    void shouldMapCommonFieldsAndMergeSupplierConfig() {
        SmsConfigResp config = config("aliyun");
        config.setId(9L);
        config.setAccessKey("ak");
        config.setSecretKey("sk");
        config.setSignature("签名");
        config.setTemplateId("tpl");
        config.setWeight(3);
        config.setRetryInterval(10);
        config.setMaxRetries(2);
        config.setSupplierConfig("{\"maximum\":5}");

        @SuppressWarnings({"unchecked", "rawtypes"})
        BaseProviderFactory factory = mock(BaseProviderFactory.class);
        when(factory.getConfigClass()).thenReturn((Class) TestConfig.class);
        holderMock.when(() -> ProviderFactoryHolder.requireForSupplier("aliyun"))
            .thenReturn(factory);

        BaseConfig result = SmsConfigUtils.from(config);

        assertEquals("9", result.getConfigId());
        assertEquals("ak", result.getAccessKeyId());
        assertEquals("sk", result.getAccessKeySecret());
        assertEquals("签名", result.getSignature());
        assertEquals("tpl", result.getTemplateId());
        assertEquals(3, result.getWeight());
        assertEquals(10, result.getRetryInterval());
        assertEquals(2, result.getMaxRetries());
        // 供应商自定义配置合并进公共配置
        assertEquals(5, result.getMaximum());
    }

    /**
     * 可实例化的测试配置（BaseConfig 为抽象类，供应商工厂返回的是具体子类）
     */
    public static class TestConfig extends BaseConfig {

        @Override
        public String getSupplier() {
            return "aliyun";
        }
    }

    @Test
    void shouldOmitOptionalFieldsWhenAbsent() {
        SmsConfigResp config = config("aliyun");
        config.setId(1L);
        config.setSupplierConfig("");

        @SuppressWarnings({"unchecked", "rawtypes"})
        BaseProviderFactory factory = mock(BaseProviderFactory.class);
        when(factory.getConfigClass()).thenReturn((Class) TestConfig.class);
        holderMock.when(() -> ProviderFactoryHolder.requireForSupplier("aliyun"))
            .thenReturn(factory);

        BaseConfig result = SmsConfigUtils.from(config);

        assertEquals("1", result.getConfigId());
        // 可选字段未提供时保持 BaseConfig 自身默认值（weight=1、retryInterval=5、maxRetries=0）
        assertEquals(1, result.getWeight());
        assertEquals(5, result.getRetryInterval());
        assertEquals(0, result.getMaxRetries());
    }

    private SmsConfigResp config(String supplier) {
        SmsConfigResp config = new SmsConfigResp();
        config.setSupplier(supplier);
        return config;
    }
}
