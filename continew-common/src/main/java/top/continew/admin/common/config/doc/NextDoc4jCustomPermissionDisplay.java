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

import lombok.RequiredArgsConstructor;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.admin.common.config.crud.CrudApiPermissionPrefixCache;
import top.continew.starter.extension.crud.annotation.CrudApi;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;
import top.nextdoc4j.security.core.enhancer.NextDoc4jSecurityMetadataResolver;
import top.nextdoc4j.security.core.model.NextDoc4jSecurityMetadata;
import top.nextdoc4j.security.satoken.constant.NextDoc4jSaTokenConstant;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * NextDoc4j 自定义权限码展示
 *
 * @author echo
 * @since 2025/12/18
 */
@Component
@RequiredArgsConstructor
public class NextDoc4jCustomPermissionDisplay implements NextDoc4jSecurityMetadataResolver {

    @Override
    public void resolve(Class<?> beanType, Method method, NextDoc4jSecurityMetadata metadata) {
        // 处理 CrudRequestMapping 和 CrudApi 注解生成的权限信息
        resolveCrudPermission(beanType, method, metadata);
    }

    @Override
    public boolean supports(Class<?> beanType, Method method) {
        // 检查类上是否有 CrudRequestMapping 注解且方法上有 CrudApi 注解
        CrudRequestMapping crudRequestMapping = AnnotatedElementUtils
            .findMergedAnnotation(beanType, CrudRequestMapping.class);
        CrudApi crudApi = getCrudApi(beanType, method);
        return crudRequestMapping != null && crudApi != null;
    }

    @Override
    public String getName() {
        return "CustomPermissionDisplay";
    }

    /**
     * 解析 CRUD 权限信息
     */
    private void resolveCrudPermission(Class<?> beanType, Method method, NextDoc4jSecurityMetadata metadata) {
        CrudRequestMapping crudRequestMapping = AnnotatedElementUtils
            .findMergedAnnotation(beanType, CrudRequestMapping.class);
        CrudApi crudApi = getCrudApi(beanType, method);

        if (crudRequestMapping == null || crudApi == null) {
            return;
        }

        // 检查方法上是否有 @SaIgnore 注解,如果有则跳过
        if (hasSaIgnore(beanType, method)) {
            return;
        }

        // 检查方法上是否已经有 @SaCheckRole 或 @SaCheckPermission 注解
        // 如果有,重写了方法,跳过 CRUD 自动生成,让插件处理
        if (hasSaTokenAnnotation(beanType, method)) {
            return;
        }

        // 跳过字典类型的 API
        if (Api.DICT.equals(crudApi.value()) || Api.TREE_DICT.equals(crudApi.value())) {
            return;
        }

        // 获取权限前缀
        String permissionPrefix = CrudApiPermissionPrefixCache.get(beanType);
        if (permissionPrefix == null || permissionPrefix.isEmpty()) {
            return;
        }

        // 获取 API 名称并生成权限字符串
        String apiName = BaseController.getApiName(crudApi.value());
        String permission = "%s:%s".formatted(permissionPrefix, apiName.toLowerCase());

        // 添加到权限列表中
        metadata.addPermission(new String[] {permission}, "AND", "crud", new String[0]);
    }

    /**
     * 解析 CRUD API 注解（优先重写方法，其次父类方法兜底）
     */
    private CrudApi getCrudApi(Class<?> beanType, Method method) {
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, beanType);
        Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        // 优先当前方法上的注解（如果重写方法自己标了 @CrudApi，就用它）
        CrudApi crudApi = AnnotatedElementUtils.findMergedAnnotation(bridgedMethod, CrudApi.class);
        if (crudApi != null) {
            return crudApi;
        }

        // 兜底：回退到原始方法（父类/接口定义）
        if (!bridgedMethod.equals(method)) {
            return AnnotatedElementUtils.findMergedAnnotation(method, CrudApi.class);
        }
        return null;
    }

    /**
     * 检查方法或类上是否有 @SaIgnore 注解
     */
    private boolean hasSaIgnore(Class<?> beanType, Method method) {
        // 检查方法上的 @SaIgnore 注解
        Annotation methodAnnotation = method.getAnnotation(NextDoc4jSaTokenConstant.SA_IGNORE_CLASS);
        if (methodAnnotation != null) {
            return true;
        }

        // 检查类上的 @SaIgnore 注解
        Annotation classAnnotation = beanType.getAnnotation(NextDoc4jSaTokenConstant.SA_IGNORE_CLASS);
        return classAnnotation != null;
    }

    /**
     * 检查方法或类上是否有 @SaCheckRole 或 @SaCheckPermission 注解
     * 如果有这些注解,说明开发者手动配置了权限,应该跳过 CRUD 自动生成
     */
    private boolean hasSaTokenAnnotation(Class<?> beanType, Method method) {
        // 检查方法上的注解
        Annotation methodPermission = method.getAnnotation(NextDoc4jSaTokenConstant.SA_CHECK_PERMISSION_CLASS);
        Annotation methodRole = method.getAnnotation(NextDoc4jSaTokenConstant.SA_CHECK_ROLE_CLASS);

        if (methodPermission != null || methodRole != null) {
            return true;
        }

        // 检查类上的注解
        Annotation classPermission = beanType.getAnnotation(NextDoc4jSaTokenConstant.SA_CHECK_PERMISSION_CLASS);
        Annotation classRole = beanType.getAnnotation(NextDoc4jSaTokenConstant.SA_CHECK_ROLE_CLASS);

        return classPermission != null || classRole != null;
    }

    @Override
    public int getOrder() {
        return 200;
    }
}
