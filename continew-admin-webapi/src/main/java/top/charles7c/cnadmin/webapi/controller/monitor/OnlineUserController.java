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

package top.charles7c.cnadmin.webapi.controller.monitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.consts.CacheConstants;
import top.charles7c.cnadmin.common.model.dto.LoginUser;
import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;
import top.charles7c.cnadmin.monitor.model.query.OnlineUserQuery;
import top.charles7c.cnadmin.monitor.model.vo.*;

/**
 * 在线用户 API
 *
 * @author Charles7c
 * @since 2023/1/20 21:51
 */
@Tag(name = "在线用户 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/online/user")
public class OnlineUserController {

    @Operation(summary = "分页查询在线用户列表")
    @GetMapping
    public R<PageDataVO<OnlineUserVO>> list(@Validated OnlineUserQuery query, @Validated PageQuery pageQuery) {
        List<LoginUser> loginUserList = new ArrayList<>();
        List<String> tokenKeyList = StpUtil.searchTokenValue("", 0, -1, false);
        for (String tokenKey : tokenKeyList) {
            String token = StrUtil.subAfter(tokenKey, ":", true);
            // 忽略已过期或失效 Token
            if (StpUtil.stpLogic.getTokenActivityTimeoutByToken(token) < SaTokenDao.NEVER_EXPIRE) {
                continue;
            }

            // 获取 Token Session
            SaSession saSession = StpUtil.getTokenSessionByToken(token);
            LoginUser loginUser = saSession.get(CacheConstants.LOGIN_USER_CACHE_KEY, new LoginUser());

            // 检查是否符合查询条件
            if (Boolean.TRUE.equals(checkQuery(query, loginUser))) {
                loginUserList.add(loginUser);
            }
        }

        // 构建分页数据
        List<OnlineUserVO> list = BeanUtil.copyToList(loginUserList, OnlineUserVO.class);
        CollUtil.sort(list, Comparator.comparing(OnlineUserVO::getLoginTime).reversed());
        PageDataVO<OnlineUserVO> pageDataVO = PageDataVO.build(pageQuery.getPage(), pageQuery.getSize(), list);
        return R.ok(pageDataVO);
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
            flag1 = loginUser.getUsername().contains(nickname) || loginUser.getNickname().contains(nickname);
        }

        boolean flag2 = true;
        List<Date> loginTime = query.getLoginTime();
        if (CollUtil.isNotEmpty(loginTime)) {
            flag2 =
                DateUtil.isIn(DateUtil.date(loginUser.getLoginTime()).toJdkDate(), loginTime.get(0), loginTime.get(1));
        }
        return flag1 && flag2;
    }

    @Operation(summary = "强退在线用户")
    @DeleteMapping("/{token}")
    public R kickout(@PathVariable String token) {
        String currentToken = StpUtil.getTokenValue();
        CheckUtils.throwIfEqual(token, currentToken, "不能强退当前登录");

        StpUtil.kickoutByTokenValue(token);
        return R.ok("强退成功");
    }
}
