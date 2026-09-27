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

package top.continew.admin.auth.adapter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.api.AuthPolicyLockTarget;
import top.continew.admin.auth.api.AuthPolicyLockTargetResolver;
import top.continew.admin.system.mapper.ClientMapper;
import top.continew.admin.system.model.entity.ClientDO;

import java.util.Collection;
import java.util.List;

/** 将客户端数据库主键解析为认证会话使用的客户端标识。 */
@Component
@RequiredArgsConstructor
public class ClientPolicyLockTargetResolver implements AuthPolicyLockTargetResolver {

    private final ClientMapper clientMapper;

    @Override
    public Collection<AuthPolicyLockTarget> resolve(Object[] args) {
        for (Object value : args) {
            if (value instanceof Long id) {
                return resolveClientIds(List.of(id));
            }
            if (value instanceof Collection<?> values) {
                List<Long> ids = values.stream().filter(Long.class::isInstance)
                    .map(Long.class::cast).toList();
                if (!ids.isEmpty()) {
                    return resolveClientIds(ids);
                }
            }
        }
        throw new IllegalArgumentException("认证客户端策略锁参数无效");
    }

    private Collection<AuthPolicyLockTarget> resolveClientIds(List<Long> ids) {
        return clientMapper
            .selectList(Wrappers.<ClientDO>lambdaQuery().select(ClientDO::getClientId)
                .in(ClientDO::getId, ids))
            .stream().map(ClientDO::getClientId)
            .filter(clientId -> clientId != null && !clientId.isBlank())
            .map(AuthPolicyLockTarget::client).toList();
    }
}
