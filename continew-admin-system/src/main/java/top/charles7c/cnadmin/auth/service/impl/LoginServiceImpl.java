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

package top.charles7c.cnadmin.auth.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;

import top.charles7c.cnadmin.auth.service.LoginService;
import top.charles7c.cnadmin.auth.service.PermissionService;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.model.entity.UserDO;
import top.charles7c.cnadmin.system.service.DeptService;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 登录业务实现类
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserService userService;
    private final DeptService deptService;
    private final PermissionService permissionService;

    @Override
    public String login(String username, String password) {
        UserDO userDO = userService.getByUsername(username);
        CheckUtils.throwIfNull(userDO, "用户名或密码错误");
        Long userId = userDO.getUserId();
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(password, userId.toString()), userDO.getPassword(), "用户名或密码错误");
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, userDO.getStatus(), "此账号已被禁用，如有疑问，请联系管理员");

        // 登录
        LoginUser loginUser = BeanUtil.copyProperties(userDO, LoginUser.class);
        loginUser.setDeptName(ExceptionUtils.exToNull(() -> deptService.get(loginUser.getDeptId()).getDeptName()));
        loginUser.setPermissions(permissionService.listPermissionByUserId(userId));
        loginUser.setRoles(permissionService.listRoleCodeByUserId(userId));
        LoginHelper.login(loginUser);

        // 返回令牌
        return StpUtil.getTokenValue();
    }
}
