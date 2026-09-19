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

import org.springframework.stereotype.Component;
import top.continew.admin.auth.api.AuthPolicyLockTarget;
import top.continew.admin.auth.api.AuthPolicyLockTargetResolver;

import java.util.Collection;
import java.util.List;

/** 根据约定的业务方法参数解析用户策略锁目标。 */
@Component
public class UserArgumentPolicyLockTargetResolver implements AuthPolicyLockTargetResolver {

    @Override
    public Collection<AuthPolicyLockTarget> resolve(Object[] args) {
        for (Object value : args) {
            if (value instanceof Long userId) {
                return List.of(AuthPolicyLockTarget.user(userId));
            }
            if (value instanceof Collection<?> values) {
                List<AuthPolicyLockTarget> targets = values.stream().filter(Long.class::isInstance)
                    .map(Long.class::cast).map(AuthPolicyLockTarget::user).toList();
                if (!targets.isEmpty()) {
                    return targets;
                }
            }
        }
        throw new IllegalArgumentException("认证用户策略锁参数无效");
    }
}
