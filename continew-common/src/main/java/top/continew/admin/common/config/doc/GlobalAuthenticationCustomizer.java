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

package top.continew.admin.common.config.doc;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.map.MapUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import top.continew.starter.apidoc.autoconfigure.SpringDocExtensionProperties;
import top.continew.starter.auth.satoken.autoconfigure.SaTokenExtensionProperties;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局鉴权参数定制器
 *
 * @author echo
 * @since 2024/12/31 13:36
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalAuthenticationCustomizer implements GlobalOpenApiCustomizer {

    private final SpringDocExtensionProperties properties;
    private final SaTokenExtensionProperties saTokenExtensionProperties;
    private final ApplicationContext context;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 定制 OpenAPI 文档
     *
     * @param openApi 当前 OpenAPI 对象
     */
    @Override
    public void customise(OpenAPI openApi) {
        if (MapUtil.isEmpty(openApi.getPaths())) {
            return;
        }

        // 收集需要排除的路径（包括 Sa-Token 配置中的排除路径和 @SaIgnore 注解路径）
        Set<String> excludedPaths = collectExcludedPaths();

        // 遍历所有路径，为需要鉴权的路径添加安全认证配置
        openApi.getPaths().forEach((path, pathItem) -> {
            if (isPathExcluded(path, excludedPaths)) {
                // 路径在排除列表中，跳过处理
                return;
            }
            // 为路径添加安全认证参数
            addAuthenticationParameters(pathItem);
        });
    }

    /**
     * 收集所有需要排除的路径
     *
     * @return 排除路径集合
     */
    private Set<String> collectExcludedPaths() {
        Set<String> excludedPaths = new HashSet<>();
        excludedPaths.addAll(Arrays.asList(saTokenExtensionProperties.getSecurity().getExcludes()));
        excludedPaths.addAll(resolveSaIgnorePaths());
        return excludedPaths;
    }

    /**
     * 为路径项添加认证参数
     *
     * @param pathItem 当前路径项
     */
    private void addAuthenticationParameters(PathItem pathItem) {
        Components components = properties.getComponents();
        if (components == null || MapUtil.isEmpty(components.getSecuritySchemes())) {
            return;
        }
        Map<String, SecurityScheme> securitySchemes = components.getSecuritySchemes();
        List<String> schemeNames = securitySchemes.values().stream().map(SecurityScheme::getName).toList();
        pathItem.readOperations().forEach(operation -> {
            SecurityRequirement securityRequirement = new SecurityRequirement();
            schemeNames.forEach(securityRequirement::addList);
            operation.addSecurityItem(securityRequirement);
        });
    }

    /**
     * 解析所有带有 @SaIgnore 注解的路径
     *
     * @return 被忽略的路径集合
     */
    private Set<String> resolveSaIgnorePaths() {
        // 获取所有标注 @RestController 的 Bean
        Map<String, Object> controllers = context.getBeansWithAnnotation(RestController.class);
        Set<String> ignoredPaths = new HashSet<>();

        // 遍历所有控制器，解析 @SaIgnore 注解路径
        controllers.values().forEach(controllerBean -> {
            Class<?> controllerClass = AopUtils.getTargetClass(controllerBean);
            List<String> classPaths = getClassPaths(controllerClass);

            // 类级别的 @SaIgnore 注解
            if (controllerClass.isAnnotationPresent(SaIgnore.class)) {
                classPaths.forEach(classPath -> ignoredPaths.add(classPath + "/**"));
            }

            // 方法级别的 @SaIgnore 注解
            Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(SaIgnore.class))
                .forEach(method -> ignoredPaths.addAll(combinePaths(classPaths, getMethodPaths(method))));
        });

        return ignoredPaths;
    }

    /**
     * 获取类上的所有路径
     *
     * @param controller 控制器类
     * @return 类路径列表
     */
    private List<String> getClassPaths(Class<?> controller) {
        List<String> classPaths = new ArrayList<>();
        // 处理 @RequestMapping 注解
        if (controller.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
            classPaths.addAll(Arrays.asList(mapping.value()));
        }
        // 处理 @CrudRequestMapping 注解
        if (controller.isAnnotationPresent(CrudRequestMapping.class)) {
            CrudRequestMapping mapping = controller.getAnnotation(CrudRequestMapping.class);
            if (!mapping.value().isEmpty()) {
                classPaths.add(mapping.value());
            }
        }
        return classPaths;
    }

    /**
     * 获取方法上的所有路径
     *
     * @param method 控制器方法
     * @return 方法路径列表
     */
    private List<String> getMethodPaths(Method method) {
        List<String> methodPaths = new ArrayList<>();

        // 检查方法上的各种映射注解
        if (method.isAnnotationPresent(GetMapping.class)) {
            methodPaths.addAll(Arrays.asList(method.getAnnotation(GetMapping.class).value()));
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            methodPaths.addAll(Arrays.asList(method.getAnnotation(PostMapping.class).value()));
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            methodPaths.addAll(Arrays.asList(method.getAnnotation(PutMapping.class).value()));
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            methodPaths.addAll(Arrays.asList(method.getAnnotation(DeleteMapping.class).value()));
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            methodPaths.addAll(Arrays.asList(method.getAnnotation(RequestMapping.class).value()));
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            methodPaths.addAll(Arrays.asList(method.getAnnotation(PatchMapping.class).value()));
        }

        return methodPaths;
    }

    /**
     * 组合类路径和方法路径
     *
     * @param classPaths  类路径列表
     * @param methodPaths 方法路径列表
     * @return 完整路径集合
     */
    private Set<String> combinePaths(List<String> classPaths, List<String> methodPaths) {
        return classPaths.stream()
            .flatMap(classPath -> methodPaths.stream().map(methodPath -> classPath + methodPath))
            .collect(Collectors.toSet());
    }

    /**
     * 检查路径是否在排除列表中
     *
     * @param path          当前路径
     * @param excludedPaths 排除路径集合，支持通配符
     * @return 是否匹配排除规则
     */
    private boolean isPathExcluded(String path, Set<String> excludedPaths) {
        return excludedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
