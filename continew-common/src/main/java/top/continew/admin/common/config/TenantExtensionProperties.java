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

package top.continew.admin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.continew.starter.core.constant.PropertiesConstants;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.util.List;

/**
 * 租户扩展配置属性
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/29 12:05
 */
@Data
@ConfigurationProperties(prefix = PropertiesConstants.TENANT)
public class TenantExtensionProperties {

    /**
     * 请求头中租户编码键名（默认：X-Tenant-Code）
     */
    private String tenantCodeHeader = "X-Tenant-Code";

    /**
     * 默认租户 ID（默认：0）
     */
    private Long defaultTenantId = 0L;

    /**
     * 忽略菜单 ID（租户不能使用的菜单）
     */
    private List<Long> ignoreMenus;

    /**
     * 是否为默认租户
     *
     * @return 是否为默认租户
     */
    public boolean isDefaultTenant() {
        return defaultTenantId != null && defaultTenantId.equals(TenantContextHolder.getTenantId());
    }
}
