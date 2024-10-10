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

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.validation.ValidationUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import net.dreamlu.mica.core.result.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.GenderEnum;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.system.mapper.UserMapper;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.UserDO;
import top.continew.admin.system.model.entity.UserRoleDO;
import top.continew.admin.system.model.query.UserQuery;
import top.continew.admin.system.model.req.*;
import top.continew.admin.system.model.resp.UserDetailResp;
import top.continew.admin.system.model.resp.UserImportParseResp;
import top.continew.admin.system.model.resp.UserImportResp;
import top.continew.admin.system.model.resp.UserResp;
import top.continew.admin.system.service.*;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.crud.service.CommonUserService;
import top.continew.starter.extension.crud.service.impl.BaseServiceImpl;
import top.continew.starter.web.util.FileUploadUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static top.continew.admin.system.enums.ImportPolicyEnum.*;
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

    private final PasswordEncoder passwordEncoder;
    private final UserPasswordHistoryService userPasswordHistoryService;
    private final OnlineUserService onlineUserService;
    private final OptionService optionService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;

    @Resource
    private DeptService deptService;
    @Value("${avatar.support-suffix}")
    private String[] avatarSupportSuffix;

    @Override
    public PageResp<UserResp> page(UserQuery query, PageQuery pageQuery) {
        QueryWrapper<UserDO> queryWrapper = this.buildQueryWrapper(query);
        super.sort(queryWrapper, pageQuery);
        IPage<UserDetailResp> page = baseMapper.selectUserPage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        PageResp<UserResp> pageResp = PageResp.build(page, super.getListClass());
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
    public void downloadImportUserTemplate(HttpServletResponse response) throws IOException {
        try {
            FileUploadUtils.download(response, ResourceUtil
                .getStream("templates/import/userImportTemplate.xlsx"), "用户导入模板.xlsx");
        } catch (Exception e) {
            log.error("下载用户导入模板失败：", e);
            response.setCharacterEncoding(CharsetUtil.UTF_8);
            response.setContentType(ContentType.JSON.toString());
            response.getWriter().write(JSONUtil.toJsonStr(R.fail("下载用户导入模板失败")));
        }
    }

    @Override
    public UserImportParseResp parseImportUser(MultipartFile file) {
        UserImportParseResp userImportResp = new UserImportParseResp();
        List<UserImportRowReq> userRowList;
        // 读取表格数据
        try {
            userRowList = EasyExcel.read(file.getInputStream())
                .head(UserImportRowReq.class)
                .sheet()
                .headRowNumber(1)
                .doReadSync();
        } catch (Exception e) {
            log.error("用户导入数据文件解析异常：", e);
            throw new BusinessException("数据文件解析异常");
        }
        userImportResp.setTotalRows(userRowList.size());
        if (CollUtil.isEmpty(userRowList)) {
            throw new BusinessException("数据文件格式错误");
        }

        // 过滤无效数据
        List<UserImportRowReq> validUserRowList = filterErrorUserImportData(userRowList);
        userImportResp.setValidRows(validUserRowList.size());
        if (CollUtil.isEmpty(validUserRowList)) {
            throw new BusinessException("数据文件格式错误");
        }

        // 检测表格内数据是否合法
        Set<String> seenEmails = new HashSet<>();
        boolean hasDuplicateEmail = validUserRowList.stream()
            .map(UserImportRowReq::getEmail)
            .anyMatch(email -> email != null && !seenEmails.add(email));
        CheckUtils.throwIf(hasDuplicateEmail, "存在重复邮箱，请检测数据");
        Set<String> seenPhones = new HashSet<>();
        boolean hasDuplicatePhone = validUserRowList.stream()
            .map(UserImportRowReq::getPhone)
            .anyMatch(phone -> phone != null && !seenPhones.add(phone));
        CheckUtils.throwIf(hasDuplicatePhone, "存在重复手机，请检测数据");

        // 校验是否存在错误角色
        List<String> roleNames = validUserRowList.stream().map(UserImportRowReq::getRoleName).distinct().toList();
        int existRoleCount = roleService.countByNames(roleNames);
        CheckUtils.throwIf(existRoleCount < roleNames.size(), "存在错误角色，请检查数据");
        // 校验是否存在错误部门
        List<String> deptNames = validUserRowList.stream().map(UserImportRowReq::getDeptName).distinct().toList();
        int existDeptCount = deptService.countByNames(deptNames);
        CheckUtils.throwIf(existDeptCount < deptNames.size(), "存在错误部门，请检查数据");

        // 查询重复用户
        userImportResp
            .setDuplicateUserRows(countExistByField(validUserRowList, UserImportRowReq::getUsername, UserDO::getUsername, false));
        // 查询重复邮箱
        userImportResp
            .setDuplicateEmailRows(countExistByField(validUserRowList, UserImportRowReq::getEmail, UserDO::getEmail, true));
        // 查询重复手机
        userImportResp
            .setDuplicatePhoneRows(countExistByField(validUserRowList, UserImportRowReq::getPhone, UserDO::getPhone, true));

        // 设置导入会话并缓存数据，有效期10分钟
        String importKey = UUID.fastUUID().toString(true);
        RedisUtils.set(CacheConstants.DATA_IMPORT_KEY + importKey, JSONUtil.toJsonStr(validUserRowList), Duration
            .ofMinutes(10));
        userImportResp.setImportKey(importKey);

        return userImportResp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserImportResp importUser(UserImportReq req) {
        // 校验导入会话是否过期
        List<UserImportRowReq> importUserList;
        try {
            String data = RedisUtils.get(CacheConstants.DATA_IMPORT_KEY + req.getImportKey());
            importUserList = JSONUtil.toList(data, UserImportRowReq.class);
            CheckUtils.throwIf(CollUtil.isEmpty(importUserList), "导入已过期，请重新上传");
        } catch (Exception e) {
            log.error("导入异常:", e);
            throw new BusinessException("导入已过期，请重新上传");
        }
        // 已存在数据查询
        List<String> existEmails = listExistByField(importUserList, UserImportRowReq::getEmail, UserDO::getEmail);
        List<String> existPhones = listExistByField(importUserList, UserImportRowReq::getPhone, UserDO::getPhone);
        List<UserDO> existUserList = listByUsernames(importUserList.stream()
            .map(UserImportRowReq::getUsername)
            .filter(Objects::nonNull)
            .toList());
        List<String> existUsernames = existUserList.stream().map(UserDO::getUsername).toList();
        CheckUtils
            .throwIf(isExitImportUser(req, importUserList, existUsernames, existEmails, existPhones), "数据不符合导入策略，已退出导入");

        // 基础数据准备
        Map<String, Long> userMap = existUserList.stream()
            .collect(Collectors.toMap(UserDO::getUsername, UserDO::getId));
        List<RoleDO> roleList = roleService.listByNames(importUserList.stream()
            .map(UserImportRowReq::getRoleName)
            .distinct()
            .toList());
        Map<String, Long> roleMap = roleList.stream().collect(Collectors.toMap(RoleDO::getName, RoleDO::getId));
        List<DeptDO> deptList = deptService.listByNames(importUserList.stream()
            .map(UserImportRowReq::getDeptName)
            .distinct()
            .toList());
        Map<String, Long> deptMap = deptList.stream().collect(Collectors.toMap(DeptDO::getName, DeptDO::getId));

        // 批量操作数据库集合
        List<UserDO> insertList = new ArrayList<>();
        List<UserDO> updateList = new ArrayList<>();
        List<UserRoleDO> userRoleDOList = new ArrayList<>();
        // ID生成器
        IdGenerator idGenerator = DefaultIdGeneratorProvider.INSTANCE.getShare();
        for (UserImportRowReq row : importUserList) {
            if (isSkipUserImport(req, row, existUsernames, existPhones, existEmails)) {
                // 按规则跳过该行
                continue;
            }
            UserDO userDO = BeanUtil.toBeanIgnoreError(row, UserDO.class);
            userDO.setStatus(req.getDefaultStatus());
            userDO.setPwdResetTime(LocalDateTime.now());
            userDO.setGender(EnumUtil.getBy(GenderEnum::getDescription, row.getGender(), GenderEnum.UNKNOWN));
            userDO.setDeptId(deptMap.get(row.getDeptName()));
            // 修改 or 新增
            if (UPDATE.validate(req.getDuplicateUser(), row.getUsername(), existUsernames)) {
                userDO.setId(userMap.get(row.getUsername()));
                updateList.add(userDO);
            } else {
                userDO.setId(idGenerator.generate());
                userDO.setIsSystem(false);
                insertList.add(userDO);
            }
            userRoleDOList.add(new UserRoleDO(userDO.getId(), roleMap.get(row.getRoleName())));
        }
        doImportUser(insertList, updateList, userRoleDOList);
        RedisUtils.delete(CacheConstants.DATA_IMPORT_KEY + req.getImportKey());
        return new UserImportResp(insertList.size() + updateList.size(), insertList.size(), updateList.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public void doImportUser(List<UserDO> insertList, List<UserDO> updateList, List<UserRoleDO> userRoleDOList) {
        if (CollUtil.isNotEmpty(insertList)) {
            baseMapper.insert(insertList);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
            userRoleService.deleteByUserIds(updateList.stream().map(UserDO::getId).toList());
        }
        if (CollUtil.isNotEmpty(userRoleDOList)) {
            userRoleService.saveBatch(userRoleDOList);
        }
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
        CheckUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(newStatus) && ObjectUtil.equal(id, UserContextHolder
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
        // 如果禁用用户，则踢出在线用户
        if (DisEnableStatusEnum.DISABLE.equals(newStatus)) {
            onlineUserService.kickOut(id);
            return;
        }
        // 如果角色有变更，则更新在线用户权限信息
        if (isSaveUserRoleSuccess) {
            UserContext userContext = UserContextHolder.getContext(id);
            if (null != userContext) {
                userContext.setRoles(roleService.listByUserId(id));
                userContext.setPermissions(roleService.listPermissionByUserId(id));
                UserContextHolder.setContext(userContext);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(key = "#ids", name = CacheConstants.USER_KEY_PREFIX, multi = true)
    public void delete(List<Long> ids) {
        CheckUtils.throwIf(CollUtil.contains(ids, UserContextHolder.getUserId()), "不允许删除当前用户");
        List<UserDO> list = baseMapper.lambdaQuery()
            .select(UserDO::getNickname, UserDO::getIsSystem)
            .in(UserDO::getId, ids)
            .list();
        Optional<UserDO> isSystemData = list.stream().filter(UserDO::getIsSystem).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选用户 [{}] 是系统内置用户，不允许删除", isSystemData.orElseGet(UserDO::new)
            .getNickname());
        // 删除用户和角色关联
        userRoleService.deleteByUserIds(ids);
        // 删除历史密码
        userPasswordHistoryService.deleteByUserIds(ids);
        // 删除用户
        super.delete(ids);
    }

    @Override
    public void resetPassword(UserPasswordResetReq req, Long id) {
        super.getById(id);
        baseMapper.lambdaUpdate()
            .set(UserDO::getPassword, req.getNewPassword())
            .set(UserDO::getPwdResetTime, LocalDateTime.now())
            .eq(UserDO::getId, id)
            .update();
    }

    @Override
    public void updateRole(UserRoleUpdateReq updateReq, Long id) {
        super.getById(id);
        // 保存用户和角色关联
        userRoleService.add(updateReq.getRoleIds(), id);
    }

    @Override
    public String updateAvatar(MultipartFile avatarFile, Long id) throws IOException {
        String avatarImageType = FileNameUtil.extName(avatarFile.getOriginalFilename());
        CheckUtils.throwIf(!StrUtil.equalsAnyIgnoreCase(avatarImageType, avatarSupportSuffix), "头像仅支持 {} 格式的图片", String
            .join(StringConstants.CHINESE_COMMA, avatarSupportSuffix));
        // 更新用户头像
        String base64 = ImgUtil.toBase64DataUri(ImgUtil.scale(ImgUtil.toImage(avatarFile
            .getBytes()), 100, 100, null), avatarImageType);
        baseMapper.lambdaUpdate().set(UserDO::getAvatar, base64).eq(UserDO::getId, id).update();
        return base64;
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
        int passwordRepetitionTimes = this.checkPassword(newPassword, user);
        // 更新密码和密码重置时间
        baseMapper.lambdaUpdate()
            .set(UserDO::getPassword, newPassword)
            .set(UserDO::getPwdResetTime, LocalDateTime.now())
            .eq(UserDO::getId, id)
            .update();
        // 保存历史密码
        userPasswordHistoryService.add(id, password, passwordRepetitionTimes);
        // 修改后登出
        StpUtil.logout();
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
    @Cached(key = "#id", name = CacheConstants.USER_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public String getNicknameById(Long id) {
        return baseMapper.selectNicknameById(id);
    }

    @Override
    protected <E> List<E> list(UserQuery query, SortQuery sortQuery, Class<E> targetClass) {
        QueryWrapper<UserDO> queryWrapper = this.buildQueryWrapper(query);
        // 设置排序
        super.sort(queryWrapper, sortQuery);
        List<UserDetailResp> entityList = baseMapper.selectUserList(queryWrapper);
        if (this.getEntityClass() == targetClass) {
            return (List<E>)entityList;
        }
        return BeanUtil.copyToList(entityList, targetClass);
    }

    @Override
    protected QueryWrapper<UserDO> buildQueryWrapper(UserQuery query) {
        String description = query.getDescription();
        DisEnableStatusEnum status = query.getStatus();
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
            .and(null != deptId && !SysConstants.SUPER_DEPT_ID.equals(deptId), q -> {
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
     * 判断是否跳过导入
     *
     * @param req            导入参数
     * @param row            导入数据
     * @param existUsernames 导入数据中已存在的用户名
     * @param existEmails    导入数据中已存在的邮箱
     * @param existPhones    导入数据中已存在的手机号
     * @return 是否跳过
     */
    private boolean isSkipUserImport(UserImportReq req,
                                     UserImportRowReq row,
                                     List<String> existUsernames,
                                     List<String> existPhones,
                                     List<String> existEmails) {
        return SKIP.validate(req.getDuplicateUser(), row.getUsername(), existUsernames) || SKIP.validate(req
            .getDuplicateEmail(), row.getEmail(), existEmails) || SKIP.validate(req.getDuplicatePhone(), row
                .getPhone(), existPhones);
    }

    /**
     * 判断是否退出导入
     *
     * @param req            导入参数
     * @param list           导入数据
     * @param existUsernames 导入数据中已存在的用户名
     * @param existEmails    导入数据中已存在的邮箱
     * @param existPhones    导入数据中已存在的手机号
     * @return 是否退出
     */
    private boolean isExitImportUser(UserImportReq req,
                                     List<UserImportRowReq> list,
                                     List<String> existUsernames,
                                     List<String> existEmails,
                                     List<String> existPhones) {
        return list.stream()
            .anyMatch(row -> EXIT.validate(req.getDuplicateUser(), row.getUsername(), existUsernames) || EXIT
                .validate(req.getDuplicateEmail(), row.getEmail(), existEmails) || EXIT.validate(req
                    .getDuplicatePhone(), row.getPhone(), existPhones));
    }

    /**
     * 按指定数据集获取数据库已存在的数量
     *
     * @param userRowList 导入的数据源
     * @param rowField    导入数据的字段
     * @param dbField     对比数据库的字段
     * @return 存在的数量
     */
    private int countExistByField(List<UserImportRowReq> userRowList,
                                  Function<UserImportRowReq, String> rowField,
                                  SFunction<UserDO, ?> dbField,
                                  boolean fieldEncrypt) {
        List<String> fieldValues = userRowList.stream().map(rowField).filter(Objects::nonNull).toList();
        if (fieldValues.isEmpty()) {
            return 0;
        }
        return (int)this.count(Wrappers.<UserDO>lambdaQuery()
            .in(dbField, fieldEncrypt ? SecureUtils.encryptFieldByAes(fieldValues) : fieldValues));
    }

    /**
     * 按指定数据集获取数据库已存在内容
     *
     * @param userRowList 导入的数据源
     * @param rowField    导入数据的字段
     * @param dbField     对比数据库的字段
     * @return 存在的内容
     */
    private List<String> listExistByField(List<UserImportRowReq> userRowList,
                                          Function<UserImportRowReq, String> rowField,
                                          SFunction<UserDO, String> dbField) {
        List<String> fieldValues = userRowList.stream().map(rowField).filter(Objects::nonNull).toList();
        if (fieldValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserDO> userDOList = baseMapper.selectList(Wrappers.<UserDO>lambdaQuery()
            .in(dbField, SecureUtils.encryptFieldByAes(fieldValues))
            .select(dbField));
        return userDOList.stream().map(dbField).filter(Objects::nonNull).toList();
    }

    /**
     * 过滤无效的导入用户数据,批量导入不严格校验数据
     */
    private List<UserImportRowReq> filterErrorUserImportData(List<UserImportRowReq> userImportList) {
        // 校验过滤
        List<UserImportRowReq> list = userImportList.stream()
            .filter(row -> ValidationUtil.validate(row).size() == 0)
            .toList();
        // 用户名去重
        return list.stream()
            .collect(Collectors.toMap(UserImportRowReq::getUsername, user -> user, (existing, replacement) -> existing))
            .values()
            .stream()
            .toList();
    }

    /**
     * 检测密码合法性
     *
     * @param password 密码
     * @param user     用户信息
     * @return 密码允许重复使用次数
     */
    private int checkPassword(String password, UserDO user) {
        Map<String, String> passwordPolicy = optionService.getByCategory(CATEGORY);
        // 密码最小长度
        PASSWORD_MIN_LENGTH.validate(password, MapUtil.getInt(passwordPolicy, PASSWORD_MIN_LENGTH.name()), user);
        // 密码是否必须包含特殊字符
        PASSWORD_REQUIRE_SYMBOLS.validate(password, MapUtil.getInt(passwordPolicy, PASSWORD_REQUIRE_SYMBOLS
            .name()), user);
        // 密码是否允许包含正反序账号名
        PASSWORD_ALLOW_CONTAIN_USERNAME.validate(password, MapUtil
            .getInt(passwordPolicy, PASSWORD_ALLOW_CONTAIN_USERNAME.name()), user);
        // 密码重复使用次数
        int passwordRepetitionTimes = MapUtil.getInt(passwordPolicy, PASSWORD_REPETITION_TIMES.name());
        PASSWORD_REPETITION_TIMES.validate(password, passwordRepetitionTimes, user);
        return passwordRepetitionTimes;
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

    private List<UserDO> listByUsernames(List<String> usernames) {
        return this.list(Wrappers.<UserDO>lambdaQuery()
            .in(UserDO::getUsername, usernames)
            .select(UserDO::getId, UserDO::getUsername));
    }
}
