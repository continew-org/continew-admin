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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;

/**
 * CRUD API 权限前缀缓存测试。
 *
 * <p>覆盖缓存写入与命中、未命中时回退解析 {@link CrudRequestMapping} 注解、
 * 无注解返回 null，以及路径解析规则（多段路径驼峰化）与非法路径报错。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class CrudApiPermissionPrefixCacheTest {

    @AfterEach
    void tearDown() {
        CrudApiPermissionPrefixCache.clear();
    }

    @Test
    void shouldParsePrefixFromPath() {
        CrudApiPermissionPrefixCache.put(DemoController.class, "/system/user");

        assertEquals("system:user", CrudApiPermissionPrefixCache.get(DemoController.class));
    }

    @Test
    void shouldCamelCaseMultiSegmentPath() {
        CrudApiPermissionPrefixCache.put(DemoController.class, "/system/dict/item");

        assertEquals("system:dictItem", CrudApiPermissionPrefixCache.get(DemoController.class));
    }

    @Test
    void shouldThrowWhenPathTooShort() {
        assertThrows(IllegalArgumentException.class,
            () -> CrudApiPermissionPrefixCache.put(DemoController.class, "/user"));
    }

    @Test
    void shouldFallbackToAnnotationWhenCacheMissed() {
        assertEquals("demo:annotated", CrudApiPermissionPrefixCache.get(AnnotatedController.class));
    }

    @Test
    void shouldReturnNullWhenNoAnnotationAndNoCache() {
        assertNull(CrudApiPermissionPrefixCache.get(PlainController.class));
    }

    @Test
    void shouldCacheAnnotationResolution() {
        assertEquals("demo:annotated", CrudApiPermissionPrefixCache.get(AnnotatedController.class));
        assertEquals("demo:annotated", CrudApiPermissionPrefixCache.get(AnnotatedController.class));

        assertEquals(1, CrudApiPermissionPrefixCache.getAll().size());
    }

    /**
     * 无注解的演示控制器
     */
    static class DemoController {
    }

    /**
     * 带 CRUD 路径注解的演示控制器
     */
    @CrudRequestMapping("/demo/annotated")
    static class AnnotatedController {
    }

    /**
     * 无注解且无缓存的演示控制器
     */
    static class PlainController {
    }
}
