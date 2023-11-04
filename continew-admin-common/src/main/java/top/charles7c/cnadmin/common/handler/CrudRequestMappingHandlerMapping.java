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

package top.charles7c.cnadmin.common.handler;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;
import top.charles7c.cnadmin.common.annotation.CrudRequestMapping;
import top.charles7c.cnadmin.common.util.ExceptionUtils;

import java.lang.reflect.Method;

import static top.charles7c.cnadmin.common.annotation.CrudRequestMapping.Api;

/**
 * CRUD 请求映射器处理器映射器
 *
 * @author Charles7c
 * @since 2023/1/27 10:30
 */
public class CrudRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(@NonNull Method method, @NonNull Class<?> handlerType) {
        RequestMappingInfo requestMappingInfo = super.getMappingForMethod(method, handlerType);
        if (null == requestMappingInfo) {
            return null;
        }

        // 如果没有声明 @CrudRequestMapping 注解，直接返回
        if (!handlerType.isAnnotationPresent(CrudRequestMapping.class)) {
            return requestMappingInfo;
        }

        // 获取 @CrudRequestMapping 注解信息
        CrudRequestMapping crudRequestMapping = handlerType.getDeclaredAnnotation(CrudRequestMapping.class);
        // 拼接路径前缀（合并了 @RequestMapping 的部分能力）
        String pathPrefix = crudRequestMapping.value();
        if (StrUtil.isNotBlank(pathPrefix)) {
            /*
             * 问题：RequestMappingInfo.paths(pathPrefix) 返回的 RequestMappingInfo 对象里 pathPatternsCondition = null
             * 导致 combine() 方法抛出断言异常！ 修复：创建 options 对象，并设置 PatternParser
             */
            RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
            options.setPatternParser(PathPatternParser.defaultInstance);
            requestMappingInfo =
                RequestMappingInfo.paths(pathPrefix).options(options).build().combine(requestMappingInfo);
        }

        // 过滤 API
        Api[] apiArr = crudRequestMapping.api();
        // 如果非本类中定义，且 API 列表中不包含，则忽略
        Api api = ExceptionUtils.exToNull(() -> Api.valueOf(method.getName().toUpperCase()));
        if (method.getDeclaringClass() != handlerType && !ArrayUtil.containsAny(apiArr, Api.ALL, api)) {
            return null;
        }
        return requestMappingInfo;
    }
}
