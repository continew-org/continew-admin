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

package top.charles7c.cnadmin.auth.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.auth.model.query.OnlineUserQuery;
import top.charles7c.cnadmin.auth.model.vo.OnlineUserVO;
import top.charles7c.cnadmin.auth.service.OnlineUserService;
import top.charles7c.cnadmin.common.constant.StringConsts;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;

/**
 * 在线用户业务实现类
 *
 * @author Charles7c
 * @author Lion Li（RuoYi-Vue-Plus）
 * @since 2023/3/25 22:49
 */
@Service
public class OnlineUserServiceImpl implements OnlineUserService {

    @Override
    public PageDataVO<OnlineUserVO> page(OnlineUserQuery query, PageQuery pageQuery) {
        List<LoginUser> loginUserList = this.list(query);
        List<OnlineUserVO> list = BeanUtil.copyToList(loginUserList, OnlineUserVO.class);
        return PageDataVO.build(pageQuery.getPage(), pageQuery.getSize(), list);
    }

    @Override
    public List<LoginUser> list(OnlineUserQuery query) {
        List<LoginUser> loginUserList = new ArrayList<>();
        // 查询所有登录用户
        List<String> tokenKeyList = StpUtil.searchTokenValue(StringConsts.EMPTY, 0, -1, false);
        for (String tokenKey : tokenKeyList) {
            String token = StrUtil.subAfter(tokenKey, StringConsts.COLON, true);
            // 忽略已过期或失效 Token
            if (StpUtil.stpLogic.getTokenActivityTimeoutByToken(token) < SaTokenDao.NEVER_EXPIRE) {
                continue;
            }
            // 检查是否符合查询条件
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (this.checkQuery(query, loginUser)) {
                loginUserList.add(loginUser);
            }
        }
        // 设置排序
        CollUtil.sort(loginUserList, Comparator.comparing(LoginUser::getLoginTime).reversed());
        return loginUserList;
    }

    @Override
    public void cleanByRoleId(Long roleId) {
        List<LoginUser> loginUserList = this.list(new OnlineUserQuery());
        loginUserList.parallelStream().forEach(u -> {
            if (u.getRoleSet().stream().anyMatch(r -> r.getId().equals(roleId))) {
                try {
                    StpUtil.logoutByTokenValue(u.getToken());
                } catch (NotLoginException ignored) {
                }
            }
        });
    }

    /**
     * 检查是否符合查询条件
     *
     * @param query
     *            查询条件
     * @param loginUser
     *            登录用户信息
     * @return 是否符合查询条件
     */
    private boolean checkQuery(OnlineUserQuery query, LoginUser loginUser) {
        boolean flag1 = true;
        String nickname = query.getNickname();
        if (StrUtil.isNotBlank(nickname)) {
            flag1 = StrUtil.contains(loginUser.getUsername(), nickname)
                || StrUtil.contains(LoginHelper.getNickname(loginUser.getId()), nickname);
        }

        boolean flag2 = true;
        List<Date> loginTime = query.getLoginTime();
        if (CollUtil.isNotEmpty(loginTime)) {
            flag2 =
                DateUtil.isIn(DateUtil.date(loginUser.getLoginTime()).toJdkDate(), loginTime.get(0), loginTime.get(1));
        }
        return flag1 && flag2;
    }
}
