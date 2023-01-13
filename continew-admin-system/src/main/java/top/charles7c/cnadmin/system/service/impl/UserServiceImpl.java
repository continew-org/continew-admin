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

package top.charles7c.cnadmin.system.service.impl;

import java.io.File;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.config.properties.LocalStorageProperties;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.util.FileUtils;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.common.util.validate.ValidationUtils;
import top.charles7c.cnadmin.system.mapper.UserMapper;
import top.charles7c.cnadmin.system.model.entity.SysUser;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 用户业务实现类
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final LocalStorageProperties localStorageProperties;

    @Override
    public SysUser getByUsername(String username) {
        return userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile avatarFile, Long userId) {
        // 上传新头像
        String avatarPath = localStorageProperties.getPath().getAvatar();
        File newAvatarFile = FileUtils.upload(avatarFile, avatarPath, false);
        CheckUtils.exIfNull(newAvatarFile, "上传头像失败");
        assert newAvatarFile != null;

        // 更新用户头像
        String newAvatar = newAvatarFile.getName();
        userMapper.update(null,
            new LambdaUpdateWrapper<SysUser>().set(SysUser::getAvatar, newAvatar).eq(SysUser::getUserId, userId));

        // 删除原头像
        LoginUser loginUser = LoginHelper.getLoginUser();
        String oldAvatar = loginUser.getAvatar();
        if (StrUtil.isNotBlank(loginUser.getAvatar())) {
            FileUtil.del(avatarPath + oldAvatar);
        }

        // 更新登录用户信息
        loginUser.setAvatar(newAvatar);
        LoginHelper.updateLoginUser(loginUser);
        return newAvatar;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUser user) {
        userMapper.updateById(user);

        // 更新登录用户信息
        SysUser sysUser = this.getById(user.getUserId());
        LoginUser loginUser = LoginHelper.getLoginUser();
        BeanUtil.copyProperties(sysUser, loginUser);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPassword, String newPassword, Long userId) {
        SysUser sysUser = this.getById(userId);
        ValidationUtils.exIfNotEqual(SecureUtils.md5Salt(oldPassword, userId.toString()), sysUser.getPassword(),
            "当前密码错误");

        // 更新密码和密码重置时间
        LocalDateTime now = LocalDateTime.now();
        userMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, SecureUtils.md5Salt(newPassword, userId.toString()))
                .set(SysUser::getPwdResetTime, now).eq(SysUser::getUserId, userId));

        // 更新登录用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setPwdResetTime(now);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String newEmail, String currentPassword, Long userId) {
        // 校验
        SysUser sysUser = this.getById(userId);
        ValidationUtils.exIfNotEqual(SecureUtils.md5Salt(currentPassword, userId.toString()), sysUser.getPassword(),
            "当前密码错误");
        Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getEmail, newEmail));
        ValidationUtils.exIfCondition(() -> count > 0, "邮箱已绑定其他账号，请更换其他邮箱");
        ValidationUtils.exIfEqual(newEmail, sysUser.getEmail(), "新邮箱不能与当前邮箱相同");

        // 更新邮箱
        userMapper.update(null,
            new LambdaUpdateWrapper<SysUser>().set(SysUser::getEmail, newEmail).eq(SysUser::getUserId, userId));

        // 更新登录用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setEmail(newEmail);
        LoginHelper.updateLoginUser(loginUser);
    }

    /**
     * 根据 ID 查询
     *
     * @param userId
     *            用户 ID
     * @return 用户信息
     */
    private SysUser getById(Long userId) {
        ValidationUtils.exIfNull(userId, "用户不存在");
        SysUser sysUser = userMapper.selectById(userId);
        ValidationUtils.exIfNull(sysUser, String.format("ID为 [%s] 的用户已不存在", userId));
        return sysUser;
    }
}
