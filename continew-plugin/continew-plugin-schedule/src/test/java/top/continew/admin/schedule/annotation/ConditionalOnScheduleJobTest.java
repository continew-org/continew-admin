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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Snail Job 条件装配注解元数据测试。
 *
 * <p>以反射断言启用/禁用两个条件注解绑定的 @ConditionalOnProperty 元数据，
 * 锁定 snail-job.enabled 属性的条件装配契约（启用注解缺省即装配，禁用注解
 * 仅在显式配置 false 时装配）。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class ConditionalOnScheduleJobTest {

    @Test
    void shouldBindEnabledPropertyOnEnabledAnnotation() {
        ConditionalOnProperty conditional = ConditionalOnEnabledScheduleJob.class
            .getAnnotation(ConditionalOnProperty.class);

        assertEquals("snail-job", conditional.prefix());
        assertEquals("enabled", conditional.name()[0]);
        assertEquals("true", conditional.havingValue());
        assertTrue(conditional.matchIfMissing());
    }

    @Test
    void shouldBindDisabledPropertyOnDisabledAnnotation() {
        ConditionalOnProperty conditional = ConditionalOnDisabledScheduleJob.class
            .getAnnotation(ConditionalOnProperty.class);

        assertEquals("snail-job", conditional.prefix());
        assertEquals("enabled", conditional.name()[0]);
        assertEquals("false", conditional.havingValue());
        assertEquals(false, conditional.matchIfMissing());
    }
}
