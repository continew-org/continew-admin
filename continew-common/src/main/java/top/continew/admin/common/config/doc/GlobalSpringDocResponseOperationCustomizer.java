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

import cn.hutool.core.util.ClassUtil;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OperationService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springdoc.core.utils.SpringDocAnnotationsUtils;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import top.continew.starter.web.autoconfigure.response.GlobalResponseProperties;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import static org.springdoc.core.converters.ConverterUtils.isResponseTypeWrapper;
import static org.springdoc.core.utils.SpringDocAnnotationsUtils.extractSchema;

/**
 * 全局响应操作自定义器
 * <p>
 * 自定义 OpenAPI 文档中的响应结构，将原始返回类型包装为统一的响应格式
 * </p>
 *
 * @author echo
 * @since 2025/07/08 09:34
 */
@Component
public class GlobalSpringDocResponseOperationCustomizer extends GenericResponseService {

    private final GlobalResponseProperties globalResponseProperties;
    private final Class<Object> responseClass;
    private final PropertyResolverUtils propertyResolverUtils;

    public GlobalSpringDocResponseOperationCustomizer(OperationService operationService,
        SpringDocConfigProperties springDocConfigProperties,
        PropertyResolverUtils propertyResolverUtils,
        GlobalResponseProperties globalResponseProperties) {
        super(operationService, springDocConfigProperties, propertyResolverUtils);
        this.globalResponseProperties = globalResponseProperties;
        this.responseClass =
            ClassUtil.loadClass(globalResponseProperties.getResponseClassFullName());
        this.propertyResolverUtils = propertyResolverUtils;
    }

    /**
     * 构建响应内容
     *
     * @param components     组件信息
     * @param annotations    方法注解
     * @param methodProduces 方法支持的媒体类型
     * @param jsonView       JSON 视图
     * @param returnType     返回类型
     * @return 响应内容
     */
    @Override
    public Content buildContent(Components components,
        Annotation[] annotations,
        String[] methodProduces,
        JsonView jsonView,
        Type returnType) {
        if (ArrayUtils.isEmpty(methodProduces)) {
            return new Content();
        }

        // 如果返回类型已经包含全局响应包装类，直接处理
        if (isAlreadyWrapped(returnType)) {
            return buildContentForWrappedType(components, annotations, methodProduces, jsonView,
                returnType);
        }

        // 包装返回类型为全局响应格式
        Type wrappedType = wrapReturnType(returnType);

        if (isVoid(wrappedType)) {
            return null;
        }

        return buildContentForWrappedType(components, annotations, methodProduces, jsonView,
            wrappedType);
    }

    /**
     * 检查返回类型是否已被全局响应包装
     *
     * @param returnType 返回类型
     * @return 是否已被包装
     */
    private boolean isAlreadyWrapped(Type returnType) {
        return returnType.getTypeName()
            .contains(globalResponseProperties.getResponseClassFullName());
    }

    /**
     * 包装返回类型为全局响应格式
     *
     * @param returnType 原始返回类型
     * @return 包装后的类型
     */
    private Type wrapReturnType(Type returnType) {
        if (returnType == void.class || returnType == Void.class) {
            return TypeUtils.parameterize(responseClass, Void.class);
        }
        return TypeUtils.parameterize(responseClass, returnType);
    }

    /**
     * 为包装后的类型构建内容
     *
     * @param components     组件信息
     * @param annotations    方法注解
     * @param methodProduces 方法支持的媒体类型
     * @param jsonView       JSON 视图
     * @param returnType     返回类型
     * @return 响应内容
     */
    private Content buildContentForWrappedType(Components components,
        Annotation[] annotations,
        String[] methodProduces,
        JsonView jsonView,
        Type returnType) {
        Content content = new Content();
        Schema<?> schema = calculateSchema(components, returnType, jsonView, annotations);

        if (schema != null) {
            io.swagger.v3.oas.models.media.MediaType mediaType =
                new io.swagger.v3.oas.models.media.MediaType();
            mediaType.setSchema(schema);
            setContent(methodProduces, content, mediaType);
        }

        return content;
    }

    /**
     * 检查类型是否为 void 类型
     *
     * @param returnType 返回类型
     * @return 是否为 void 类型
     */
    private boolean isVoid(Type returnType) {
        if (Void.TYPE.equals(returnType) || Void.class.equals(returnType)) {
            return true;
        }

        if (returnType instanceof ParameterizedType parameterizedType) {
            Type[] types = parameterizedType.getActualTypeArguments();
            if (isResponseTypeWrapper(ResolvableType.forType(returnType).getRawClass())) {
                return isVoid(types[0]);
            }
        }

        return false;
    }

    /**
     * 计算响应 Schema
     *
     * @param components  组件信息
     * @param returnType  返回类型
     * @param jsonView    JSON 视图
     * @param annotations 方法注解
     * @return Schema 对象
     */
    private Schema<?> calculateSchema(Components components,
        Type returnType,
        JsonView jsonView,
        Annotation[] annotations) {
        if (isVoid(returnType) || SpringDocAnnotationsUtils.isAnnotationToIgnore(returnType)) {
            return null;
        }
        return extractSchema(components, returnType, jsonView, annotations,
            propertyResolverUtils.getSpecVersion());
    }

    /**
     * 设置响应内容的媒体类型
     *
     * @param methodProduces 方法支持的媒体类型数组
     * @param content        响应内容
     * @param mediaType      媒体类型对象
     */
    private void setContent(String[] methodProduces,
        Content content,
        io.swagger.v3.oas.models.media.MediaType mediaType) {
        Arrays.stream(methodProduces)
            .forEach(mediaTypeStr -> content.addMediaType(mediaTypeStr, mediaType));
    }
}
