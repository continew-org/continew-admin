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

package top.continew.admin.extension.scheduling;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.aizuda.snailjob.common.core.util.SnailJobVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;

/**
 * 任务调度服务端启动程序
 *
 * @author KAI
 * @since 2024/6/25 22:24
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ScheduleServerApplication extends com.aizuda.snailjob.server.SnailJobServerApplication
    implements ApplicationRunner {

    private final ServerProperties serverProperties;

    public static void main(String[] args) {
        SpringApplication.run(ScheduleServerApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        String hostAddress = NetUtil.getLocalhostStr();
        Integer port = serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        String baseUrl = URLUtil.normalize("%s:%s%s".formatted(hostAddress, port, contextPath));
        log.info("------------------------------------------------------");
        log.info("{} started successfully.", SpringUtil.getApplicationName());
        log.info("Snail Job: v{} (Spring Boot: v{})", SnailJobVersion.getVersion(),
            SpringBootVersion.getVersion());
        log.info("服务地址: {}", baseUrl);
        log.info("服务文档: https://snailjob.opensnail.com");
        log.info("注意: 此服务为 Snail Job 服务端 (为方便本地开发使用而特意提供)");
        log.info("注意: 实际生产环境，部署 Snail Job 服务端可根据官方文档进行操作");
        log.info("注意: 此服务非 ContiNew Admin 服务端!!!");
        log.info("------------------------------------------------------");
    }
}
