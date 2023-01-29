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

import static top.charles7c.cnadmin.common.annotation.CrudRequestMapping.Api;

import java.lang.reflect.Method;

import lombok.NoArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.annotation.CrudRequestMapping;
import top.charles7c.cnadmin.common.util.ExceptionUtils;

/**
 * CRUD 请求映射器处理器映射器
 *
 * @author Charles7c
 * @since 2023/1/27 10:30
 */
@NoArgsConstructor
public class CrudRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(@NonNull Method method, @NonNull Class<?> handlerType) {
        RequestMappingInfo requestMappingInfo = super.getMappingForMethod(method, handlerType);
        if (requestMappingInfo == null) {
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
            requestMappingInfo = RequestMappingInfo.paths(pathPrefix).build().combine(requestMappingInfo);
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
