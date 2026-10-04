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

import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserExtraContext;
import top.continew.starter.auth.refresh.token.model.RefreshSessionPrincipal;

/**
 * 登录主体快照适配器：将 Admin 用户上下文映射为认证会话模块的主体快照。
 *
 * <p>认证会话模块不依赖用户实体，登录签发时由本适配器提供创建会话所需的主体信息，
 * 随 Refresh Session 一并保存，用于会话管理展示与失效原因判定。</p>
 *
 * @author Charles7c
 * @since 4.2.0
 */
public record RefreshSessionPrincipalAdapter(UserContext userContext,
    UserExtraContext extraContext) implements RefreshSessionPrincipal {

    @Override
    public Long getUserId() {
        return this.userContext.getId();
    }

    @Override
    public String getUsername() {
        return this.userContext.getUsername();
    }

    @Override
    public String getNickname() {
        return this.userContext.getNickname();
    }

    @Override
    public Long getTenantId() {
        return this.extraContext.getTenantId();
    }

    @Override
    public String getIp() {
        return this.extraContext.getIp();
    }

    @Override
    public String getAddress() {
        return this.extraContext.getAddress();
    }

    @Override
    public String getBrowser() {
        return this.extraContext.getBrowser();
    }

    @Override
    public String getOs() {
        return this.extraContext.getOs();
    }
}
