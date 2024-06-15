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

package top.continew.admin.common.model.dto;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.continew.admin.common.constant.SysConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 登录用户信息
 *
 * @author Charles7c
 * @since 2022/12/24 13:01
 */
@Data
@NoArgsConstructor
public class LoginUser implements Serializable {

    @Serial
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
     * 部门 ID
     */
    private Long deptId;

    /**
     * 权限码集合
     */
    private Set<String> permissions;

    /**
     * 角色编码集合
     */
    private Set<String> roleCodes;

    /**
     * 角色集合
     */
    private Set<RoleDTO> roles;

    /**
     * 令牌
     */
    private String token;

    /**
     * IP
     */
    private String ip;

    /**
     * IP 归属地
     */
    private String address;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 最后一次修改密码时间
     */
    private LocalDateTime pwdResetTime;

    /**
     * 登录时系统设置的密码过期天数
     */
    private Integer passwordExpirationDays;

    public LoginUser(Set<String> permissions,
                     Set<String> roleCodes,
                     Set<RoleDTO> roles,
                     Integer passwordExpirationDays) {
        this.permissions = permissions;
        this.roleCodes = roleCodes;
        this.roles = roles;
        this.passwordExpirationDays = passwordExpirationDays;
    }

    /**
     * 是否为管理员
     *
     * @return true：是；false：否
     */
    public boolean isAdmin() {
        if (CollUtil.isEmpty(roleCodes)) {
            return false;
        }
        return roleCodes.contains(SysConstants.ADMIN_ROLE_CODE);
    }

    /**
     * 密码是否已过期
     *
     * @return 是否过期
     */
    public boolean isPasswordExpired() {
        // 永久有效
        if (this.passwordExpirationDays == null || this.passwordExpirationDays <= SysConstants.NO) {
            return false;
        }
        // 初始密码（第三方登录用户）暂不提示修改
        if (this.pwdResetTime == null) {
            return false;
        }
        return this.pwdResetTime.plusDays(this.passwordExpirationDays).isBefore(LocalDateTime.now());
    }
}
