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

package top.continew.admin.config.satoken;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import top.continew.admin.system.controller.UserProfileController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 密码过期放行名单与被放行接口的一致性测试。
 *
 * <p>回归：放行名单曾写成 /system/user/password（该接口并不存在），导致密码过期用户连自助改密都被
 * 拦截、只能等管理员重置。这里用反射从控制器拼出真实路径后与配置比对，接口改路径或配置写错都会失败。</p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
class PasswordExcludePathTest {

    @Test
    void excludesShouldContainRealChangePasswordEndpoint() throws Exception {
        List<String> excludes = this.loadConfiguredExcludes();
        String changePasswordPath = this.resolveChangePasswordPath();
        assertTrue(excludes.contains(changePasswordPath),
            "密码过期放行名单缺少自助改密接口：" + changePasswordPath + "，当前配置：" + excludes);
    }

    /**
     * 按运行时的方式绑定 application.yml 中的放行名单
     *
     * @return 放行路径列表
     * @throws Exception 读取失败
     */
    private List<String> loadConfiguredExcludes() throws Exception {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
            .load("application", new ClassPathResource("config/application.yml"));
        Binder binder = new Binder(ConfigurationPropertySources.from(sources));
        LoginPasswordProperties properties = binder
            .bind("auth.password", LoginPasswordProperties.class)
            .orElseThrow(() -> new IllegalStateException("application.yml 中缺少 auth.password 配置"));
        return Arrays.asList(properties.getExcludes());
    }

    /**
     * 反射拼出修改密码接口的真实路径
     *
     * @return 形如 /user/profile/password 的路径
     */
    private String resolveChangePasswordPath() {
        RequestMapping classMapping = UserProfileController.class
            .getAnnotation(RequestMapping.class);
        Method method = Arrays.stream(UserProfileController.class.getDeclaredMethods())
            .filter(item -> "updatePassword".equals(item.getName()))
            .findFirst()
            .orElseThrow(
                () -> new IllegalStateException("UserProfileController 缺少 updatePassword 方法"));
        PatchMapping methodMapping = method.getAnnotation(PatchMapping.class);
        return classMapping.value()[0] + methodMapping.value()[0];
    }
}
