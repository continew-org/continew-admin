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

package top.continew.admin.common.config.mybatis;

import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.extension.datapermission.enums.DataScope;
import top.continew.starter.extension.datapermission.model.RoleData;
import top.continew.starter.extension.datapermission.model.UserData;
import top.continew.starter.extension.datapermission.provider.DataPermissionUserDataProvider;

/**
 * 数据权限用户数据提供者
 *
 * @author Charles7c
 * @since 2023/12/21 21:19
 */
public class DefaultDataPermissionUserDataProvider implements DataPermissionUserDataProvider {

    @Override
    public boolean isFilter() {
        return !UserContextHolder.isSuperAdmin() && !UserContextHolder.isTenantAdmin();
    }

    @Override
    public UserData getUserData() {
        UserContext userContext = UserContextHolder.getContext();
        UserData userData = new UserData();
        userData.setUserId(userContext.getId());
        userData.setDeptId(userContext.getDeptId());
        userData.setRoles(CollUtils.mapToSet(userContext.getRoles(),
            r -> new RoleData(r.getId(), DataScope.valueOf(r
                .getDataScope()
                .name()))));
        return userData;
    }
}
