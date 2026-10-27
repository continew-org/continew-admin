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

package top.continew.admin.common.config.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.web.annotation.EnableGlobalResponse;
import top.continew.starter.web.model.R;

import java.util.Map;

/**
 * 全局异常处理器回归测试。
 *
 * <p>
 * 回归：Starter 3.0 的 {@code globalExceptionHandler} 与 Admin 同名处理器在 bean 定义层面冲突，
 * Starter 的兜底处理器覆盖了 Admin 的处理器，导致所有业务异常都被改写成「服务器异常，请联系管理员」。
 * 这里用真实组件扫描 + Starter 自动配置复现同名冲突，断言业务异常仍返回自身文案。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class GlobalExceptionHandlerTest {

    private AnnotationConfigWebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 与启动类一致：application.yml 开启了 bean 定义覆盖，冲突时后注册的 Starter 处理器会静默胜出
        context = new AnnotationConfigWebApplicationContext() {

            @Override
            protected DefaultListableBeanFactory createBeanFactory() {
                DefaultListableBeanFactory beanFactory = super.createBeanFactory();
                beanFactory.setAllowBeanDefinitionOverriding(true);
                return beanFactory;
            }
        };
        // 与 application.yml 中的失败提示保持一致，用于验证兜底处理器仍在生效
        context.getEnvironment()
            .getPropertySources()
            .addFirst(new MapPropertySource("test-response", Map
                .of("continew-starter.web.response.default-error-msg", "服务器异常，请联系管理员")));
        context.setServletContext(new MockServletContext());
        context.register(TestConfig.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("业务异常返回自身文案")
    void shouldKeepBadRequestMessage() throws Exception {
        mockMvc
            .perform(get("/test/exception/bad-request"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.msg").value("客户端不存在"));
    }

    @Test
    @DisplayName("未登录异常返回 401 与登录提示")
    void shouldHandleNotLoginException() throws Exception {
        mockMvc
            .perform(get("/test/exception/not-login"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401"))
            .andExpect(jsonPath("$.msg").value("您的登录状态已过期，请重新登录"));
    }

    @Test
    @DisplayName("无权限异常返回 403 与授权提示")
    void shouldHandleNotPermissionException() throws Exception {
        mockMvc
            .perform(get("/test/exception/not-permission"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("403"))
            .andExpect(jsonPath("$.msg").value("没有访问权限，请联系管理员授权"));
    }

    @Test
    @DisplayName("未分类异常仍由 Starter 兜底处理器接管")
    void shouldFallbackToStarterHandler() throws Exception {
        mockMvc
            .perform(get("/test/exception/unknown"))
            .andExpect(jsonPath("$.msg").value("服务器异常，请联系管理员"));
    }

    /**
     * 模拟启动类的组件扫描范围与 Starter 全局响应配置
     */
    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableGlobalResponse
    @ComponentScan(basePackageClasses = AdminGlobalExceptionHandler.class)
    static class TestConfig {

        /**
         * 抛出各类异常的测试接口
         */
        @RestController
        static class FailingController {

            @GetMapping("/test/exception/bad-request")
            R badRequest() {
                throw new BadRequestException("客户端不存在");
            }

            @GetMapping("/test/exception/not-login")
            R notLogin() {
                throw new NotLoginException("未提供 token", "login", NotLoginException.NOT_TOKEN);
            }

            @GetMapping("/test/exception/not-permission")
            R notPermission() {
                throw new NotPermissionException("system:user:list", "login");
            }

            @GetMapping("/test/exception/unknown")
            R unknown() {
                throw new IllegalStateException("内部细节不应对用户可见");
            }
        }
    }
}
