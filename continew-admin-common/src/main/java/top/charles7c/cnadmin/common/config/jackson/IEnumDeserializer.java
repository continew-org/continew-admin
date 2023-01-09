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

package top.charles7c.cnadmin.common.config.jackson;

import java.io.IOException;
import java.lang.reflect.Field;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;

/**
 * 通用枚举接口 IEnum 反序列化器
 *
 * @author Charles7c
 * @since 2023/1/8 13:56
 */
@JacksonStdImpl
public class IEnumDeserializer extends JsonDeserializer<IEnum> {

    /** 静态实例 */
    public static final IEnumDeserializer SERIALIZER_INSTANCE = new IEnumDeserializer();

    @Override
    public IEnum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Class<?> targetClass = jsonParser.getCurrentValue().getClass();
        String fieldName = jsonParser.getCurrentName();
        String value = jsonParser.getText();
        return this.getEnum(targetClass, value, fieldName);
    }

    /**
     * 通过某字段对应值获取枚举，获取不到时为 {@code null}
     *
     * @param targetClass
     *            目标类型
     * @param value
     *            字段值
     * @param fieldName
     *            字段名
     * @return 对应枚举 ，获取不到时为 {@code null}
     */
    public IEnum getEnum(Class<?> targetClass, String value, String fieldName) {
        Field field = ReflectUtil.getField(targetClass, fieldName);
        Class<?> fieldTypeClass = field.getType();
        Object[] enumConstants = fieldTypeClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            if (ClassUtil.isAssignable(IEnum.class, fieldTypeClass)) {
                IEnum iEnum = (IEnum)enumConstant;
                if (iEnum.getValue().equals(Integer.valueOf(value))) {
                    return iEnum;
                }
            }
        }
        return null;
    }
}
