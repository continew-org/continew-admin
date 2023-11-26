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

import lombok.RequiredArgsConstructor;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.util.StrUtil;

import top.charles7c.continew.starter.extension.crud.annotation.NoResponseAdvice;
import top.charles7c.continew.starter.extension.crud.model.resp.R;

/**
 * 全局响应结果处理器
 *
 * @author BULL_BCLS
 * @since 2023/10/8 20:19
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseBodyAdviceHandler implements ResponseBodyAdvice<Object> {

    private static final String[] EXCLUDE = {"MultipleOpenApiWebMvcResource", "SwaggerConfigResource",};
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        return !methodParameter.getParameterType().isAssignableFrom(R.class)
            && !methodParameter.hasMethodAnnotation(NoResponseAdvice.class)
            && !StrUtil.equalsAny(methodParameter.getDeclaringClass().getSimpleName(), EXCLUDE);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
        ServerHttpResponse response) {
        if (String.class.equals(returnType.getGenericParameterType())) {
            try {
                return objectMapper.writeValueAsString(R.ok(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return R.ok(body);
    }
}
