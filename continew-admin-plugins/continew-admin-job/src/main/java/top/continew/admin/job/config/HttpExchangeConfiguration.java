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

package top.continew.admin.job.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import top.continew.admin.job.api.JobApi;
import top.continew.admin.job.api.JobBatchApi;
import top.continew.admin.job.api.JobClient;
import top.continew.admin.job.constant.JobConstants;

/**
 * HTTP Exchange 配置
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/25 18:03
 */
@Configuration
@RequiredArgsConstructor
public class HttpExchangeConfiguration {

    private final ObjectMapper objectMapper;
    @Value("${snail-job.server.url}")
    private String baseUrl;

    @Value("${snail-job.namespace}")
    private String namespace;

    @Value("${snail-job.server.username}")
    private String username;

    @Value("${snail-job.server.password}")
    private String password;

    @Bean
    public JobApi jobApi() {
        return httpServiceProxyFactory().createClient(JobApi.class);
    }

    @Bean
    public JobBatchApi jobBatchApi() {
        return httpServiceProxyFactory().createClient(JobBatchApi.class);
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(10));
                conn.addHandlerLast(new WriteTimeoutHandler(10));
            });
        WebClient webClient = WebClient.builder()
            .codecs(config -> config.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper)))
            .codecs(config -> config.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper)))
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .filter((request, next) -> {
                // 设置请求头
                ClientRequest filtered = ClientRequest.from(request)
                    .header(JobConstants.NAMESPACE_ID_HEADER, namespace)
                    .header(JobConstants.AUTH_TOKEN_HEADER, jobClient().getToken())
                    .build();
                return next.exchange(filtered);
            })
            .baseUrl(baseUrl)
            .build();
        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
    }

    @Bean
    public JobClient jobClient() {
        return new JobClient(baseUrl, username, password);
    }
}
