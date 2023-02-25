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
import java.util.List;

import javax.annotation.Resource;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.config.properties.LocalStorageProperties;
import top.charles7c.cnadmin.common.consts.Constants;
import top.charles7c.cnadmin.common.consts.FileConstants;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.service.CommonUserService;
import top.charles7c.cnadmin.common.util.ExceptionUtils;
import top.charles7c.cnadmin.common.util.FileUtils;
import top.charles7c.cnadmin.common.util.SecureUtils;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.system.mapper.UserMapper;
import top.charles7c.cnadmin.system.model.entity.UserDO;
import top.charles7c.cnadmin.system.model.query.UserQuery;
import top.charles7c.cnadmin.system.model.request.UpdateUserRoleRequest;
import top.charles7c.cnadmin.system.model.request.UserRequest;
import top.charles7c.cnadmin.system.model.vo.UserDetailVO;
import top.charles7c.cnadmin.system.model.vo.UserVO;
import top.charles7c.cnadmin.system.service.DeptService;
import top.charles7c.cnadmin.system.service.RoleService;
import top.charles7c.cnadmin.system.service.UserRoleService;
import top.charles7c.cnadmin.system.service.UserService;

/**
 * 用户业务实现类
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, UserDO, UserVO, UserDetailVO, UserQuery, UserRequest>
    implements UserService, CommonUserService {

    private final UserRoleService userRoleService;
    private final LocalStorageProperties localStorageProperties;
    @Resource
    private RoleService roleService;
    @Resource
    private DeptService deptService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(UserRequest request) {
        String username = request.getUsername();
        boolean isExist = this.checkNameExists(username, request.getUserId());
        CheckUtils.throwIf(() -> isExist, String.format("新增失败，'%s'已存在", username));

        // 新增用户
        request.setStatus(DisEnableStatusEnum.ENABLE);
        Long userId = super.add(request);
        super.lambdaUpdate()
            .set(UserDO::getPassword, SecureUtils.md5Salt(Constants.DEFAULT_PASSWORD, userId.toString()))
            .set(UserDO::getPwdResetTime, LocalDateTime.now()).eq(UserDO::getUserId, userId).update();
        // 保存用户和角色关联
        userRoleService.save(request.getRoleIds(), userId);
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserRequest request) {
        String username = request.getUsername();
        boolean isExist = this.checkNameExists(username, request.getUserId());
        CheckUtils.throwIf(() -> isExist, String.format("修改失败，'%s'已存在", username));

        // 更新用户
        super.update(request);
        Long userId = request.getUserId();
        // 保存用户和角色关联
        userRoleService.save(request.getRoleIds(), userId);
    }

    /**
     * 检查名称是否存在
     *
     * @param name
     *            名称
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean checkNameExists(String name, Long id) {
        return super.lambdaQuery().eq(UserDO::getUsername, name).ne(id != null, UserDO::getUserId, id).exists();
    }

    @Override
    public void fillDetail(Object detailObj) {
        super.fillDetail(detailObj);
        if (detailObj instanceof UserDetailVO) {
            UserDetailVO detailVO = (UserDetailVO)detailObj;
            detailVO.setDeptName(ExceptionUtils.exToNull(() -> deptService.get(detailVO.getDeptId()).getDeptName()));
            List<Long> roleIds = userRoleService.listRoleIdsByUserId(detailVO.getUserId());
            detailVO.setRoleIds(roleIds);
            detailVO.setRoleNames(String.join(",", roleService.listRoleNamesByRoleIds(roleIds)));
        }
    }

    @Override
    public UserDO getByUsername(String username) {
        return super.lambdaQuery().eq(UserDO::getUsername, username).one();
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
        super.lambdaUpdate().set(UserDO::getAvatar, newAvatar).eq(UserDO::getUserId, userId).update();

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
    public void updatePassword(String oldPassword, String newPassword, Long userId) {
        CheckUtils.throwIfEqual(newPassword, oldPassword, "新密码不能与当前密码相同");
        UserDO userDO = super.getById(userId);
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(oldPassword, userId.toString()), userDO.getPassword(), "当前密码错误");

        // 更新密码和密码重置时间
        LocalDateTime now = LocalDateTime.now();
        super.lambdaUpdate().set(UserDO::getPassword, SecureUtils.md5Salt(newPassword, userId.toString()))
            .set(UserDO::getPwdResetTime, now).eq(UserDO::getUserId, userId).update();

        // 更新登录用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setPwdResetTime(now);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String newEmail, String currentPassword, Long userId) {
        UserDO userDO = super.getById(userId);
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(currentPassword, userId.toString()), userDO.getPassword(),
            "当前密码错误");
        Long count = super.lambdaQuery().eq(UserDO::getEmail, newEmail).count();
        CheckUtils.throwIf(() -> count > 0, "邮箱已绑定其他账号，请更换其他邮箱");
        CheckUtils.throwIfEqual(newEmail, userDO.getEmail(), "新邮箱不能与当前邮箱相同");

        // 更新邮箱
        super.lambdaUpdate().set(UserDO::getEmail, newEmail).eq(UserDO::getUserId, userId).update();

        // 更新登录用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setEmail(newEmail);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    public void resetPassword(Long userId) {
        UserDO userDO = super.getById(userId);
        userDO.setPassword(SecureUtils.md5Salt(Constants.DEFAULT_PASSWORD, userId.toString()));
        userDO.setPwdResetTime(LocalDateTime.now());
        baseMapper.updateById(userDO);
    }

    @Override
    public void updateUserRole(UpdateUserRoleRequest request, Long userId) {
        super.getById(userId);
        // 保存用户和角色关联
        userRoleService.save(request.getRoleIds(), userId);
    }

    @Override
    public Long countByDeptIds(List<Long> deptIds) {
        return super.lambdaQuery().in(UserDO::getDeptId, deptIds).count();
    }

    @Override
    public Long countByRoleIds(List<Long> roleIds) {
        return userRoleService.countByRoleIds(roleIds);
    }

    @Override
    public String getNicknameById(Long userId) {
        return super.getById(userId).getNickname();
    }
}
