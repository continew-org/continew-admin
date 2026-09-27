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

/**
 * 认证会话撤销通知器。
 *
 * @author luoqiz
 */
public interface AuthSessionRevocationNotifier {

    /**
     * 通知所有实时连接撤销指定登录会话。
     *
     * @param sessionId Refresh Session ID
     */
    void notifyRevoked(String sessionId);
}
