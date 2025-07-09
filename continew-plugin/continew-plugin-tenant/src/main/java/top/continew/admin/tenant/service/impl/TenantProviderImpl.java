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

package top.continew.admin.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zaxxer.hikari.HikariConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.resp.TenantDbConnectDetailResp;
import top.continew.admin.tenant.service.TenantDbConnectService;
import top.continew.admin.tenant.service.TenantService;
import top.continew.admin.tenant.util.DbConnectUtil;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.tenant.config.TenantDataSource;
import top.continew.starter.extension.tenant.config.TenantProvider;
import top.continew.starter.extension.tenant.context.TenantContext;
import top.continew.starter.extension.tenant.enums.TenantIsolationLevel;

/**
 * @description: 租户数据源提供者实现
 * @author: 小熊
 * @create: 2024-12-12 15:35
 */
@Service
@RequiredArgsConstructor
public class TenantProviderImpl implements TenantProvider {

    private final TenantService tenantService;
    private final TenantDbConnectService tenantDbConnectService;

    @Override
    public TenantContext getByTenantId(String tenantId, boolean verify) {
        TenantContext context = new TenantContext();
        if (StrUtil.isNotEmpty(tenantId) && !SysConstants.DEFAULT_TENANT.equals(tenantId)) {
            Long longTenantId = Long.valueOf(tenantId);
            TenantDO tenantDO = tenantService.getTenantById(longTenantId);
            CheckUtils.throwIfNull(tenantDO, "租户[{}]不存在", tenantId);
            CheckUtils.throwIf(verify && DisEnableStatusEnum.DISABLE.getValue()
                .equals(tenantDO.getStatus()), "租户[{}]已被禁用", tenantId);
            context.setTenantId(longTenantId);
            TenantIsolationLevel isolationLevel = TenantIsolationLevel.DATASOURCE.ordinal() == tenantDO
                .getIsolationLevel() ? TenantIsolationLevel.DATASOURCE : TenantIsolationLevel.LINE;
            context.setIsolationLevel(isolationLevel);
            if (isolationLevel.equals(TenantIsolationLevel.DATASOURCE)) {
                TenantDbConnectDetailResp dbConnectReq = tenantDbConnectService.get(tenantDO.getDbConnectId());
                String dbName = SysConstants.TENANT_DB_PREFIX + tenantDO.getTenantSn();
                HikariConfig hikariConfig = DbConnectUtil.formatHikariConfig(dbConnectReq.getHost(), dbConnectReq
                    .getPort(), dbConnectReq.getUsername(), dbConnectReq.getPassword(), dbName, DbConnectUtil
                        .getDefaultMysqlConnectParameter());
                TenantDataSource source = new TenantDataSource();
                source.setPoolName(tenantId);
                source.setDriverClassName(hikariConfig.getDriverClassName());
                source.setUrl(hikariConfig.getJdbcUrl());
                source.setUsername(hikariConfig.getUsername());
                source.setPassword(hikariConfig.getPassword());
                context.setDataSource(source);
            }
        } else {
            context.setTenantId(Long.valueOf(SysConstants.DEFAULT_TENANT));
            context.setIsolationLevel(TenantIsolationLevel.LINE);
        }
        return context;
    }

}
