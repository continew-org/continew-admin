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

package top.charles7c.cnadmin.system.service;

import org.springframework.web.multipart.MultipartFile;

import top.charles7c.cnadmin.system.model.entity.UserDO;

/**
 * 用户业务接口
 *
 * @author Charles7c
 * @since 2022/12/21 21:48
 */
public interface UserService {

    /**
     * 根据用户名查询
     *
     * @param username
     *            用户名
     * @return 用户信息
     */
    UserDO getByUsername(String username);

    /**
     * 上传头像
     *
     * @param avatar
     *            头像文件
     * @param userId
     *            用户 ID
     * @return 新头像路径
     */
    String uploadAvatar(MultipartFile avatar, Long userId);

    /**
     * 修改信息
     *
     * @param user
     *            用户信息
     */
    void update(UserDO user);

    /**
     * 修改密码
     *
     * @param oldPassword
     *            当前密码
     * @param newPassword
     *            新密码
     * @param userId
     *            用户 ID
     */
    void updatePassword(String oldPassword, String newPassword, Long userId);

    /**
     * 修改邮箱
     *
     * @param newEmail
     *            新邮箱
     * @param currentPassword
     *            当前密码
     * @param userId
     *            用户ID
     */
    void updateEmail(String newEmail, String currentPassword, Long userId);

    /**
     * 根据 ID 查询
     *
     * @param userId
     *            用户ID
     * @return 用户信息
     */
    UserDO getById(Long userId);
}
