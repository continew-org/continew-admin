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

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import top.charles7c.cnadmin.common.base.BaseService;
import top.charles7c.cnadmin.system.model.entity.UserDO;
import top.charles7c.cnadmin.system.model.query.UserQuery;
import top.charles7c.cnadmin.system.model.request.UpdateBasicInfoRequest;
import top.charles7c.cnadmin.system.model.request.UpdateUserRoleRequest;
import top.charles7c.cnadmin.system.model.request.UserRequest;
import top.charles7c.cnadmin.system.model.vo.UserDetailVO;
import top.charles7c.cnadmin.system.model.vo.UserVO;

/**
 * 用户业务接口
 *
 * @author Charles7c
 * @since 2022/12/21 21:48
 */
public interface UserService extends BaseService<UserVO, UserDetailVO, UserQuery, UserRequest> {

    /**
     * 上传头像
     *
     * @param avatar
     *            头像文件
     * @param id
     *            ID
     * @return 新头像路径
     */
    String uploadAvatar(MultipartFile avatar, Long id);

    /**
     * 修改基础信息
     *
     * @param request
     *            修改信息
     * @param id
     *            ID
     */
    void updateBasicInfo(UpdateBasicInfoRequest request, Long id);

    /**
     * 修改密码
     *
     * @param oldPassword
     *            当前密码
     * @param newPassword
     *            新密码
     * @param id
     *            ID
     */
    void updatePassword(String oldPassword, String newPassword, Long id);

    /**
     * 修改邮箱
     *
     * @param newEmail
     *            新邮箱
     * @param currentPassword
     *            当前密码
     * @param id
     *            ID
     */
    void updateEmail(String newEmail, String currentPassword, Long id);

    /**
     * 重置密码
     *
     * @param id
     *            ID
     */
    void resetPassword(Long id);

    /**
     * 修改角色
     *
     * @param request
     *            修改信息
     * @param id
     *            ID
     */
    void updateRole(UpdateUserRoleRequest request, Long id);

    /**
     * 根据用户名查询
     *
     * @param username
     *            用户名
     * @return 用户信息
     */
    UserDO getByUsername(String username);

    /**
     * 根据部门 ID 列表查询
     *
     * @param deptIds
     *            部门 ID 列表
     * @return 用户数量
     */
    Long countByDeptIds(List<Long> deptIds);
}
