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

package top.continew.admin.auth.model;

/**
 * 登录安全状态版本快照。
 *
 * <p>快照在最终状态复查前读取，并随 Refresh Session 保存。用户、客户端或租户发生
 * 强制失效操作时递增对应版本，能够使并发创建但未被索引扫描命中的会话立即失效。</p>
 *
 * @param userVersion   用户安全版本
 * @param clientVersion 客户端安全版本
 * @param tenantVersion 租户安全版本
 * @author luoqiz
 * @since 4.2.0
 */
public record AuthSecurityVersion(long userVersion, long clientVersion, long tenantVersion) {
}
