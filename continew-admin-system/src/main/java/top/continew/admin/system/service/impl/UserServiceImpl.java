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

package top.continew.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.util.helper.LoginHelper;
import top.continew.admin.system.mapper.UserMapper;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.UserDO;
import top.continew.admin.system.model.query.UserQuery;
import top.continew.admin.system.model.req.UserBasicInfoUpdateReq;
import top.continew.admin.system.model.req.UserPasswordResetReq;
import top.continew.admin.system.model.req.UserReq;
import top.continew.admin.system.model.req.UserRoleUpdateReq;
import top.continew.admin.system.model.resp.UserDetailResp;
import top.continew.admin.system.model.resp.UserResp;
import top.continew.admin.system.service.*;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.crud.service.CommonUserService;
import top.continew.starter.extension.crud.service.impl.BaseServiceImpl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static top.continew.admin.system.enums.PasswordPolicyEnum.*;

/**
 * 用户业务实现
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, UserDO, UserResp, UserDetailResp, UserQuery, UserReq> implements UserService, CommonUserService {

    private final OnlineUserService onlineUserService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final FileService fileService;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final OptionService optionService;
    private final UserPasswordHistoryService userPasswordHistoryService;
    @Resource
    private DeptService deptService;
    @Value("${avatar.support-suffix}")
    private String[] avatarSupportSuffix;

    @Override
    public PageResp<UserResp> page(UserQuery query, PageQuery pageQuery) {
        QueryWrapper<UserDO> queryWrapper = this.buildQueryWrapper(query);
        IPage<UserDO> page = baseMapper.selectUserPage(pageQuery.toPage(), queryWrapper);
        PageResp<UserResp> pageResp = PageResp.build(page, this.listClass);
        pageResp.getList().forEach(this::fill);
        return pageResp;
    }

    @Override
    public Long add(UserDO user) {
        user.setStatus(DisEnableStatusEnum.ENABLE);
        baseMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheUpdate(key = "#id", value = "#req.nickname", name = CacheConstants.USER_KEY_PREFIX)
    public void update(UserReq req, Long id) {
        final String errorMsgTemplate = "修改失败，[{}] 已存在";
        String username = req.getUsername();
        CheckUtils.throwIf(this.isNameExists(username, id), errorMsgTemplate, username);
        String email = req.getEmail();
        CheckUtils.throwIf(StrUtil.isNotBlank(email) && this.isEmailExists(email, id), errorMsgTemplate, email);
        String phone = req.getPhone();
        CheckUtils.throwIf(StrUtil.isNotBlank(phone) && this.isPhoneExists(phone, id), errorMsgTemplate, phone);
        DisEnableStatusEnum newStatus = req.getStatus();
        CheckUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(newStatus) && ObjectUtil.equal(id, LoginHelper
            .getUserId()), "不允许禁用当前用户");
        UserDO oldUser = super.getById(id);
        if (Boolean.TRUE.equals(oldUser.getIsSystem())) {
            CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, newStatus, "[{}] 是系统内置用户，不允许禁用", oldUser
                .getNickname());
            Collection<Long> disjunctionRoleIds = CollUtil.disjunction(req.getRoleIds(), userRoleService
                .listRoleIdByUserId(id));
            CheckUtils.throwIfNotEmpty(disjunctionRoleIds, "[{}] 是系统内置用户，不允许变更角色", oldUser.getNickname());
        }
        // 更新信息
        UserDO newUser = BeanUtil.toBean(req, UserDO.class);
        newUser.setId(id);
        baseMapper.updateById(newUser);
        // 保存用户和角色关联
        boolean isSaveUserRoleSuccess = userRoleService.add(req.getRoleIds(), id);
        // 如果功能权限或数据权限有变更，则清除关联的在线用户（重新登录以获取最新角色权限）
        if (DisEnableStatusEnum.DISABLE.equals(newStatus) || isSaveUserRoleSuccess) {
            onlineUserService.cleanByUserId(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(key = "#ids", name = CacheConstants.USER_KEY_PREFIX, multi = true)
    public void delete(List<Long> ids) {
        CheckUtils.throwIf(CollUtil.contains(ids, LoginHelper.getUserId()), "不允许删除当前用户");
        List<UserDO> list = baseMapper.lambdaQuery()
            .select(UserDO::getNickname, UserDO::getIsSystem)
            .in(UserDO::getId, ids)
            .list();
        Optional<UserDO> isSystemData = list.stream().filter(UserDO::getIsSystem).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选用户 [{}] 是系统内置用户，不允许删除", isSystemData.orElseGet(UserDO::new)
            .getNickname());
        // 删除用户和角色关联
        userRoleService.deleteByUserIds(ids);
        // 删除用户
        super.delete(ids);
    }

    @Override
    public void resetPassword(UserPasswordResetReq req, Long id) {
        UserDO user = super.getById(id);
        user.setPassword(req.getNewPassword());
        user.setPwdResetTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    @Override
    public void updateRole(UserRoleUpdateReq updateReq, Long id) {
        super.getById(id);
        // 保存用户和角色关联
        userRoleService.add(updateReq.getRoleIds(), id);
    }

    @Override
    public String uploadAvatar(MultipartFile avatarFile, Long id) {
        String avatarImageType = FileNameUtil.extName(avatarFile.getOriginalFilename());
        CheckUtils.throwIf(!StrUtil.equalsAnyIgnoreCase(avatarImageType, avatarSupportSuffix), "头像仅支持 {} 格式的图片", String
            .join(StringConstants.CHINESE_COMMA, avatarSupportSuffix));
        // 上传新头像
        UserDO user = super.getById(id);
        FileInfo fileInfo = fileService.upload(avatarFile);
        // 更新用户头像
        String newAvatar = fileInfo.getUrl();
        baseMapper.lambdaUpdate().set(UserDO::getAvatar, newAvatar).eq(UserDO::getId, id).update();
        // 删除原头像
        String oldAvatar = user.getAvatar();
        if (StrUtil.isNotBlank(oldAvatar)) {
            fileStorageService.delete(oldAvatar);
        }
        return newAvatar;
    }

    @Override
    @CacheUpdate(key = "#id", value = "#req.nickname", name = CacheConstants.USER_KEY_PREFIX)
    public void updateBasicInfo(UserBasicInfoUpdateReq req, Long id) {
        super.getById(id);
        baseMapper.lambdaUpdate()
            .set(UserDO::getNickname, req.getNickname())
            .set(UserDO::getGender, req.getGender())
            .eq(UserDO::getId, id)
            .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPassword, String newPassword, Long id) {
        CheckUtils.throwIfEqual(newPassword, oldPassword, "新密码不能与当前密码相同");
        UserDO user = super.getById(id);
        String password = user.getPassword();
        if (StrUtil.isNotBlank(password)) {
            CheckUtils.throwIf(!passwordEncoder.matches(oldPassword, password), "当前密码错误");
        }
        // 校验密码合法性
        int passwordReusePolicy = this.checkPassword(newPassword, user);
        // 更新密码和密码重置时间
        user.setPassword(newPassword);
        user.setPwdResetTime(LocalDateTime.now());
        baseMapper.updateById(user);
        // 保存历史密码
        userPasswordHistoryService.add(id, password, passwordReusePolicy);
    }

    @Override
    public boolean isPasswordExpired(LocalDateTime pwdResetTime) {
        // 永久有效
        int passwordExpirationDays = optionService.getValueByCode2Int(PASSWORD_EXPIRATION_DAYS.name());
        if (passwordExpirationDays <= SysConstants.NO) {
            return false;
        }
        // 初始密码也提示修改
        if (pwdResetTime == null) {
            return true;
        }
        return pwdResetTime.plusDays(passwordExpirationDays).isBefore(LocalDateTime.now());
    }

    @Override
    public void updatePhone(String newPhone, String oldPassword, Long id) {
        UserDO user = super.getById(id);
        CheckUtils.throwIf(!passwordEncoder.matches(oldPassword, user.getPassword()), "当前密码错误");
        CheckUtils.throwIf(this.isPhoneExists(newPhone, id), "手机号已绑定其他账号，请更换其他手机号");
        CheckUtils.throwIfEqual(newPhone, user.getPhone(), "新手机号不能与当前手机号相同");
        // 更新手机号
        baseMapper.lambdaUpdate().set(UserDO::getPhone, newPhone).eq(UserDO::getId, id).update();
    }

    @Override
    public void updateEmail(String newEmail, String oldPassword, Long id) {
        UserDO user = super.getById(id);
        CheckUtils.throwIf(!passwordEncoder.matches(oldPassword, user.getPassword()), "当前密码错误");
        CheckUtils.throwIf(this.isEmailExists(newEmail, id), "邮箱已绑定其他账号，请更换其他邮箱");
        CheckUtils.throwIfEqual(newEmail, user.getEmail(), "新邮箱不能与当前邮箱相同");
        // 更新邮箱
        baseMapper.lambdaUpdate().set(UserDO::getEmail, newEmail).eq(UserDO::getId, id).update();
    }

    @Override
    public void resetPassword(UserPasswordResetReq req, Long id) {
        UserDO user = super.getById(id);
        user.setPassword(req.getNewPassword());
        user.setPwdResetTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(UserRoleUpdateReq updateReq, Long id) {
        super.getById(id);
        // 保存用户和角色关联
        userRoleService.add(updateReq.getRoleIds(), id);
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
    @Cached(key = "#id", cacheType = CacheType.BOTH, name = CacheConstants.USER_KEY_PREFIX, syncLocal = true)
    public String getNicknameById(Long id) {
        return baseMapper.selectNicknameById(id);
    }

    @Override
    protected QueryWrapper<UserDO> buildQueryWrapper(UserQuery query) {
        String description = query.getDescription();
        Integer status = query.getStatus();
        List<Date> createTimeList = query.getCreateTime();
        Long deptId = query.getDeptId();
        return new QueryWrapper<UserDO>().and(StrUtil.isNotBlank(description), q -> q.like("t1.username", description)
            .or()
            .like("t1.nickname", description)
            .or()
            .like("t1.description", description))
            .eq(null != status, "t1.status", status)
            .between(CollUtil.isNotEmpty(createTimeList), "t1.create_time", CollUtil.getFirst(createTimeList), CollUtil
                .getLast(createTimeList))
            .and(null != deptId, q -> {
                List<Long> deptIdList = deptService.listChildren(deptId)
                    .stream()
                    .map(DeptDO::getId)
                    .collect(Collectors.toList());
                deptIdList.add(deptId);
                q.in("t1.dept_id", deptIdList);
            });
    }

    @Override
    protected void beforeAdd(UserReq req) {
        final String errorMsgTemplate = "新增失败，[{}] 已存在";
        String username = req.getUsername();
        CheckUtils.throwIf(this.isNameExists(username, null), errorMsgTemplate, username);
        String email = req.getEmail();
        CheckUtils.throwIf(StrUtil.isNotBlank(email) && this.isEmailExists(email, null), errorMsgTemplate, email);
        String phone = req.getPhone();
        CheckUtils.throwIf(StrUtil.isNotBlank(phone) && this.isPhoneExists(phone, null), errorMsgTemplate, phone);
    }

    @Override
    protected void afterAdd(UserReq req, UserDO user) {
        Long userId = user.getId();
        baseMapper.lambdaUpdate().set(UserDO::getPwdResetTime, LocalDateTime.now()).eq(UserDO::getId, userId).update();
        // 保存用户和角色关联
        userRoleService.add(req.getRoleIds(), userId);
    }

    /**
     * 检测密码合法性
     *
     * @param password 密码
     * @param user     用户信息
     */
    private int checkPassword(String password, UserDO user) {
        // 密码最小长度
        PASSWORD_MIN_LENGTH.validate(password, optionService.getValueByCode2Int(PASSWORD_MIN_LENGTH.name()), user);
        // 密码是否必须包含特殊字符
        PASSWORD_CONTAIN_SPECIAL_CHARACTERS.validate(password, optionService
            .getValueByCode2Int(PASSWORD_CONTAIN_SPECIAL_CHARACTERS.name()), user);
        // 密码是否允许包含正反序账号名
        PASSWORD_ALLOW_CONTAIN_USERNAME.validate(password, optionService
            .getValueByCode2Int(PASSWORD_ALLOW_CONTAIN_USERNAME.name()), user);
        // 密码重复使用规则
        int passwordReusePolicy = optionService.getValueByCode2Int(PASSWORD_REUSE_POLICY.name());
        PASSWORD_REUSE_POLICY.validate(password, passwordReusePolicy, user);
        return passwordReusePolicy;
    }

    /**
     * 名称是否存在
     *
     * @param name 名称
     * @param id   ID
     * @return 是否存在
     */
    private boolean isNameExists(String name, Long id) {
        return baseMapper.lambdaQuery().eq(UserDO::getUsername, name).ne(null != id, UserDO::getId, id).exists();
    }

    /**
     * 邮箱是否存在
     *
     * @param email 邮箱
     * @param id    ID
     * @return 是否存在
     */
    private boolean isEmailExists(String email, Long id) {
        Long count = baseMapper.selectCountByEmail(email, id);
        return null != count && count > 0;
    }

    /**
     * 手机号码是否存在
     *
     * @param phone 手机号码
     * @param id    ID
     * @return 是否存在
     */
    private boolean isPhoneExists(String phone, Long id) {
        Long count = baseMapper.selectCountByPhone(phone, id);
        return null != count && count > 0;
    }
}
