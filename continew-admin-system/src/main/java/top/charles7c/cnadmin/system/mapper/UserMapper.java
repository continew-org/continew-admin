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

package top.charles7c.cnadmin.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;

import top.charles7c.cnadmin.common.annotation.DataPermission;
import top.charles7c.cnadmin.common.base.BaseMapper;
import top.charles7c.cnadmin.system.model.entity.UserDO;

/**
 * 用户 Mapper
 *
 * @author Charles7c
 * @since 2022/12/22 21:47
 */
public interface UserMapper extends BaseMapper<UserDO> {

    @Override
    @DataPermission
    List<UserDO> selectList(@Param(Constants.WRAPPER) Wrapper<UserDO> queryWrapper);

    @Override
    @DataPermission
    <P extends IPage<UserDO>> P selectPage(P page, @Param(Constants.WRAPPER) Wrapper<UserDO> queryWrapper);

    /**
     * 根据用户名查询
     *
     * @param username
     *            用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM `sys_user` WHERE `username` = #{username}")
    UserDO selectByUsername(@Param("username") String username);
}
