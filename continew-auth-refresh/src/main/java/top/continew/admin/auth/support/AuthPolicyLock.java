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

import org.redisson.api.RLock;
import top.continew.admin.auth.exception.RefreshTokenException;

import java.util.concurrent.TimeUnit;

/**
 * 项目内认证策略锁。
 *
 * <p>支持 Redisson 普通锁和读写锁，避免为认证并发策略修改 ContiNew Starter。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
public final class AuthPolicyLock implements AutoCloseable {

    private final RLock lock;
    private final boolean acquired;

    private AuthPolicyLock(RLock lock, boolean acquired) {
        this.lock = lock;
        this.acquired = acquired;
    }

    /** 在指定时间内获取锁，并使用 Redisson watchdog 自动续期。 */
    public static AuthPolicyLock acquire(RLock lock, long waitMillis) {
        boolean acquired;
        try {
            acquired = lock.tryLock(waitMillis, -1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw RefreshTokenException.internalServerError("认证请求已中断");
        }
        if (!acquired) {
            throw RefreshTokenException.tooManyRequests("认证请求正在处理中，请稍后重试");
        }
        return new AuthPolicyLock(lock, true);
    }

    @Override
    public void close() {
        if (acquired && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
