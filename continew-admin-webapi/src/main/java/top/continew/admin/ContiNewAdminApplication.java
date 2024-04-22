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

package top.continew.admin;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.starter.core.autoconfigure.project.ProjectProperties;
import top.continew.starter.core.constant.PropertiesConstants;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.annotation.EnableCrudRestController;
import top.continew.starter.web.annotation.EnableGlobalExceptionHandler;

import java.net.InetAddress;

/**
 * 启动程序
 *
 * @author Charles7c
 * @since 2022/12/8 23:15
 */
@Slf4j
@RestController
@EnableFileStorage
@SpringBootApplication
@RequiredArgsConstructor
@EnableCrudRestController
@EnableGlobalExceptionHandler
@EnableMethodCache(basePackages = "top.continew.admin")
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
        return "%s service started successfully.".formatted(projectProperties.getName());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        Integer port = serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        String baseUrl = URLUtil.normalize("%s:%s%s".formatted(hostAddress, port, contextPath));
        log.info("----------------------------------------------");
        log.info("{} service started successfully.", projectProperties.getName());
        log.info("API 地址：{}", baseUrl);
        String docEnabledProperty = PropertiesConstants.SPRINGDOC_SWAGGER_UI + StringConstants.DOT + PropertiesConstants.ENABLED;
        if (Boolean.TRUE.equals(SpringUtil.getProperty(docEnabledProperty, boolean.class, false))) {
            log.info("API 文档：{}/doc.html", baseUrl);
        }
        log.info("----------------------------------------------");
    }
}
