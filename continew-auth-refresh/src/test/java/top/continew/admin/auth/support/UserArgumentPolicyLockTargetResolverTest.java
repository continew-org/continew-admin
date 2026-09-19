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

package top.continew.admin.auth.support;

import org.junit.jupiter.api.Test;
import top.continew.admin.auth.api.AuthPolicyLockTarget;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 用户策略锁参数解析测试。 */
class UserArgumentPolicyLockTargetResolverTest {

    private final UserArgumentPolicyLockTargetResolver resolver =
        new UserArgumentPolicyLockTargetResolver();

    @Test
    void shouldResolveUserIdFromUpdatePasswordSignature() {
        List<AuthPolicyLockTarget> targets = List.copyOf(resolver.resolve(
            new Object[] {"old-password", "new-password", 1L}));

        assertEquals(List.of(AuthPolicyLockTarget.user(1L)), targets);
    }

    @Test
    void shouldResolveUserIdsFromCollectionArgument() {
        List<AuthPolicyLockTarget> targets = List.copyOf(resolver.resolve(
            new Object[] {"ignored", List.of(1L, 2L)}));

        assertEquals(List.of(AuthPolicyLockTarget.user(1L), AuthPolicyLockTarget.user(2L)),
            targets);
    }

    @Test
    void shouldRejectArgumentsWithoutUserTarget() {
        assertThrows(IllegalArgumentException.class,
            () -> resolver.resolve(new Object[] {"old-password", "new-password"}));
    }
}
