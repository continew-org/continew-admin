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

import java.util.Collection;

/**
 * 将业务方法参数解析为认证会话策略锁目标。
 *
 * <p>实现由 system 或 tenant 插件提供，认证会话模块不引用任何业务 Mapper。</p>
 *
 * @author luoqiz
 */
public interface AuthPolicyLockTargetResolver {

    Collection<AuthPolicyLockTarget> resolve(Object[] args);
}
