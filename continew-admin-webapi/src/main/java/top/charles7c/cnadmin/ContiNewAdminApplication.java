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
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;

import top.charles7c.continew.starter.core.autoconfigure.project.ProjectProperties;

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
public class ContiNewAdminApplication implements ApplicationRunner {

    private final ProjectProperties projectProperties;
    private final ServerProperties serverProperties;

    public static void main(String[] args) {
        SpringApplication.run(ContiNewAdminApplication.class, args);
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
        return String.format("%s service started successfully.", projectProperties.getName());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        Integer port = serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        String baseUrl = URLUtil.normalize(String.format("%s:%s%s", hostAddress, port, contextPath));
        log.info("----------------------------------------------");
        log.info("{} service started successfully.", projectProperties.getName());
        log.info("API 地址：{}", baseUrl);
        Boolean docEnabled = Convert.toBool(SpringUtil.getProperty("springdoc.swagger-ui.enabled"));
        if (Boolean.TRUE.equals(docEnabled)) {
            log.info("API 文档：{}/doc.html", baseUrl);
        }
        log.info("----------------------------------------------");
    }
}
