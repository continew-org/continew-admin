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

package top.charles7c.continew.admin.system.service.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.annotation.Resource;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.continew.admin.common.config.properties.LocalStorageProperties;
import top.charles7c.continew.admin.common.constant.CacheConstants;
import top.charles7c.continew.admin.common.constant.FileConstants;
import top.charles7c.continew.admin.common.constant.SysConstants;
import top.charles7c.continew.admin.common.enums.DisEnableStatusEnum;
import top.charles7c.continew.admin.common.util.SecureUtils;
import top.charles7c.continew.admin.common.util.helper.LoginHelper;
import top.charles7c.continew.admin.system.mapper.UserMapper;
import top.charles7c.continew.admin.system.model.entity.UserDO;
import top.charles7c.continew.admin.system.model.query.UserQuery;
import top.charles7c.continew.admin.system.model.req.UserBasicInfoUpdateReq;
import top.charles7c.continew.admin.system.model.req.UserReq;
import top.charles7c.continew.admin.system.model.req.UserRoleUpdateReq;
import top.charles7c.continew.admin.system.model.resp.UserDetailResp;
import top.charles7c.continew.admin.system.model.resp.UserResp;
import top.charles7c.continew.admin.system.service.DeptService;
import top.charles7c.continew.admin.system.service.RoleService;
import top.charles7c.continew.admin.system.service.UserRoleService;
import top.charles7c.continew.admin.system.service.UserService;
import top.charles7c.continew.starter.core.constant.StringConstants;
import top.charles7c.continew.starter.core.util.ExceptionUtils;
import top.charles7c.continew.starter.core.util.FileUploadUtils;
import top.charles7c.continew.starter.extension.crud.base.BaseServiceImpl;
import top.charles7c.continew.starter.extension.crud.base.CommonUserService;
import top.charles7c.continew.starter.extension.crud.util.validate.CheckUtils;

