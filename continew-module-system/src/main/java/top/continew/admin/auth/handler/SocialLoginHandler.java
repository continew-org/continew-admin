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

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.xkcoding.justauth.AuthRequestFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Component;
import top.continew.admin.auth.AbstractLoginHandler;
import top.continew.admin.auth.enums.AuthTypeEnum;
import top.continew.admin.auth.model.req.SocialLoginReq;
import top.continew.admin.auth.model.resp.LoginResp;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.GenderEnum;
import top.continew.admin.system.enums.MessageTemplateEnum;
import top.continew.admin.system.enums.MessageTypeEnum;
import top.continew.admin.system.model.entity.RoleDO;
import top.continew.admin.system.model.entity.UserDO;
import top.continew.admin.system.model.entity.UserSocialDO;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.resp.ClientResp;
import top.continew.admin.system.service.MessageService;
import top.continew.admin.system.service.UserRoleService;
import top.continew.admin.system.service.UserSocialService;
import top.continew.starter.core.autoconfigure.project.ProjectProperties;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.messaging.websocket.util.WebSocketUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
    private final ProjectProperties projectProperties;

    @Override
    public LoginResp login(SocialLoginReq req, ClientResp client, HttpServletRequest request) {
        // 获取第三方登录信息
        AuthRequest authRequest = this.getAuthRequest(req.getSource());
        AuthCallback callback = new AuthCallback();
        callback.setCode(req.getCode());
        callback.setState(req.getState());
        AuthResponse<AuthUser> response = authRequest.login(callback);
        ValidationUtils.throwIf(!response.ok(), response.getMsg());
        AuthUser authUser = response.getData();
        // 如未绑定则自动注册新用户，保存或更新关联信息
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
            user.setStatus(DisEnableStatusEnum.ENABLE);
            userService.save(user);
            Long userId = user.getId();
            RoleDO role = roleService.getByCode(SysConstants.SUPER_ROLE_CODE);
            userRoleService.assignRolesToUser(Collections.singletonList(role.getId()), userId);
            userSocial = new UserSocialDO();
            userSocial.setUserId(userId);
            userSocial.setSource(source);
            userSocial.setOpenId(openId);
            this.sendSecurityMsg(user);
        } else {
            user = BeanUtil.copyProperties(userService.getById(userSocial.getUserId()), UserDO.class);
        }
        // 检查用户状态
        super.checkUserStatus(user);
        userSocial.setMetaJson(JSONUtil.toJsonStr(authUser));
        userSocial.setLastLoginTime(LocalDateTime.now());
        userSocialService.saveOrUpdate(userSocial);
        // 执行认证
        String token = super.authenticate(user, client);
        return LoginResp.builder().token(token).build();
    }

    @Override
    public void preLogin(SocialLoginReq req, ClientResp client, HttpServletRequest request) {
        super.preLogin(req, client, request);
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.SOCIAL;
    }

    /**
     * 获取 AuthRequest
     *
     * @param source 平台名称
     * @return AuthRequest
     */
    private AuthRequest getAuthRequest(String source) {
        try {
            return authRequestFactory.get(source);
        } catch (Exception e) {
            throw new BadRequestException("暂不支持 [%s] 平台账号登录".formatted(source));
        }
    }

    /**
     * 发送安全消息
     *
     * @param user 用户信息
     */
    private void sendSecurityMsg(UserDO user) {
        MessageReq req = new MessageReq();
        MessageTemplateEnum socialRegister = MessageTemplateEnum.SOCIAL_REGISTER;
        req.setTitle(socialRegister.getTitle().formatted(projectProperties.getName()));
        req.setContent(socialRegister.getContent().formatted(user.getNickname()));
        req.setType(MessageTypeEnum.SECURITY);
        messageService.add(req, CollUtil.toList(user.getId()));
        List<String> tokenList = StpUtil.getTokenValueListByLoginId(user.getId());
        for (String token : tokenList) {
            WebSocketUtils.sendMessage(token, "1");
        }
    }
}
