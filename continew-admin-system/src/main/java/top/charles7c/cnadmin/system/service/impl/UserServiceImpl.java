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
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.common.config.properties.LocalStorageProperties;
import top.charles7c.cnadmin.common.constant.FileConsts;
import top.charles7c.cnadmin.common.constant.StringConsts;
import top.charles7c.cnadmin.common.constant.SysConsts;
import top.charles7c.cnadmin.common.enums.DataTypeEnum;
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
import top.charles7c.cnadmin.system.service.*;

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
    private final RoleService roleService;
    private final LocalStorageProperties localStorageProperties;
    @Resource
    private DeptService deptService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(UserRequest request) {
        String username = request.getUsername();
        boolean isExists = this.checkNameExists(username, null);
        CheckUtils.throwIf(isExists, "新增失败，[{}] 已存在", username);

        // 新增信息
        request.setStatus(DisEnableStatusEnum.ENABLE);
        Long userId = super.add(request);
        baseMapper.lambdaUpdate()
            .set(UserDO::getPassword, SecureUtils.md5Salt(SysConsts.DEFAULT_PASSWORD, userId.toString()))
            .set(UserDO::getPwdResetTime, LocalDateTime.now()).eq(UserDO::getId, userId).update();
        // 保存用户和角色关联
        userRoleService.save(request.getRoleIds(), userId);
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserRequest request, Long id) {
        String username = request.getUsername();
        boolean isExists = this.checkNameExists(username, id);
        CheckUtils.throwIf(isExists, "修改失败，[{}] 已存在", username);
        UserDO oldUser = super.getById(id);
        if (DataTypeEnum.SYSTEM.equals(oldUser.getType())) {
            CheckUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(request.getStatus()), "[{}] 是系统内置用户，不允许禁用",
                oldUser.getNickname());
            List<Long> oldRoleIdList =
                userRoleService.listRoleIdByUserId(id).stream().sorted().collect(Collectors.toList());
            List<Long> newRoleIdList = request.getRoleIds().stream().sorted().collect(Collectors.toList());
            CheckUtils.throwIf(!CollUtil.isEqualList(newRoleIdList, oldRoleIdList), "[{}] 是系统内置用户，不允许变更所属角色",
                oldUser.getNickname());
        }

        // 更新信息
        super.update(request, id);
        // 保存用户和角色关联
        userRoleService.save(request.getRoleIds(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        List<UserDO> list =
            baseMapper.lambdaQuery().select(UserDO::getNickname, UserDO::getType).in(UserDO::getId, ids).list();
        Optional<UserDO> isSystemData = list.stream().filter(u -> DataTypeEnum.SYSTEM.equals(u.getType())).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选用户 [{}] 是系统内置用户，不允许删除",
            isSystemData.orElseGet(UserDO::new).getNickname());

        // 删除用户
        super.delete(ids);
        // 删除用户和角色关联
        userRoleService.deleteByUserIds(ids);
    }

    @Override
    public void fillDetail(Object detailObj) {
        super.fillDetail(detailObj);
        if (detailObj instanceof UserDetailVO) {
            UserDetailVO detailVO = (UserDetailVO)detailObj;
            detailVO.setDeptName(ExceptionUtils.exToNull(() -> deptService.get(detailVO.getDeptId()).getName()));
            List<Long> roleIdList = userRoleService.listRoleIdByUserId(detailVO.getId());
            detailVO.setRoleIds(roleIdList);
            detailVO.setRoleNames(String.join(StringConsts.CHINESE_COMMA, roleService.listNameByIds(roleIdList)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile avatarFile, Long id) {
        Long avatarMaxSizeInMb = localStorageProperties.getAvatarMaxSizeInMb();
        CheckUtils.throwIf(avatarFile.getSize() > avatarMaxSizeInMb * 1024 * 1024, "请上传小于 {}MB 的图片", avatarMaxSizeInMb);
        String avatarImageType = FileNameUtil.extName(avatarFile.getOriginalFilename());
        String[] avatarSupportImgTypes = FileConsts.AVATAR_SUPPORTED_IMG_TYPES;
        CheckUtils.throwIf(!StrUtil.equalsAnyIgnoreCase(avatarImageType, avatarSupportImgTypes), "头像仅支持 {} 格式的图片",
            String.join(StringConsts.CHINESE_COMMA, avatarSupportImgTypes));

        // 上传新头像
        String avatarPath = localStorageProperties.getPath().getAvatar();
        File newAvatarFile = FileUtils.upload(avatarFile, avatarPath, false);
        CheckUtils.throwIfNull(newAvatarFile, "上传头像失败");
        assert newAvatarFile != null;

        // 更新用户头像
        String newAvatar = newAvatarFile.getName();
        baseMapper.lambdaUpdate().set(UserDO::getAvatar, newAvatar).eq(UserDO::getId, id).update();

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
    public void updatePassword(String oldPassword, String newPassword, Long id) {
        CheckUtils.throwIfEqual(newPassword, oldPassword, "新密码不能与当前密码相同");
        UserDO userDO = super.getById(id);
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(oldPassword, id.toString()), userDO.getPassword(), "当前密码错误");

        // 更新密码和密码重置时间
        LocalDateTime now = LocalDateTime.now();
        baseMapper.lambdaUpdate().set(UserDO::getPassword, SecureUtils.md5Salt(newPassword, id.toString()))
            .set(UserDO::getPwdResetTime, now).eq(UserDO::getId, id).update();

        // 更新登录用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setPwdResetTime(now);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String newEmail, String currentPassword, Long id) {
        UserDO userDO = super.getById(id);
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(currentPassword, id.toString()), userDO.getPassword(), "当前密码错误");
        Long count = baseMapper.lambdaQuery().eq(UserDO::getEmail, newEmail).count();
        CheckUtils.throwIf(count > 0, "邮箱已绑定其他账号，请更换其他邮箱");
        CheckUtils.throwIfEqual(newEmail, userDO.getEmail(), "新邮箱不能与当前邮箱相同");

        // 更新邮箱
        baseMapper.lambdaUpdate().set(UserDO::getEmail, newEmail).eq(UserDO::getId, id).update();

        // 更新登录用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setEmail(newEmail);
        LoginHelper.updateLoginUser(loginUser);
    }

    @Override
    public void resetPassword(Long id) {
        UserDO userDO = super.getById(id);
        userDO.setPassword(SecureUtils.md5Salt(SysConsts.DEFAULT_PASSWORD, id.toString()));
        userDO.setPwdResetTime(LocalDateTime.now());
        baseMapper.updateById(userDO);
    }

    @Override
    public void updateRole(UpdateUserRoleRequest request, Long id) {
        super.getById(id);
        // 保存用户和角色关联
        userRoleService.save(request.getRoleIds(), id);
    }

    @Override
    public UserDO getByUsername(String username) {
        return baseMapper.lambdaQuery().eq(UserDO::getUsername, username).one();
    }

    @Override
    public Long countByDeptIds(List<Long> deptIds) {
        return baseMapper.lambdaQuery().in(UserDO::getDeptId, deptIds).count();
    }

    @Override
    public String getNicknameById(Long id) {
        return super.getById(id).getNickname();
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
        return baseMapper.lambdaQuery().eq(UserDO::getUsername, name).ne(id != null, UserDO::getId, id).exists();
    }
}
