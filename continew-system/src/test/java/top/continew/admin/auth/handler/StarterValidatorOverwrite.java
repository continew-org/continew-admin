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

package top.continew.admin.auth.handler;

import sun.misc.Unsafe;
import top.continew.starter.core.util.validation.Validator;

import java.lang.reflect.Field;

/**
 * 测试辅助：覆写 starter Validator 的静态实例，消除类加载时序对桩定的影响。
 *
 * @author Charles7c
 * @since 5.0.0
 */
final class StarterValidatorOverwrite {

    private StarterValidatorOverwrite() {
    }

    /**
     * 将 starter Validator 的静态实例覆写为指定校验器
     *
     * @param jakartaValidator 目标校验器
     */
    static void overwriteInstance(jakarta.validation.Validator jakartaValidator) {
        try {
            Field field = Validator.class.getDeclaredField("VALIDATOR_INSTANCE");
            Unsafe unsafe = unsafeInstance();
            Object base = unsafe.staticFieldBase(field); // NOSONAR: 覆写静态实例的唯一途径，JDK 25 仍支持
            long offset = unsafe.staticFieldOffset(field); // NOSONAR: 同上
            unsafe.putObject(base, offset, jakartaValidator); // NOSONAR: 同上
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("覆写 Validator 静态实例失败", e);
        }
    }

    private static Unsafe unsafeInstance() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("获取 Unsafe 实例失败", e);
        }
    }
}
