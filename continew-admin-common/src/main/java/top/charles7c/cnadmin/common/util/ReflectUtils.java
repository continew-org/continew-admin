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

package top.charles7c.cnadmin.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import cn.hutool.core.util.ReflectUtil;

/**
 * 反射工具类
 *
 * @author Charles7c
 * @since 2023/1/15 22:05
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtils {

    /**
     * 获得一个类中所有非静态字段名列表，包括其父类中的字段<br>
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass
     *            类
     * @return 非静态字段名列表
     * @throws SecurityException
     *             安全检查异常
     */
    public static List<String> getNonStaticFieldsName(Class<?> beanClass) throws SecurityException {
        List<Field> nonStaticFields = getNonStaticFields(beanClass);
        return nonStaticFields.stream().map(Field::getName).collect(Collectors.toList());
    }

    /**
     * 获得一个类中所有非静态字段列表，包括其父类中的字段<br>
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass
     *            类
     * @return 非静态字段列表
     * @throws SecurityException
     *             安全检查异常
     */
    public static List<Field> getNonStaticFields(Class<?> beanClass) throws SecurityException {
        Field[] fields = ReflectUtil.getFields(beanClass);
        return Arrays.stream(fields).filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList());
    }
}
