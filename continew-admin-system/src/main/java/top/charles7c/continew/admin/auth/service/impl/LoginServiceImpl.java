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

package top.charles7c.continew.admin.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.charles7c.continew.admin.auth.model.resp.RouteResp;
import top.charles7c.continew.admin.auth.service.LoginService;
import top.charles7c.continew.admin.auth.service.PermissionService;
import top.charles7c.continew.admin.common.constant.RegexConstants;
import top.charles7c.continew.admin.common.constant.SysConstants;
import top.charles7c.continew.admin.common.enums.DisEnableStatusEnum;
import top.charles7c.continew.admin.common.enums.GenderEnum;
import top.charles7c.continew.admin.common.enums.MenuTypeEnum;
import top.charles7c.continew.admin.common.enums.MessageTypeEnum;
import top.charles7c.continew.admin.common.model.dto.LoginUser;
import top.charles7c.continew.admin.common.util.helper.LoginHelper;
import top.charles7c.continew.admin.system.enums.MessageTemplateEnum;
import top.charles7c.continew.admin.system.model.entity.DeptDO;
import top.charles7c.continew.admin.system.model.entity.RoleDO;
import top.charles7c.continew.admin.system.model.entity.UserDO;
import top.charles7c.continew.admin.system.model.entity.UserSocialDO;
import top.charles7c.continew.admin.system.model.req.MessageReq;
import top.charles7c.continew.admin.system.model.resp.MenuResp;
import top.charles7c.continew.admin.system.service.*;
import top.continew.starter.core.autoconfigure.project.ProjectProperties;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.extension.crud.annotation.TreeField;
import top.continew.starter.extension.crud.util.TreeUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 登录业务实现
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final ProjectProperties projectProperties;
    private final UserService userService;
    private final DeptService deptService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PermissionService permissionService;
    private final UserRoleService userRoleService;
    private final UserSocialService userSocialService;
    private final MessageService messageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String accountLogin(String username, String password) {
        UserDO user = userService.getByUsername(username);
        CheckUtils.throwIfNull(user, "用户名或密码不正确");
        CheckUtils.throwIf(!passwordEncoder.matches(password, user.getPassword()), "用户名或密码不正确");
        this.checkUserStatus(user);
        return this.login(user);
    }

    @Override
    public String phoneLogin(String phone) {
        UserDO user = userService.getByPhone(phone);
        CheckUtils.throwIfNull(user, "此手机号未绑定本系统账号");
        this.checkUserStatus(user);
        return this.login(user);
    }

    @Override
    public String emailLogin(String email) {
        UserDO user = userService.getByEmail(email);
        CheckUtils.throwIfNull(user, "此邮箱未绑定本系统账号");
        this.checkUserStatus(user);
        return this.login(user);
    }

    @Override
    public String socialLogin(AuthUser authUser) {
        String source = authUser.getSource();
        String openId = authUser.getUuid();
        UserSocialDO userSocial = userSocialService.getBySourceAndOpenId(source, openId);
        UserDO user;
        if (null == userSocial) {
            String username = authUser.getUsername();
            String nickname = authUser.getNickname();
            UserDO existsUser = userService.getByUsername(username);
            String randomStr = RandomUtil.randomString(RandomUtil.BASE_CHAR, 5);
            if (null != existsUser || !ReUtil.isMatch(RegexConstants.USERNAME, username)) {
                username = randomStr + IdUtil.fastSimpleUUID();
            }
            if (!ReUtil.isMatch(RegexConstants.GENERAL_NAME, nickname)) {
                nickname = source.toLowerCase() + randomStr;
            }
            user = new UserDO();
            user.setUsername(username);
            user.setNickname(nickname);
            user.setGender(GenderEnum.valueOf(authUser.getGender().name()));
            user.setAvatar(authUser.getAvatar());
            user.setDeptId(SysConstants.SUPER_DEPT_ID);
            Long userId = userService.add(user);
            RoleDO role = roleService.getByCode(SysConstants.ADMIN_ROLE_CODE);
            userRoleService.add(Collections.singletonList(role.getId()), userId);
            userSocial = new UserSocialDO();
            userSocial.setUserId(userId);
            userSocial.setSource(source);
            userSocial.setOpenId(openId);
            this.sendSystemMsg(user);
        } else {
            user = BeanUtil.copyProperties(userService.getById(userSocial.getUserId()), UserDO.class);
        }
        this.checkUserStatus(user);
        userSocial.setMetaJson(JSONUtil.toJsonStr(authUser));
        userSocial.setLastLoginTime(LocalDateTime.now());
        userSocialService.saveOrUpdate(userSocial);
        return this.login(user);
    }

    @Override
    public List<RouteResp> buildRouteTree(Long userId) {
        Set<String> roleCodeSet = permissionService.listRoleCodeByUserId(userId);
        if (CollUtil.isEmpty(roleCodeSet)) {
            return new ArrayList<>(0);
        }
        // 查询菜单列表
        Set<MenuResp> menuSet = new LinkedHashSet<>();
        if (roleCodeSet.contains(SysConstants.ADMIN_ROLE_CODE)) {
            menuSet.addAll(menuService.listAll());
        } else {
            roleCodeSet.forEach(roleCode -> menuSet.addAll(menuService.listByRoleCode(roleCode)));
        }
        List<MenuResp> menuList = menuSet.stream().filter(m -> !MenuTypeEnum.BUTTON.equals(m.getType())).toList();
        // 构建路由树
        TreeField treeField = MenuResp.class.getDeclaredAnnotation(TreeField.class);
        TreeNodeConfig treeNodeConfig = TreeUtils.genTreeNodeConfig(treeField);
        List<Tree<Long>> treeList = TreeUtils.build(menuList, treeNodeConfig, (m, tree) -> {
            tree.setId(m.getId());
            tree.setParentId(m.getParentId());
            tree.setName(m.getTitle());
            tree.setWeight(m.getSort());
            tree.putExtra("type", m.getType().getValue());
            tree.putExtra("path", m.getPath());
            tree.putExtra("name", m.getName());
            tree.putExtra("component", m.getComponent());
            tree.putExtra("redirect", m.getRedirect());
            tree.putExtra("icon", m.getIcon());
            tree.putExtra("isExternal", m.getIsExternal());
            tree.putExtra("isCache", m.getIsCache());
            tree.putExtra("isHidden", m.getIsHidden());
            tree.putExtra("permission", m.getPermission());
        });
        return BeanUtil.copyToList(treeList, RouteResp.class);
    }

    /**
     * 登录并缓存用户信息
     *
     * @param user 用户信息
     * @return 令牌
     */
    private String login(UserDO user) {
        Long userId = user.getId();
        LoginUser loginUser = BeanUtil.copyProperties(user, LoginUser.class);
        loginUser.setPermissions(permissionService.listPermissionByUserId(userId));
        loginUser.setRoleCodes(permissionService.listRoleCodeByUserId(userId));
        loginUser.setRoles(roleService.listByUserId(userId));
        return LoginHelper.login(loginUser);
    }

    /**
     * 检查用户状态
     *
     * @param user 用户信息
     */
    private void checkUserStatus(UserDO user) {
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, user.getStatus(), "此账号已被禁用，如有疑问，请联系管理员");
        DeptDO dept = deptService.getById(user.getDeptId());
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, dept.getStatus(), "此账号所属部门已被禁用，如有疑问，请联系管理员");
    }

    /**
     * 发送系统消息
     * 
     * @param user 用户信息
     */
    private void sendSystemMsg(UserDO user) {
        MessageReq req = new MessageReq();
        MessageTemplateEnum socialRegister = MessageTemplateEnum.SOCIAL_REGISTER;
        req.setTitle(StrUtil.format(socialRegister.getTitle(), projectProperties.getName()));
        req.setContent(StrUtil.format(socialRegister.getContent(), user.getNickname()));
        req.setType(MessageTypeEnum.SYSTEM);
        messageService.add(req, CollUtil.toList(user.getId()));
    }
}
