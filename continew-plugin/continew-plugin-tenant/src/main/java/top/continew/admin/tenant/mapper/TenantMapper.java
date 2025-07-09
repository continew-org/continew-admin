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

package top.continew.admin.tenant.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.tenant.model.entity.TenantDO;
import top.continew.admin.tenant.model.resp.TenantResp;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 租户 Mapper
 *
 * @author 小熊
 * @since 2024/11/26 17:20
 */
@DS(SysConstants.DEFAULT_DATASOURCE)
@Mapper
public interface TenantMapper extends BaseMapper<TenantDO> {

    @Select("SELECT sys_tenant.*,sys_tenant_package.`name` as package_name FROM sys_tenant\n" + "LEFT JOIN sys_tenant_package ON sys_tenant.package_id = sys_tenant_package.id\n" + "${ew.getCustomSqlSegment}")
    IPage<TenantResp> listTenant(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper);

}