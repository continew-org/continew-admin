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

package top.continew.admin.common.config.jackson;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import top.continew.starter.data.mybatis.plus.base.IBaseEnum;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 通用枚举接口 IBaseEnum 反序列化器
 *
 * @author Charles7c
 * @see IBaseEnum
 * @since 2023/1/8 13:56
 */
@JacksonStdImpl
public class BaseEnumDeserializer extends JsonDeserializer<IBaseEnum> {

    /**
     * 静态实例
     */
    public static final BaseEnumDeserializer SERIALIZER_INSTANCE = new BaseEnumDeserializer();

    @Override
    public IBaseEnum deserialize(JsonParser jsonParser,
                                 DeserializationContext deserializationContext) throws IOException {
        Class<?> targetClass = jsonParser.getCurrentValue().getClass();
        String fieldName = jsonParser.getCurrentName();
        String value = jsonParser.getText();
        return this.getEnum(targetClass, value, fieldName);
    }

    /**
     * 通过某字段对应值获取枚举实例，获取不到时为 {@code null}
     *
     * @param targetClass 目标类型
     * @param value       字段值
     * @param fieldName   字段名
     * @return 对应枚举实例 ，获取不到时为 {@code null}
     */
    private IBaseEnum getEnum(Class<?> targetClass, String value, String fieldName) {
        Field field = ReflectUtil.getField(targetClass, fieldName);
        Class<?> fieldTypeClass = field.getType();
        Object[] enumConstants = fieldTypeClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            if (ClassUtil.isAssignable(IBaseEnum.class, fieldTypeClass)) {
                IBaseEnum baseEnum = (IBaseEnum)enumConstant;
                if (baseEnum.getValue().equals(Integer.valueOf(value))) {
                    return baseEnum;
                }
            }
        }
        return null;
    }
}
