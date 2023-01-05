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

package top.charles7c.cnadmin.common.util.validate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author Charles7c
 * @since 2023/1/2 22:12
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Validator {

    /**
     * 如果为空，抛出异常
     *
     * @param obj
     *            被检测的对象
     * @param message
     *            错误信息
     * @param exceptionType
     *            异常类型
     */
    protected static void exIfNull(Object obj, String message, Class<? extends RuntimeException> exceptionType) {
        if (obj == null) {
            log.error(message);
            throw ReflectUtil.newInstance(exceptionType, message);
        }
    }

    /**
     * 如果为空，抛出异常
     *
     * @param str
     *            被检测的字符串
     * @param message
     *            错误信息
     * @param exceptionType
     *            异常类型
     */
    public static void exIfBlank(CharSequence str, String message, Class<? extends RuntimeException> exceptionType) {
        if (StrUtil.isBlank(str)) {
            log.error(message);
            throw ReflectUtil.newInstance(exceptionType, message);
        }
    }

    /**
     * 如果相同，抛出异常
     *
     * @param obj1
     *            要比较的对象1
     * @param obj2
     *            要比较的对象2
     * @param message
     *            错误信息
     * @param exceptionType
     *            异常类型
     */
    public static void exIfEqual(Object obj1, Object obj2, String message,
        Class<? extends RuntimeException> exceptionType) {
        if (ObjectUtil.equals(obj1, obj2)) {
            log.error(message);
            throw ReflectUtil.newInstance(exceptionType, message);
        }
    }

    /**
     * 如果不相同，抛出异常
     *
     * @param obj1
     *            要比较的对象1
     * @param obj2
     *            要比较的对象2
     * @param message
     *            错误信息
     * @param exceptionType
     *            异常类型
     */
    public static void exIfNotEqual(Object obj1, Object obj2, String message,
        Class<? extends RuntimeException> exceptionType) {
        if (ObjectUtil.notEqual(obj1, obj2)) {
            log.error(message);
            throw ReflectUtil.newInstance(exceptionType, message);
        }
    }

    /**
     * 如果条件成立，抛出异常
     *
     * @param conditionSupplier
     *            条件
     * @param message
     *            错误信息
     * @param exceptionType
     *            异常类型
     */
    public static void exIfCondition(java.util.function.BooleanSupplier conditionSupplier, String message,
        Class<? extends RuntimeException> exceptionType) {
        if (conditionSupplier != null && conditionSupplier.getAsBoolean()) {
            log.error(message);
            throw ReflectUtil.newInstance(exceptionType, message);
        }
    }
}