/**
 * 用户业务实现
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheConstants.USER_KEY_PREFIX)
public class UserServiceImpl extends BaseServiceImpl<UserMapper, UserDO, UserResp, UserDetailResp, UserQuery, UserReq>
    implements UserService, CommonUserService {

    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final LocalStorageProperties localStorageProperties;
    @Resource
    private DeptService deptService;

    @Override
    public Long save(UserDO user) {
        user.setStatus(DisEnableStatusEnum.ENABLE);
        baseMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(UserReq req) {
        String username = req.getUsername();
        CheckUtils.throwIf(this.isNameExists(username, null), "新增失败，[{}] 已存在", username);
        String email = req.getEmail();
        CheckUtils.throwIf(StrUtil.isNotBlank(email) && this.isEmailExists(email, null), "新增失败，[{}] 已存在", email);
        String phone = req.getPhone();
        CheckUtils.throwIf(StrUtil.isNotBlank(phone) && this.isPhoneExists(phone, null), "新增失败，[{}] 已存在", phone);
        // 新增信息
        req.setStatus(DisEnableStatusEnum.ENABLE);
        Long userId = super.add(req);
        baseMapper.lambdaUpdate()
            .set(UserDO::getPassword, SecureUtils.md5Salt(SysConstants.DEFAULT_PASSWORD, userId.toString()))
            .set(UserDO::getPwdResetTime, LocalDateTime.now()).eq(UserDO::getId, userId).update();
        // 保存用户和角色关联
        userRoleService.save(req.getRoleIds(), userId);
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserReq req, Long id) {
        String username = req.getUsername();
        CheckUtils.throwIf(this.isNameExists(username, id), "修改失败，[{}] 已存在", username);
        String email = req.getEmail();
        CheckUtils.throwIf(StrUtil.isNotBlank(email) && this.isEmailExists(email, id), "修改失败，[{}] 已存在", email);
        String phone = req.getPhone();
        CheckUtils.throwIf(StrUtil.isNotBlank(phone) && this.isPhoneExists(phone, id), "修改失败，[{}] 已存在", phone);
        DisEnableStatusEnum newStatus = req.getStatus();
        CheckUtils.throwIf(
            DisEnableStatusEnum.DISABLE.equals(newStatus) && ObjectUtil.equal(id, LoginHelper.getUserId()),
            "不允许禁用当前用户");
        UserDO oldUser = super.getById(id);
        if (oldUser.getIsSystem()) {
            CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, newStatus, "[{}] 是系统内置用户，不允许禁用",
                oldUser.getNickname());
            Collection<Long> disjunctionRoleIds =
                CollUtil.disjunction(req.getRoleIds(), userRoleService.listRoleIdByUserId(id));
            CheckUtils.throwIfNotEmpty(disjunctionRoleIds, "[{}] 是系统内置用户，不允许变更所属角色", oldUser.getNickname());
        }
        // 更新信息
        super.update(req, id);
        // 保存用户和角色关联
        userRoleService.save(req.getRoleIds(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        CheckUtils.throwIf(CollUtil.contains(ids, LoginHelper.getUserId()), "不允许删除当前用户");
        List<UserDO> list =
            baseMapper.lambdaQuery().select(UserDO::getNickname, UserDO::getIsSystem).in(UserDO::getId, ids).list();
        Optional<UserDO> isSystemData = list.stream().filter(UserDO::getIsSystem).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选用户 [{}] 是系统内置用户，不允许删除",
            isSystemData.orElseGet(UserDO::new).getNickname());
        // 删除用户和角色关联
        userRoleService.deleteByUserIds(ids);
        // 删除用户
        super.delete(ids);
    }

    @Override
    public void fillDetail(Object detailObj) {
        super.fillDetail(detailObj);
        if (detailObj instanceof UserDetailResp detail) {
            detail.setDeptName(ExceptionUtils.exToNull(() -> deptService.get(detail.getDeptId()).getName()));
            List<Long> roleIdList = userRoleService.listRoleIdByUserId(detail.getId());
            detail.setRoleIds(roleIdList);
            detail.setRoleNames(String.join(StringConstants.CHINESE_COMMA, roleService.listNameByIds(roleIdList)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile avatarFile, Long id) {
        Long avatarMaxSizeInMb = localStorageProperties.getAvatarMaxSizeInMb();
        CheckUtils.throwIf(avatarFile.getSize() > avatarMaxSizeInMb * 1024 * 1024, "请上传小于 {}MB 的图片", avatarMaxSizeInMb);
        String avatarImageType = FileNameUtil.extName(avatarFile.getOriginalFilename());
        String[] avatarSupportImgTypes = FileConstants.AVATAR_SUPPORTED_IMG_TYPES;
        CheckUtils.throwIf(!StrUtil.equalsAnyIgnoreCase(avatarImageType, avatarSupportImgTypes), "头像仅支持 {} 格式的图片",
            String.join(StringConstants.CHINESE_COMMA, avatarSupportImgTypes));
        // 上传新头像
        UserDO user = super.getById(id);
        String avatarPath = localStorageProperties.getPath().getAvatar();
        File newAvatarFile = FileUploadUtils.upload(avatarFile, avatarPath, false);
        CheckUtils.throwIfNull(newAvatarFile, "上传头像失败");
        assert null != newAvatarFile;
        // 更新用户头像
        String newAvatar = newAvatarFile.getName();
        baseMapper.lambdaUpdate().set(UserDO::getAvatar, newAvatar).eq(UserDO::getId, id).update();
        // 删除原头像
        String oldAvatar = user.getAvatar();
        if (StrUtil.isNotBlank(oldAvatar)) {
            FileUtil.del(avatarPath + oldAvatar);
        }
        return newAvatar;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBasicInfo(UserBasicInfoUpdateReq updateReq, Long id) {
        super.getById(id);
        baseMapper.lambdaUpdate().set(UserDO::getNickname, updateReq.getNickname())
            .set(UserDO::getGender, updateReq.getGender()).eq(UserDO::getId, id).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPassword, String newPassword, Long id) {
        CheckUtils.throwIfEqual(newPassword, oldPassword, "新密码不能与当前密码相同");
        UserDO user = super.getById(id);
        String password = user.getPassword();
        if (StrUtil.isNotBlank(password)) {
            CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(oldPassword, id.toString()), password, "当前密码错误");
        }
        // 更新密码和密码重置时间
        LocalDateTime now = LocalDateTime.now();
        baseMapper.lambdaUpdate().set(UserDO::getPassword, SecureUtils.md5Salt(newPassword, id.toString()))
            .set(UserDO::getPwdResetTime, now).eq(UserDO::getId, id).update();
    }

    @Override
    public void updatePhone(String newPhone, String currentPassword, Long id) {
        UserDO user = super.getById(id);
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(currentPassword, id.toString()), user.getPassword(), "当前密码错误");
        Long count = baseMapper.lambdaQuery().eq(UserDO::getPhone, newPhone).count();
        CheckUtils.throwIf(count > 0, "手机号已绑定其他账号，请更换其他手机号");
        CheckUtils.throwIfEqual(newPhone, user.getPhone(), "新手机号不能与当前手机号相同");
        // 更新手机号
        baseMapper.lambdaUpdate().set(UserDO::getPhone, newPhone).eq(UserDO::getId, id).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String newEmail, String currentPassword, Long id) {
        UserDO user = super.getById(id);
        CheckUtils.throwIfNotEqual(SecureUtils.md5Salt(currentPassword, id.toString()), user.getPassword(), "当前密码错误");
        Long count = baseMapper.lambdaQuery().eq(UserDO::getEmail, newEmail).count();
        CheckUtils.throwIf(count > 0, "邮箱已绑定其他账号，请更换其他邮箱");
        CheckUtils.throwIfEqual(newEmail, user.getEmail(), "新邮箱不能与当前邮箱相同");
        // 更新邮箱
        baseMapper.lambdaUpdate().set(UserDO::getEmail, newEmail).eq(UserDO::getId, id).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id) {
        UserDO user = super.getById(id);
        user.setPassword(SecureUtils.md5Salt(SysConstants.DEFAULT_PASSWORD, id.toString()));
        user.setPwdResetTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(UserRoleUpdateReq updateReq, Long id) {
        super.getById(id);
        // 保存用户和角色关联
        userRoleService.save(updateReq.getRoleIds(), id);
    }

    @Override
    public UserDO getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public UserDO getByPhone(String phone) {
        return baseMapper.selectByPhone(phone);
    }

    @Override
    public UserDO getByEmail(String email) {
        return baseMapper.selectByEmail(email);
    }

    @Override
    public Long countByDeptIds(List<Long> deptIds) {
        return baseMapper.lambdaQuery().in(UserDO::getDeptId, deptIds).count();
    }

    @Override
    @Cacheable(key = "#id")
    public String getNicknameById(Long id) {
        return baseMapper.selectNicknameById(id);
    }

    /**
     * 名称是否存在
     *
     * @param name
     *            名称
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean isNameExists(String name, Long id) {
        return baseMapper.lambdaQuery().eq(UserDO::getUsername, name).ne(null != id, UserDO::getId, id).exists();
    }

    /**
     * 邮箱是否存在
     *
     * @param email
     *            邮箱
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean isEmailExists(String email, Long id) {
        return baseMapper.lambdaQuery().eq(UserDO::getEmail, email).ne(null != id, UserDO::getId, id).exists();
    }

    /**
     * 手机号码是否存在
     *
     * @param phone
     *            手机号码
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean isPhoneExists(String phone, Long id) {
        return baseMapper.lambdaQuery().eq(UserDO::getPhone, phone).ne(null != id, UserDO::getId, id).exists();
    }
}
