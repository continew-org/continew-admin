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

package top.continew.admin.config.tenant;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import top.continew.admin.common.constant.SysConstants;
import top.continew.starter.core.constant.PropertiesConstants;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.extension.tenant.enums.TenantIsolationLevel;

/**
 * @description: 主数据源切面
 * @author: 小熊
 * @create: 2025-01-15 16:02
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = PropertiesConstants.TENANT, name = PropertiesConstants.ENABLED, havingValue = "true")
public class DataSourceSwitchAspect {

    @Pointcut("execution(* top.continew.admin.tenant.mapper..*(..)) || " + "execution(* top.continew.admin.tenant.service..*(..)) || " + "execution(* top.continew.admin.system.mapper.ClientMapper.*(..)) || " + "execution(* top.continew.admin.system.service.ClientService.*(..)) || " + "execution(* top.continew.admin.system.mapper.DictMapper.*(..)) || " + "execution(* top.continew.admin.system.service.DictService.*(..)) || " + "execution(* top.continew.admin.system.mapper.DictItemMapper.*(..)) || " + "execution(* top.continew.admin.system.service.DictItemService.*(..)) || " + "execution(* top.continew.admin.system.mapper.OptionMapper.*(..)) || " + "execution(* top.continew.admin.system.service.OptionService.*(..)) || " + "execution(* top.continew.admin.system.mapper.StorageMapper.*(..)) || " + "execution(* top.continew.admin.system.service.StorageService.*(..))")
    public void MasterDataSourceMethods() {
    }

    @Before("MasterDataSourceMethods()")
    public void switchToMasterDataSource() {
        if (TenantContextHolder.getIsolationLevel() == TenantIsolationLevel.DATASOURCE) {
            DynamicDataSourceContextHolder.push(SysConstants.DEFAULT_DATASOURCE);
        }
    }

    @After("MasterDataSourceMethods()")
    public void clearDataSourceContext() {
        if (TenantContextHolder.getIsolationLevel() == TenantIsolationLevel.DATASOURCE) {
            DynamicDataSourceContextHolder.poll();
        }
    }

}
