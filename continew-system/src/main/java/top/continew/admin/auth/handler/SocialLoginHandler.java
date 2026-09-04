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

package top.continew.admin.auth.handler;

import top.continew.admin.common.constant.GlobalConstants;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.auth.AbstractLoginHandler;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.SocialLoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.GenderEnum;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.admin.system.enums.MessageTemplateEnum;
import top.continew.admin.system.enums.MessageTypeEnum;
import top.continew.admin.system.model.entity.DeptDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.model.entity.user.UserSocialDO;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.DeptService;
import top.continew.admin.system.service.MessageService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserSocialService;
import top.continew.starter.auth.justauth.AuthRequestFactory;
import top.continew.starter.core.autoconfigure.application.ApplicationProperties;
import top.continew.starter.core.util.validation.ValidationUtils;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 第三方账号登录处理器
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/25 14:21
 */
@Component
@RequiredArgsConstructor
public class SocialLoginHandler extends AbstractLoginHandler<SocialLoginReq> {

    private final AuthRequestFactory authRequestFactory;
    private final UserSocialService userSocialService;
    private final UserRoleService userRoleService;
    private final MessageService messageService;
    private final ApplicationProperties applicationProperties;
    private final DeptService deptService;

    @Override
    @Transactional
    public LoginResp login(SocialLoginReq req, ClientResp client, HttpServletRequest request,
        HttpServletResponse response) {
        // 获取第三方登录信息
        AuthRequest authRequest = authRequestFactory.getAuthRequest(req.getSource());
        AuthCallback callback = new AuthCallback();
        callback.setCode(req.getCode());
        callback.setState(req.getState());
        AuthResponse<AuthUser> authResponse = authRequest.login(callback);
        ValidationUtils.throwIf(!authResponse.ok(), authResponse.getMsg());
        AuthUser authUser = authResponse.getData();
        // 如未绑定则自动注册新用户，保存或更新关联信息
        String source = authUser.getSource();
        String openId = authUser.getUuid();
        UserSocialDO userSocial = userSocialService.getBySourceAndOpenId(source, openId);
        UserDO user;
        if (userSocial == null) {
            String username = authUser.getUsername();
            String nickname = authUser.getNickname();
            UserDO existsUser = userService.getByUsername(username);
            String randomStr = RandomUtil.randomString(RandomUtil.BASE_CHAR, 5);
            if (existsUser != null || !ReUtil.isMatch(RegexConstants.USERNAME, username)) {
                username = randomStr + IdUtil.fastSimpleUUID();
            }
            if (!ReUtil.isMatch(RegexConstants.GENERAL_NAME, nickname)) {
                nickname = source.toLowerCase() + randomStr;
            }
            user = new UserDO();
            user.setUsername(username);
            user.setNickname(nickname);
            if (authUser.getGender() != null) {
                user.setGender(GenderEnum.valueOf(authUser.getGender().name()));
            }
            user.setAvatar(authUser.getAvatar());
            // 默认设置为系统内置数据的根部门 如果需要设置其他部门自行替换查询条件
            DeptDO deptDO =
                deptService.getOne(new LambdaQueryWrapper<DeptDO>().eq(DeptDO::getIsSystem, true)
                    .eq(DeptDO::getParentId, 0));
            ValidationUtils.throwIf(deptDO == null, "未查询到系统内置部门");
            user.setDeptId(deptDO.getId());
            user.setStatus(DisEnableStatusEnum.ENABLE);
            userService.save(user);
            Long userId = user.getId();
            userRoleService.assignRolesToUser(Collections.singletonList(roleService
                .getIdByCode(RoleCodeEnum.GENERAL_USER.getCode())), userId);
            userSocial = new UserSocialDO();
            userSocial.setUserId(userId);
            userSocial.setSource(source);
            userSocial.setOpenId(openId);
            this.sendSecurityMsg(user);
        } else {
            user =
                BeanUtil.copyProperties(userService.getById(userSocial.getUserId()), UserDO.class);
        }
        // 检查用户状态
        super.checkUserStatus(user);
        userSocial.setMetaJson(JSONUtil.toJsonStr(authUser));
        userSocial.setLastLoginTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
        userSocialService.saveOrUpdate(userSocial);
        // 执行认证
        return super.authenticate(user, client, request, response);
    }

    @Override
    public void preLogin(SocialLoginReq req, ClientResp client, HttpServletRequest request) {
        super.preLogin(req, client, request);
        if (StpUtil.isLogin()) {
            refreshTokenService.revokeCurrent(StpUtil.getTokenValue(),
                refreshTokenService.resolve(req.getRefreshToken(), request));
            StpUtil.logout();
        }
    }

    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.SOCIAL;
    }

    /**
     * 发送安全消息
     *
     * @param user 用户信息
     */
    private void sendSecurityMsg(UserDO user) {
        MessageTemplateEnum template = MessageTemplateEnum.SOCIAL_REGISTER;
        MessageReq req = new MessageReq(MessageTypeEnum.SECURITY);
        req.setTitle(template.getTitle().formatted(applicationProperties.getName()));
        req.setContent(template.getContent().formatted(user.getNickname()));
        messageService.add(req, CollUtil.toList(user.getId().toString()));
    }
}
