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

package top.continew.admin.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.continew.admin.common.config.mybatis.DataPermissionMapper;
import top.continew.admin.system.model.entity.UserDO;
import top.continew.admin.system.model.resp.UserDetailResp;
import top.continew.starter.data.mybatis.plus.datapermission.DataPermission;
import top.continew.starter.security.crypto.annotation.FieldEncrypt;

import java.util.List;

/**
 * 用户 Mapper
 *
 * @author Charles7c
 * @since 2022/12/22 21:47
 */
public interface UserMapper extends DataPermissionMapper<UserDO> {

    /**
     * 分页查询列表
     *
     * @param page         分页条件
     * @param queryWrapper 查询条件
     * @return 分页列表信息
     */
    @DataPermission("t1")
    IPage<UserDetailResp> selectUserPage(@Param("page") IPage<UserDO> page,
                                         @Param(Constants.WRAPPER) QueryWrapper<UserDO> queryWrapper);

    /**
     * 查询列表
     *
     * @param queryWrapper 查询条件
     * @return 列表信息
     */
    @DataPermission("t1")
    List<UserDetailResp> selectUserList(@Param(Constants.WRAPPER) QueryWrapper<UserDO> queryWrapper);

    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    UserDO selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone}")
    UserDO selectByPhone(@FieldEncrypt @Param("phone") String phone);

    /**
     * 根据邮箱查询
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email}")
    UserDO selectByEmail(@FieldEncrypt @Param("email") String email);

    /**
     * 根据 ID 查询昵称
     *
     * @param id ID
     * @return 昵称
     */
    @Select("SELECT nickname FROM sys_user WHERE id = #{id}")
    String selectNicknameById(@Param("id") Long id);

    /**
     * 根据邮箱查询数量
     *
     * @param email 邮箱
     * @return 用户数量
     */
    Long selectCountByEmail(@FieldEncrypt @Param("email") String email, @Param("id") Long id);

    /**
     * 根据手机号查询数量
     *
     * @param phone 手机号
     * @return 用户数量
     */
    Long selectCountByPhone(@FieldEncrypt @Param("phone") String phone, @Param("id") Long id);

    /**
     * 修改手机号
     *
     * @param phone 手机号
     * @param id    ID
     * @return 影响行数
     */
    @Select("UPDATE sys_user SET phone = #{phone} WHERE id = #{id}")
    Integer updatePhone(@FieldEncrypt @Param("phone") String phone, @Param("id") Long id);

    /**
     * 修改邮箱
     *
     * @param email 邮箱
     * @param id    ID
     * @return 影响行数
     */
    @Select("UPDATE sys_user SET email = #{email} WHERE id = #{id}")
    Integer updateEmail(@FieldEncrypt @Param("email") String email, @Param("id") Long id);
}
