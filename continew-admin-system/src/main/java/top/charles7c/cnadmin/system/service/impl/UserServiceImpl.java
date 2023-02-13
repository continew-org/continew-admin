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
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.config.properties.LocalStorageProperties;
import top.charles7c.cnadmin.common.consts.FileConstants;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.service.CommonUserService;
import top.charles7c.cnadmin.common.util.FileUtils;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.UserMapper;
import top.charles7c.cnadmin.system.model.entity.UserDO;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 用户业务实现类
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, CommonUserService {

    private final UserMapper userMapper;
    private final LocalStorageProperties localStorageProperties;

    @Override
    public UserDO getByUsername(String username) {
        return userMapper.selectOne(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile avatarFile, Long userId) {
        Long avatarMaxSizeInMb = localStorageProperties.getAvatarMaxSizeInMb();
        CheckUtils.throwIf(() -> avatarFile.getSize() > avatarMaxSizeInMb * 1024 * 1024,
            String.format("请上传小于 %s MB 的图片", avatarMaxSizeInMb));
        String avatarImageType = FileNameUtil.extName(avatarFile.getOriginalFilename());
        String[] avatarSupportImgTypes = FileConstants.AVATAR_SUPPORTED_IMG_TYPES;
        CheckUtils.throwIf(() -> !StrUtil.equalsAnyIgnoreCase(avatarImageType, avatarSupportImgTypes),
            String.format("头像仅支持 %s 格式的图片", String.join("，", avatarSupportImgTypes)));

        // 上传新头像
        String avatarPath = localStorageProperties.getPath().getAvatar();
        File newAvatarFile = FileUtils.upload(avatarFile, avatarPath, false);
        CheckUtils.throwIfNull(newAvatarFile, "上传头像失败");
        assert newAvatarFile != null;

        // 更新用户头像
        String newAvatar = newAvatarFile.getName();
        userMapper.update(null,
            new LambdaUpdateWrapper<UserDO>().set(UserDO::getAvatar, newAvatar).eq(UserDO::getUserId, userId));

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
    public void update(UserDO user) {
        userMapper.updateById(user);

        // 更新登录用户信息
        UserDO userDO = this.getById(user.getUserId());
        LoginUser loginUser = LoginHelper.getLoginUser();
        BeanUtil.copyProperties(userDO, loginUser);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPassword, String newPassword, Long userId) {
        CheckUtils.throwIfEqual(newPassword, oldPassword, "新密码不能与当前密码相同");
        UserDO userDO = this.getById(userId);
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(oldPassword, userId.toString()), userDO.getPassword(), "当前密码错误");

        // 更新密码和密码重置时间
        LocalDateTime now = LocalDateTime.now();
        userMapper.update(null,
            new LambdaUpdateWrapper<UserDO>()
                .set(UserDO::getPassword, SecureUtils.md5Salt(newPassword, userId.toString()))
                .set(UserDO::getPwdResetTime, now).eq(UserDO::getUserId, userId));

        // 更新登录用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setPwdResetTime(now);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String newEmail, String currentPassword, Long userId) {
        UserDO userDO = this.getById(userId);
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(currentPassword, userId.toString()), userDO.getPassword(),
            "当前密码错误");
        Long count = userMapper.selectCount(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getEmail, newEmail));
        CheckUtils.throwIf(() -> count > 0, "邮箱已绑定其他账号，请更换其他邮箱");
        CheckUtils.throwIfEqual(newEmail, userDO.getEmail(), "新邮箱不能与当前邮箱相同");

        // 更新邮箱
        userMapper.update(null,
            new LambdaUpdateWrapper<UserDO>().set(UserDO::getEmail, newEmail).eq(UserDO::getUserId, userId));

        // 更新登录用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setEmail(newEmail);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    public UserDO getById(Long userId) {
        UserDO userDO = userMapper.selectById(userId);
        CheckUtils.throwIfNull(userDO, String.format("ID为 [%s] 的用户已不存在", userId));
        return userDO;
    }

    @Override
    public String getNicknameById(Long userId) {
        return this.getById(userId).getNickname();
    }
}
