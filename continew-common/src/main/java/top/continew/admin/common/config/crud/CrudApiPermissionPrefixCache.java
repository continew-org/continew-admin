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

package top.continew.admin.common.config.crud;

import cn.hutool.core.util.StrUtil;
import org.springframework.core.annotation.AnnotationUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CRUD API 权限前缀缓存
 *
 * <p>
 * 缓存由 {@code SaTokenConfiguration} 在 {@code ApplicationReadyEvent} 时批量写入，而此时 Web 容器
 * 已开始接收请求，因此读取方（{@code BaseController#preHandle}）可能落在写入完成之前。为此这里做两件事：
 * 一是使用 {@link ConcurrentHashMap} 保证并发读写的可见性与安全性，二是在未命中时依据控制器自身的
 * {@link CrudRequestMapping} 注解懒解析，避免读到 {@code null} 导致权限码退化为 {@code null:xxx} 而误判 403。
 * </p>
 *
 * @author Charles7c
 * @since 2025/7/24 22:14
 */
public class CrudApiPermissionPrefixCache {

    private static final Map<Class<?>, String> PERMISSION_PREFIX_CACHE = new ConcurrentHashMap<>();

    /**
     * 存储CRUD API权限前缀
     *
     * @param controllerClazz 控制器类
     * @param path            路径
     */
    public static void put(Class<?> controllerClazz, String path) {
        String permissionPrefix = parsePermissionPrefix(path);
        PERMISSION_PREFIX_CACHE.put(controllerClazz, permissionPrefix);
    }

    /**
     * 获取CRUD API权限前缀
     *
     * <p>
     * 缓存未命中时（如启动期间到达的请求）回退到解析控制器类上的 {@link CrudRequestMapping} 注解。
     * </p>
     *
     * @param controllerClazz 控制器类
     * @return 权限前缀
     */
    public static String get(Class<?> controllerClazz) {
        return PERMISSION_PREFIX_CACHE.computeIfAbsent(controllerClazz,
            CrudApiPermissionPrefixCache::resolvePermissionPrefix);
    }

    /**
     * 清空缓存
     */
    public static void clear() {
        PERMISSION_PREFIX_CACHE.clear();
    }

    /**
     * 获取所有缓存
     *
     * @return 所有缓存（只读视图）
     */
    public static Map<Class<?>, String> getAll() {
        return Collections.unmodifiableMap(PERMISSION_PREFIX_CACHE);
    }

    /**
     * 解析控制器类上的 {@link CrudRequestMapping} 注解获取权限前缀
     *
     * @param controllerClazz 控制器类
     * @return 权限前缀，无注解时返回 {@code null}
     */
    private static String resolvePermissionPrefix(Class<?> controllerClazz) {
        CrudRequestMapping crudRequestMapping =
            AnnotationUtils.findAnnotation(controllerClazz, CrudRequestMapping.class);
        return crudRequestMapping == null ? null
            : parsePermissionPrefix(crudRequestMapping.value());
    }

    /**
     * 解析权限前缀（解析路径获取模块名和资源名）
     *
     * <p>
     * 例如：/system/user => system:user <br>
     * /system/dict/item => system:dictItem
     * </p>
     *
     * @param path 路径
     * @return 权限前缀
     */
    private static String parsePermissionPrefix(String path) {
        List<String> pathSegmentList = StrUtil.splitTrim(path, StringConstants.SLASH);
        if (pathSegmentList.size() < 2) {
            throw new IllegalArgumentException("无效的 @CrudRequestMapping 路径配置：" + path);
        }
        String moduleName = pathSegmentList.get(0);
        String resourceName =
            StrUtil.toCamelCase(String.join(StringConstants.UNDERLINE, pathSegmentList
                .subList(1, pathSegmentList.size())));
        return "%s:%s".formatted(moduleName, resourceName);
    }

    private CrudApiPermissionPrefixCache() {
    }
}
