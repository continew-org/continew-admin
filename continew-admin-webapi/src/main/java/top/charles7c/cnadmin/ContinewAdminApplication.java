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

package top.charles7c.cnadmin;

import java.net.InetAddress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaIgnore;

import top.charles7c.cnadmin.common.config.properties.ContinewAdminProperties;

/**
 * 启动程序
 *
 * @author Charles7c
 * @since 2022/12/8 23:15
 */
@Slf4j
@RestController
@SpringBootApplication
@RequiredArgsConstructor
@Import(cn.hutool.extra.spring.SpringUtil.class)
@ComponentScan(basePackages = {"top.charles7c.cnadmin", "cn.hutool.extra.spring"})
public class ContinewAdminApplication implements ApplicationRunner {

    private final ContinewAdminProperties properties;
    private final ServerProperties serverProperties;

    public static void main(String[] args) {
        SpringApplication.run(ContinewAdminApplication.class, args);
    }

    /**
     * 访问首页提示
     *
     * @return /
     */
    @Hidden
    @SaIgnore
    @GetMapping("/")
    public String index() {
        return String.format("%s backend service started successfully.", properties.getName());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        log.info("------------------------------------------------------");
        log.info("{} backend service started successfully.", properties.getName());
        log.info("后端 API 地址：http://{}:{}", hostAddress, serverProperties.getPort());
        log.info("后端 API 文档：http://{}:{}/doc.html", hostAddress, serverProperties.getPort());
        log.info("------------------------------------------------------");
    }
}
