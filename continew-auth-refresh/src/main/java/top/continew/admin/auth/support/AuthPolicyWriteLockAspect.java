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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.continew.admin.auth.api.AuthPolicyLockTarget;
import top.continew.admin.auth.api.AuthPolicyLockTargetResolver;
import top.continew.admin.auth.api.AuthPolicyWriteLocked;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 认证策略写锁协调器。
 *
 * <p>切面只理解通用策略锁目标。用户、客户端、部门、租户和套餐到目标的映射由各
 * 业务模块的 {@link AuthPolicyLockTargetResolver} 提供，因此认证会话模块不会反向
 * 依赖业务 Mapper。锁在事务代理外层获得，顺序固定为“策略锁 → 数据库事务 → 会话
 * 失效”。</p>
 *
 * @author luoqiz
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class AuthPolicyWriteLockAspect {

    private final ApplicationContext applicationContext;
    private final RefreshSessionStore sessionStore;

    /**
     * 在事务开始前获取策略写锁，并在事务完成后释放。
     *
     * @param joinPoint 当前业务方法
     * @return 业务方法返回值
     * @throws Throwable 业务方法异常
     */
    @Around("@annotation(top.continew.admin.auth.api.AuthPolicyWriteLocked)")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("认证策略变更必须在数据库事务外发起");
        }
        AuthPolicyWriteLocked locked = this.getPolicyWriteLocked(joinPoint);
        AuthPolicyLockTargetResolver resolver = applicationContext.getBean(locked.value());
        List<AuthPolicyLockTarget> targets = resolver.resolve(joinPoint.getArgs()).stream()
            .filter(Objects::nonNull)
            .filter(target -> target.key() != null && !target.key().isBlank())
            .distinct()
            .sorted(Comparator.comparing(AuthPolicyLockTarget::type)
                .thenComparing(AuthPolicyLockTarget::key))
            .toList();
        List<AuthPolicyLock> locks = new ArrayList<>(targets.size());
        try {
            for (AuthPolicyLockTarget target : targets) {
                locks.add(this.lock(target));
            }
            return joinPoint.proceed();
        } finally {
            for (int i = locks.size() - 1; i >= 0; i--) {
                try {
                    locks.get(i).close();
                } catch (RuntimeException e) {
                    log.warn("释放认证策略写锁失败", e);
                }
            }
        }
    }

    private AuthPolicyLock lock(AuthPolicyLockTarget target) {
        return switch (target.type()) {
            case USER -> sessionStore.lockUserPolicy(Long.parseLong(target.key()));
            case TENANT -> sessionStore.lockTenantPolicyWrite(Long.parseLong(target.key()));
            case CLIENT -> sessionStore.lockClientPolicyWrite(target.key());
        };
    }

    private AuthPolicyWriteLocked getPolicyWriteLocked(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method targetMethod =
            AopUtils.getMostSpecificMethod(method, joinPoint.getTarget().getClass());
        AuthPolicyWriteLocked locked = AnnotationUtils.findAnnotation(targetMethod,
            AuthPolicyWriteLocked.class);
        if (locked == null) {
            throw new IllegalStateException("认证策略写锁声明缺失");
        }
        return locked;
    }
}
