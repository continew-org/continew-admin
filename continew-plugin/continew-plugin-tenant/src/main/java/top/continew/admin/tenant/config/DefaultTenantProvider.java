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

package top.continew.admin.tenant.config;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.tenant.service.TenantService;
import top.continew.starter.core.util.ServletUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.tenant.config.TenantProvider;
import top.continew.starter.extension.tenant.context.TenantContext;

/**
 * 默认租户提供者
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/12/12 15:35
 */
@RequiredArgsConstructor
public class DefaultTenantProvider implements TenantProvider {

    private final TenantExtensionProperties tenantExtensionProperties;
    private final TenantService tenantService;

    @Override
    public TenantContext getByTenantId(String tenantIdAsString, boolean verify) {
        TenantContext context = new TenantContext();
        Long defaultTenantId = tenantExtensionProperties.getDefaultTenantId();
        context.setTenantId(defaultTenantId);
        // 请求入口（verify=true，TenantInterceptor 从请求头解析租户）在刷新/退出时忽略
        // 前端残留的租户请求头，统一返回默认租户；具体租户由业务层根据 RefreshSession
        // 的 tenantId 显式重新进入上下文（TenantUtils.execute 传 verify=false），不受此限制。
        if (verify && this.isTenantIndependentAuthRequest()) {
            return context;
        }
        // 默认租户
        if (defaultTenantId != null && defaultTenantId.toString().equals(tenantIdAsString)) {
            return context;
        }
        Long tenantId;
        // 未指定租户
        if (StrUtil.isBlank(tenantIdAsString)) {
            // 检查是否指定了租户编码（登录相关接口）
            HttpServletRequest request = ServletUtils.getRequest();
            // 异步任务没有 Servlet Request 上下文，也没有可供解析的租户请求头；此时
            // 保持默认租户。显式传入 tenantId 的异步任务会在下面的分支正常处理。
            if (request == null) {
                return context;
            }
            String tenantCode = request.getHeader(tenantExtensionProperties.getTenantCodeHeader());
            if (StrUtil.isBlank(tenantCode)) {
                return context;
            }
            Long id = tenantService.getIdByCode(tenantCode);
            CheckUtils.throwIfNull(id, "编码为 [%s] 的租户不存在".formatted(tenantCode));
            tenantId = id;
        } else {
            // 指定租户
            tenantId = Long.parseLong(tenantIdAsString);
        }
        // 检查租户状态
        if (verify) {
            tenantService.checkStatus(tenantId);
        }
        context.setTenantId(tenantId);
        return context;
    }

    private boolean isTenantIndependentAuthRequest() {
        HttpServletRequest request = ServletUtils.getRequest();
        // 权限、角色等异步加载任务不继承 Servlet Request。没有请求上下文不代表刷新或
        // 退出接口，必须继续使用 TenantUtils.execute 显式传入的租户 ID。
        if (request == null) {
            return false;
        }
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = StrUtil.removePrefix(requestUri, contextPath);
        // 支持网关未重写的 /api、/v1 等前缀；租户认证接口不能因代理前缀变化而重新
        // 读取前端租户请求头，否则普通租户的 Cookie 刷新会被误判为跨租户请求。
        return path.endsWith("/auth/refresh") || path.endsWith("/auth/logout");
    }
}
