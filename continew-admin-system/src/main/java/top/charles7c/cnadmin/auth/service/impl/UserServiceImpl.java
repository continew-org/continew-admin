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

import java.util.Date;

import org.springframework.stereotype.Service;

import top.charles7c.cnadmin.auth.model.entity.SysUser;
import top.charles7c.cnadmin.auth.service.UserService;
import top.charles7c.cnadmin.common.util.SecureUtils;

/**
 * 用户业务实现类
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
public class UserServiceImpl implements UserService {

    @Override
    public SysUser getByUsername(String username) {
        if (!"admin".equals(username)) {
            return null;
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUsername("admin");
        sysUser.setPassword(SecureUtils.md5Salt("123456", sysUser.getUserId().toString()));
        sysUser.setNickname("超级管理员");
        sysUser.setGender(1);
        sysUser.setStatus(1);
        sysUser.setCreateUser(1L);
        sysUser.setCreateTime(new Date());
        sysUser.setUpdateUser(1L);
        sysUser.setUpdateTime(new Date());
        return sysUser;
    }
}
