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

import cn.hutool.core.convert.Convert;
import top.continew.admin.common.model.dto.LoginUser;
import top.continew.admin.common.util.helper.LoginHelper;
import top.continew.starter.data.mybatis.plus.datapermission.DataPermissionCurrentUser;
import top.continew.starter.data.mybatis.plus.datapermission.DataPermissionFilter;
import top.continew.starter.data.mybatis.plus.datapermission.DataScope;

import java.util.stream.Collectors;

/**
 * 数据权限过滤器实现类
 *
 * @author Charles7c
 * @since 2023/12/21 21:19
 */
public class DataPermissionFilterImpl implements DataPermissionFilter {

    @Override
    public boolean isFilter() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return !loginUser.isAdmin();
    }

    @Override
    public DataPermissionCurrentUser getCurrentUser() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        DataPermissionCurrentUser currentUser = new DataPermissionCurrentUser();
        currentUser.setUserId(Convert.toStr(loginUser.getId()));
        currentUser.setDeptId(Convert.toStr(loginUser.getDeptId()));
        currentUser.setRoles(loginUser.getRoles()
            .stream()
            .map(r -> new DataPermissionCurrentUser.CurrentUserRole(Convert.toStr(r.getId()), DataScope.valueOf(r
                .getDataScope()
                .name())))
            .collect(Collectors.toSet()));
        return currentUser;
    }
}
