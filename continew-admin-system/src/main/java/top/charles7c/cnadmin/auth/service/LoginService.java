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

package top.charles7c.cnadmin.auth.service;

import java.util.List;

import top.charles7c.cnadmin.auth.model.vo.RouteVO;

import me.zhyd.oauth.model.AuthUser;

/**
 * 登录业务接口
 *
 * @author Charles7c
 * @since 2022/12/21 21:48
 */
public interface LoginService {

    /**
     * 用户登录
     *
     * @param username
     *            用户名
     * @param password
     *            密码
     * @return 令牌
     */
    String login(String username, String password);

    /**
     * 社交身份登录
     *
     * @param authUser
     *            社交身份信息
     * @return 令牌
     */
    String socialLogin(AuthUser authUser);

    /**
     * 构建路由树
     *
     * @param userId
     *            用户 ID
     * @return 路由树
     */
    List<RouteVO> buildRouteTree(Long userId);
}
