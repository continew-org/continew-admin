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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import org.junit.jupiter.api.Test;
import top.continew.starter.extension.crud.annotation.CrudApi;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;
import top.nextdoc4j.security.core.model.NextDoc4jSecurityMetadata;

/**
 * NextDoc4j CRUD 权限展示解析单元测试。
 *
 * <p>覆盖 supports 判定、权限码生成（列表/删除别名）、SaIgnore 与显式鉴权注解跳过、
 * 字典类 API 跳过与无前缀跳过。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class NextDoc4jCustomPermissionDisplayTest {

    private final NextDoc4jCustomPermissionDisplay resolver =
        new NextDoc4jCustomPermissionDisplay();

    @Test
    void shouldGeneratePermissionForListApi() throws Exception {
        NextDoc4jSecurityMetadata metadata = new NextDoc4jSecurityMetadata();
        java.lang.reflect.Method method = DemoController.class.getMethod("page");

        assertTrue(resolver.supports(DemoController.class, method));
        resolver.resolve(DemoController.class, method, metadata);

        assertEquals("CustomPermissionDisplay", resolver.getName());
        assertEquals(200, resolver.getOrder());
        assertEquals("system:demo:list", firstPermission(metadata));
    }

    @Test
    void shouldGenerateDeletePermissionAlias() throws Exception {
        NextDoc4jSecurityMetadata metadata = new NextDoc4jSecurityMetadata();
        java.lang.reflect.Method method = DemoController.class.getMethod("delete");

        resolver.resolve(DemoController.class, method, metadata);

        assertEquals("system:demo:delete", firstPermission(metadata));
    }

    @Test
    void shouldSkipWhenMethodIgnored() throws Exception {
        NextDoc4jSecurityMetadata metadata = new NextDoc4jSecurityMetadata();
        java.lang.reflect.Method method = IgnoredController.class.getMethod("page");

        resolver.resolve(IgnoredController.class, method, metadata);

        assertTrue(metadataIsEmpty(metadata));
    }

    @Test
    void shouldSkipWhenMethodHasExplicitPermission() throws Exception {
        NextDoc4jSecurityMetadata metadata = new NextDoc4jSecurityMetadata();
        java.lang.reflect.Method method = ExplicitPermissionController.class.getMethod("page");

        resolver.resolve(ExplicitPermissionController.class, method, metadata);

        assertTrue(metadataIsEmpty(metadata));
    }

    @Test
    void shouldSkipDictApi() throws Exception {
        NextDoc4jSecurityMetadata metadata = new NextDoc4jSecurityMetadata();
        java.lang.reflect.Method method = DictController.class.getMethod("page");

        resolver.resolve(DictController.class, method, metadata);

        assertTrue(metadataIsEmpty(metadata));
    }

    @Test
    void shouldNotSupportPlainController() throws Exception {
        java.lang.reflect.Method method = PlainController.class.getDeclaredMethod("plain");

        assertFalse(resolver.supports(PlainController.class, method));
    }

    private String firstPermission(NextDoc4jSecurityMetadata metadata) {
        Object permissions = metadataValue(metadata, "permissions");
        Object first = ((java.util.List<?>) permissions).get(0);
        return ((top.nextdoc4j.security.core.model.NextDoc4jSecurityMetadata.AuthInfo) first)
            .getValues()[0];
    }

    private boolean metadataIsEmpty(NextDoc4jSecurityMetadata metadata) {
        Object permissions = metadataValue(metadata, "permissions");
        return permissions == null || ((java.util.List<?>) permissions).isEmpty();
    }

    private Object metadataValue(NextDoc4jSecurityMetadata metadata, String field) {
        try {
            java.lang.reflect.Field f = metadata.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(metadata);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 带 CRUD 路径注解的演示控制器（权限前缀 /demo）
     */
    @CrudRequestMapping("/system/demo")
    static class DemoController {

        @CrudApi(Api.PAGE)
        public void page() {
            // 夹具方法，仅供注解解析
        }

        @CrudApi(Api.DELETE)
        public void delete() {
            // 夹具方法，仅供注解解析
        }
    }

    /**
     * 类级 SaIgnore 的演示控制器
     */
    @SaIgnore
    @CrudRequestMapping("/demo/ignored")
    static class IgnoredController {

        @CrudApi(Api.PAGE)
        public void page() {
            // 夹具方法，仅供注解解析
        }
    }

    /**
     * 方法级显式鉴权注解的演示控制器
     */
    @CrudRequestMapping("/demo/explicit")
    static class ExplicitPermissionController {

        @CrudApi(Api.PAGE)
        @SaCheckPermission("demo:custom")
        public void page() {
            // 夹具方法，仅供注解解析
        }
    }

    /**
     * 字典类 API 的演示控制器
     */
    @CrudRequestMapping(value = "/demo/dict", api = {Api.DICT, Api.PAGE})
    static class DictController {

        @CrudApi(Api.DICT)
        public void page() {
            // 夹具方法，仅供注解解析
        }
    }

    /**
     * 无注解的普通控制器
     */
    static class PlainController {

        void plain() {
        }
    }
}
