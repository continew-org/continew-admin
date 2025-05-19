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

package top.continew.admin.schedule.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import top.continew.starter.core.constant.PropertiesConstants;

import java.lang.annotation.*;

/**
 * 是否启用 Snail Job 注解
 *
 * @author Charles7c
 * @since 2025/5/18 12:03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnProperty(prefix = "snail-job", name = PropertiesConstants.ENABLED, havingValue = "true", matchIfMissing = true)
public @interface ConditionalOnEnabledScheduleJob {
}