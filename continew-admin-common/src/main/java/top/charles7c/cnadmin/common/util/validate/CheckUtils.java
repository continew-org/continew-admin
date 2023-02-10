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

import top.charles7c.cnadmin.common.exception.ServiceException;

/**
 * 业务参数校验工具类（抛出 500 ServiceException）
 *
 * @author Charles7c
 * @see ServiceException
 * @since 2023/1/2 22:12
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckUtils extends Validator {

    private static final Class<ServiceException> EXCEPTION_TYPE = ServiceException.class;

    /**
     * 如果为空，抛出异常
     *
     * @param obj
     *            被检测的对象
     * @param message
     *            错误信息
     */
    public static void throwIfNull(Object obj, String message) {
        throwIfNull(obj, message, EXCEPTION_TYPE);
    }

    /**
     * 如果不为空，抛出异常
     *
     * @param obj
     *            被检测的对象
     * @param message
     *            错误信息
     */
    public static void throwIfNotNull(Object obj, String message) {
        throwIfNotNull(obj, message, EXCEPTION_TYPE);
    }

    /**
     * 如果为空，抛出异常
     *
     * @param obj
     *            被检测的对象
     * @param message
     *            错误信息
     */
    public static void throwIfEmpty(Object obj, String message) {
        throwIfEmpty(obj, message, EXCEPTION_TYPE);
    }

    /**
     * 如果不为空，抛出异常
     *
     * @param obj
     *            被检测的对象
     * @param message
     *            错误信息
     */
    public static void throwIfNotEmpty(Object obj, String message) {
        throwIfNotEmpty(obj, message, EXCEPTION_TYPE);
    }

    /**
     * 如果为空，抛出异常
     *
     * @param str
     *            被检测的字符串
     * @param message
     *            错误信息
     */
    public static void throwIfBlank(CharSequence str, String message) {
        throwIfBlank(str, message, EXCEPTION_TYPE);
    }

    /**
     * 如果不为空，抛出异常
     *
     * @param str
     *            被检测的字符串
     * @param message
     *            错误信息
     */
    public static void throwIfNotBlank(CharSequence str, String message) {
        throwIfNotBlank(str, message, EXCEPTION_TYPE);
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
     */
    public static void throwIfEqual(Object obj1, Object obj2, String message) {
        throwIfEqual(obj1, obj2, message, EXCEPTION_TYPE);
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
     */
    public static void throwIfNotEqual(Object obj1, Object obj2, String message) {
        throwIfNotEqual(obj1, obj2, message, EXCEPTION_TYPE);
    }

    /**
     * 如果相同，抛出异常（不区分大小写）
     *
     * @param str1
     *            要比较的字符串1
     * @param str2
     *            要比较的字符串2
     * @param message
     *            错误信息
     */
    public static void throwIfEqualIgnoreCase(CharSequence str1, CharSequence str2, String message) {
        throwIfEqualIgnoreCase(str1, str2, message, EXCEPTION_TYPE);
    }

    /**
     * 如果不相同，抛出异常（不区分大小写）
     *
     * @param str1
     *            要比较的字符串1
     * @param str2
     *            要比较的字符串2
     * @param message
     *            错误信息
     */
    public static void throwIfNotEqualIgnoreCase(CharSequence str1, CharSequence str2, String message) {
        throwIfNotEqualIgnoreCase(str1, str2, message, EXCEPTION_TYPE);
    }

    /**
     * 如果条件成立，抛出异常
     *
     * @param conditionSupplier
     *            条件
     * @param message
     *            错误信息
     */
    public static void throwIf(java.util.function.BooleanSupplier conditionSupplier, String message) {
        throwIf(conditionSupplier, message, EXCEPTION_TYPE);
    }
}
