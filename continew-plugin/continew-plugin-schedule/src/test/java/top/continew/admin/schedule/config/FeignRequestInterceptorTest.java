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

package top.continew.admin.schedule.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.schedule.api.JobClient;
import top.continew.admin.schedule.constant.JobConstants;

/**
 * Feign 请求拦截器测试。
 *
 * <p>验证 apply 后请求头携带命名空间与认证令牌两个头部（键与值均断言），
 * 令牌取自调度客户端的实时获取。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class FeignRequestInterceptorTest {

    private static final String NAMESPACE = "test-namespace";
    private static final String TOKEN = "unit-test-token";

    private JobClient jobClient;
    private FeignRequestInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jobClient = mock(JobClient.class);
        interceptor = new FeignRequestInterceptor(jobClient);
        ReflectionTestUtils.setField(interceptor, "namespace", NAMESPACE);
    }

    @Test
    void shouldApplyNamespaceHeader() {
        when(jobClient.getToken()).thenReturn(TOKEN);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals(NAMESPACE, template.headers().get(JobConstants.NAMESPACE_ID_HEADER).iterator()
            .next());
        verify(jobClient).getToken();
    }

    @Test
    void shouldApplyAuthTokenHeaderFromJobClient() {
        when(jobClient.getToken()).thenReturn(TOKEN);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals(TOKEN,
            template.headers().get(JobConstants.AUTH_TOKEN_HEADER).iterator().next());
        verify(jobClient).getToken();
    }
}
