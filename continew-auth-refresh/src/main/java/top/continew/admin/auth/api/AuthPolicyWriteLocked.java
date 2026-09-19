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

package top.continew.admin.auth.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在数据库事务开始前获取认证策略写锁。
 *
 * <p>标记会改变登录资格或会话策略的项目业务方法，统一约束分布式锁与数据库事务的
 * 顺序，避免登录/刷新路径的“Redis 锁→数据库”与管理路径的“数据库→Redis 锁”互锁。</p>
 *
 * @author luoqiz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthPolicyWriteLocked {

    /** 由业务模块实现的锁目标解析器。 */
    Class<? extends AuthPolicyLockTargetResolver> value();
}
