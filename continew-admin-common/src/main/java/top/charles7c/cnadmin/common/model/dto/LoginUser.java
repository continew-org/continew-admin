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

package top.charles7c.cnadmin.common.model.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

import top.charles7c.cnadmin.common.enums.GenderEnum;

/**
 * 登录用户信息
 *
 * @author Charles7c
 * @since 2022/12/24 13:01
 */
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别（0未知 1男 2女）
     */
    private GenderEnum gender;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 描述
     */
    private String description;

    /**
     * 最后一次修改密码时间
     */
    private LocalDateTime pwdResetTime;

    /**
     * 部门 ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 令牌
     */
    private String token;

    /**
     * 登录 IP
     */
    private String clientIp;

    /**
     * 登录地点
     */
    private String location;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 权限码集合
     */
    private Set<String> permissions;

    /**
     * 角色编码集合
     */
    private Set<String> roles;
}
