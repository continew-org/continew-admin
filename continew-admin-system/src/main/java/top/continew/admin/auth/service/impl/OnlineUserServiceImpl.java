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

package top.continew.admin.auth.service.impl;

import cn.crane4j.annotation.AutoOperate;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.model.dto.LoginUser;
import top.continew.admin.common.util.helper.LoginHelper;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 在线用户业务实现
 *
 * @author Charles7c
 * @since 2023/3/25 22:49
 */
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    @Override
    @AutoOperate(type = OnlineUserResp.class, on = "list")
    public PageResp<OnlineUserResp> page(OnlineUserQuery query, PageQuery pageQuery) {
        List<LoginUser> loginUserList = this.list(query);
        List<OnlineUserResp> list = BeanUtil.copyToList(loginUserList, OnlineUserResp.class);
        return PageResp.build(pageQuery.getPage(), pageQuery.getSize(), list);
    }

    @Override
    public List<LoginUser> list(OnlineUserQuery query) {
        List<LoginUser> loginUserList = new ArrayList<>();
        // 查询所有登录用户
        List<String> tokenKeyList = StpUtil.searchTokenValue(StringConstants.EMPTY, 0, -1, false);
        tokenKeyList.parallelStream().forEach(tokenKey -> {
            String token = StrUtil.subAfter(tokenKey, StringConstants.COLON, true);
            // 忽略已过期或失效 Token
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < SaTokenDao.NEVER_EXPIRE) {
                return;
            }
            // 检查是否符合查询条件
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (this.isMatchQuery(query, loginUser)) {
                loginUserList.add(loginUser);
            }
        });
        // 设置排序
        CollUtil.sort(loginUserList, Comparator.comparing(LoginUser::getLoginTime).reversed());
        return loginUserList;
    }

    @Override
    public LocalDateTime getLastActiveTime(String token) {
        long lastActiveTime = StpUtil.getStpLogic().getTokenLastActiveTime(token);
        return lastActiveTime == SaTokenDao.NOT_VALUE_EXPIRE ? null : DateUtil.date(lastActiveTime).toLocalDateTime();
    }

    @Override
    public void kickOut(Long userId) {
        if (!StpUtil.isLogin(userId)) {
            return;
        }
        StpUtil.logout(userId);
    }

    /**
     * 是否符合查询条件
     *
     * @param query     查询条件
     * @param loginUser 登录用户信息
     * @return 是否符合查询条件
     */
    private boolean isMatchQuery(OnlineUserQuery query, LoginUser loginUser) {
        boolean flag1 = true;
        String nickname = query.getNickname();
        if (StrUtil.isNotBlank(nickname)) {
            flag1 = StrUtil.contains(loginUser.getUsername(), nickname) || StrUtil.contains(LoginHelper
                .getNickname(loginUser.getId()), nickname);
        }
        boolean flag2 = true;
        List<Date> loginTime = query.getLoginTime();
        if (CollUtil.isNotEmpty(loginTime)) {
            flag2 = DateUtil.isIn(DateUtil.date(loginUser.getLoginTime()).toJdkDate(), loginTime.get(0), loginTime
                .get(1));
        }
        boolean flag3 = true;
        Long userId = query.getUserId();
        if (null != userId) {
            flag3 = userId.equals(loginUser.getId());
        }
        boolean flag4 = true;
        Long roleId = query.getRoleId();
        if (null != roleId) {
            flag4 = loginUser.getRoles().stream().anyMatch(r -> r.getId().equals(roleId));
        }
        return flag1 && flag2 && flag3 && flag4;
    }
}
