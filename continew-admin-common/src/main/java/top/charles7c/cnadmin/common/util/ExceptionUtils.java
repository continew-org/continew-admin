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

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 异常工具类
 *
 * @author Charles7c
 * @since 2022/12/21 20:56
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtils {

    /**
     * 如果有异常，返回 null
     *
     * @param exSupplier
     *            可能会出现异常的方法执行
     * @param <T>
     *            /
     * @return /
     */
    public static <T> T exToNull(ExSupplier<T> exSupplier) {
        return exToDefault(exSupplier, null);
    }

    /**
     * 如果有异常，执行异常处理
     *
     * @param supplier
     *            可能会出现异常的方法执行
     * @param exConsumer
     *            异常处理
     * @param <T>
     *            /
     * @return /
     */
    public static <T> T exToNull(ExSupplier<T> supplier, Consumer<Exception> exConsumer) {
        return exToDefault(supplier, null, exConsumer);
    }

    /**
     * 如果有异常，返回默认值
     *
     * @param exSupplier
     *            可能会出现异常的方法执行
     * @param defaultValue
     *            默认值
     * @param <T>
     *            /
     * @return /
     */
    public static <T> T exToDefault(ExSupplier<T> exSupplier, T defaultValue) {
        return exToDefault(exSupplier, defaultValue, null);
    }

    /**
     * 如果有异常，执行异常处理，返回默认值
     *
     * @param exSupplier
     *            可能会出现异常的方法执行
     * @param defaultValue
     *            默认值
     * @param exConsumer
     *            异常处理
     * @param <T>
     *            /
     * @return /
     */
    public static <T> T exToDefault(ExSupplier<T> exSupplier, T defaultValue, Consumer<Exception> exConsumer) {
        try {
            return exSupplier.get();
        } catch (Exception ex) {
            if (exConsumer != null) {
                exConsumer.accept(ex);
            }
            return defaultValue;
        }
    }

    /**
     * 异常提供者
     *
     * @param <T>
     *            /
     */
    public interface ExSupplier<T> {
        /**
         * 获取返回值
         *
         * @return /
         * @throws Exception
         *             /
         */
        T get() throws Exception;
    }

}
