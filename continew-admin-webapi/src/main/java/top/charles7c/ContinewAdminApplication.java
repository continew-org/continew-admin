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

package top.charles7c;

import java.net.InetAddress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 启动程序
 *
 * @author Charles7c
 * @since 2022/12/8 23:15
 */
@Slf4j
@RestController
@SpringBootApplication
public class ContinewAdminApplication {

    private static Environment env;

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ContinewAdminApplication.class);
        ConfigurableApplicationContext context = application.run(args);

        env = context.getEnvironment();
        log.info("------------------------------------------------------");
        log.info("{} backend service started successfully.", env.getProperty("continew-admin.name"));
        log.info("后端 API 地址：http://{}:{}", InetAddress.getLocalHost().getHostAddress(), env.getProperty("server.port"));
        log.info("------------------------------------------------------");
    }

    /**
     * 访问首页提示
     *
     * @return /
     */
    @GetMapping("/")
    public String index() {
        return String.format("%s backend service started successfully.", env.getProperty("continew-admin.name"));
    }
}
