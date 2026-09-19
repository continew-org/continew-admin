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

package top.continew.admin.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.continew.admin.auth.service.RefreshTokenService;
import top.continew.admin.auth.service.SessionInvalidationService;

/** 认证会话失效服务实现。 */
@Service
@RequiredArgsConstructor
public class SessionInvalidationServiceImpl implements SessionInvalidationService {

    private final RefreshTokenService refreshTokenService;

    @Override
    public void invalidateClient(String clientId) {
        this.afterCommit(() -> refreshTokenService.revokeByClient(clientId));
    }

    @Override
    public void invalidateTenant(Long tenantId) {
        this.afterCommit(() -> refreshTokenService.revokeByTenant(tenantId));
    }

    @Override
    public void invalidateUser(Long userId) {
        this.afterCommit(() -> refreshTokenService.revokeByUser(userId));
    }

    @Override
    public void revokeSession(String sessionId) {
        this.afterCommit(() -> refreshTokenService.revokeBySessionId(sessionId));
    }

    /**
     * 业务数据提交后才撤销登录态。认证策略写锁由外层切面持有到事务 afterCompletion
     * 之后才释放；这里的 afterCommit（先于 afterCompletion 执行）会重新获取同一个
     * lockTenantPolicyWrite / lockUserPolicy / lockClientPolicyWrite，依赖 Redisson
     * 同线程写锁可重入。该不变量保证“数据库已提交而新登录会话穿过旧策略”的窗口不会
     * 出现；改动本方法时不得把撤销时机移出写锁持有区间。
     */
    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
