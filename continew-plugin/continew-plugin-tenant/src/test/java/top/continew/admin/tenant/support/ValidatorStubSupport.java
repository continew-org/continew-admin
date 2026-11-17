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

package top.continew.admin.tenant.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import org.mockito.MockedStatic;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

/**
 * 测试辅助：为 CheckUtils/ValidationUtils 的静态校验器提供恒通过的桩。
 *
 * <p>starter Validator 的实例在类首次加载时固化到静态字段，仅桩 SpringUtil 会受
 * 其他测试类的加载时序影响，需同时覆写静态实例。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
public final class ValidatorStubSupport {

    private ValidatorStubSupport() {
    }

    /**
     * 桩 SpringUtil 的 Jakarta 校验器并覆写 starter Validator 的静态实例
     *
     * @param springUtil SpringUtil 静态模拟对象
     */
    public static void stubJakartaValidator(MockedStatic<SpringUtil> springUtil) {
        Validator jakartaValidator = mock(Validator.class);
        when(jakartaValidator.validate(any(Object.class), any(Class.class)))
            .thenReturn(Set.<ConstraintViolation<Object>>of());
        springUtil.when(() -> SpringUtil.getBean(Validator.class)).thenReturn(jakartaValidator);
        TenantValidatorOverwrite.overwriteJakartaInstance(jakartaValidator);
    }
}
