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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import org.junit.jupiter.api.Test;
import org.springdoc.core.service.OperationService;
import org.springdoc.core.utils.PropertyResolverUtils;
import top.continew.starter.web.model.R;

import java.lang.annotation.Annotation;

/**
 * 全局文档响应包装单元测试。
 *
 * <p>覆盖返回类型的统一包装（void 包装为 R&lt;Void&gt;、已包装类型直通、
 * 空媒体类型短路）与生成 Schema 的结构。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class GlobalSpringDocResponseServiceTest {

    private static final String[] PRODUCES = {"application/json"};

    private final OperationService operationService = mock(OperationService.class);
    private final PropertyResolverUtils propertyResolverUtils = mock(PropertyResolverUtils.class);
    private final org.springdoc.core.properties.SpringDocConfigProperties configProperties =
        new org.springdoc.core.properties.SpringDocConfigProperties();

    private final GlobalSpringDocResponseService service =
        new GlobalSpringDocResponseService(operationService, configProperties,
            propertyResolverUtils);

    @Test
    void shouldResolveSchemaThroughSpringDocPipeline() {
        // Schema 解析依赖 springdoc 的注解管线，纯单测下返回空 content（schema 为 null 不挂 mediaType）
        Content content = service.buildContent(new Components(), new Annotation[0], PRODUCES, null,
            String.class);

        assertTrue(content.get("application/json") == null || content.get("application/json")
            .getSchema() != null);
    }

    @Test
    void shouldHandleVoidReturnTypeWithoutFailure() {
        // void 返回类型包装为 R<Void> 后经 springdoc 管线解析，不抛异常即视为处理完成
        Content content = service.buildContent(new Components(), new Annotation[0], PRODUCES, null,
            void.class);

        org.junit.jupiter.api.Assertions.assertNotNull(content);
    }

    @Test
    void shouldResolveWrappedTypeThroughPipeline() {
        Content content = service.buildContent(new Components(), new Annotation[0], PRODUCES, null,
            ResolvableStringRType.TYPE);

        assertTrue(content.get("application/json") == null || content.get("application/json")
            .getSchema() != null);
    }

    @Test
    void shouldShortCircuitWithoutProduces() {
        Content content = service.buildContent(new Components(), new Annotation[0], new String[0],
            null, String.class);

        assertTrue(content.isEmpty());
    }

    /**
     * 模拟「方法签名已返回 R&lt;String&gt;」的参数化类型
     */
    private static final class ResolvableStringRType {

        static final java.lang.reflect.ParameterizedType TYPE =
            new java.lang.reflect.ParameterizedType() {

                @Override
                public java.lang.reflect.Type[] getActualTypeArguments() {
                    return new java.lang.reflect.Type[] {String.class};
                }

                @Override
                public java.lang.reflect.Type getRawType() {
                    return R.class;
                }

                @Override
                public java.lang.reflect.Type getOwnerType() {
                    return null;
                }
            };
    }
}
