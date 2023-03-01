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

package top.charles7c.cnadmin.monitor.annotation;

import java.lang.annotation.*;

/**
 * 系统日志注解（用于接口方法或类上，辅助 Spring Doc OpenAPI3 使用效果最佳）
 *
 * @author Charles7c
 * @since 2022/12/23 20:00
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 日志描述（仅用于接口方法上）
     * <p>
     * 读取顺序：（越靠后优先级越高）<br>
     * 1、读取对应接口方法上的 @Operation(summary="描述") 内容<br>
     * 2、读取对应接口方法上的 @Log("描述") 内容<br>
     * </p>
     */
    String value() default "";

    /**
     * 所属模块（用于接口方法或类上）
     * <p>
     * 读取顺序：（越靠后优先级越高）<br>
     * 1、读取对应接口类上的 @Tag(name = "模块") 内容<br>
     * 2、读取对应接口类上的 @Log(module = "模块") 内容<br>
     * 3、读取对应接口方法上的 @Log(module = "模块") 内容
     * </p>
     */
    String module() default "";

    /**
     * 是否忽略日志记录（用于接口方法或类上）
     */
    boolean ignore() default false;
}
