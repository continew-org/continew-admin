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

package top.continew.admin.common.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.continew.admin.common.config.TenantExtensionProperties;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.common.enums.RoleCodeEnum;
import top.continew.starter.core.util.CollUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户上下文
 *
 * @author Charles7c
 * @since 3.4.0
 */
@Data
@NoArgsConstructor
public class UserContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 一天的分钟数（把剩余分钟向上取整为天）
     */
    private static final int MINUTES_PER_DAY = 24 * 60;

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
     * 部门 ID
     */
    private Long deptId;

    /**
     * 最后一次修改密码时间
     */
    private LocalDateTime pwdResetTime;

    /**
     * 登录时系统设置的密码过期天数
     */
    private Integer passwordExpirationDays;

    /**
     * 登录时系统设置的密码到期提醒天数
     */
    private Integer passwordExpirationWarningDays;

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
    private Set<RoleContext> roles;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * 客户端 ID
     */
    private String clientId;

    /**
     * 租户 ID
     */
    private Long tenantId;

    public UserContext(Set<String> permissions, Set<RoleContext> roles,
        Integer passwordExpirationDays) {
        this.permissions = permissions;
        this.setRoles(roles);
        this.passwordExpirationDays = passwordExpirationDays;
    }

    /**
     * 设置角色
     *
     * @param roles 角色
     */
    public void setRoles(Set<RoleContext> roles) {
        this.roles = roles;
        this.roleCodes = CollUtils.mapToSet(roles, RoleContext::getCode);
    }

    /**
     * 密码过期时间
     *
     * @return 密码过期时间（策略关闭或未设置密码时为 null）
     */
    private LocalDateTime pwdExpireTime() {
        // 永久有效
        if (this.passwordExpirationDays == null
            || this.passwordExpirationDays <= GlobalConstants.Boolean.NO) {
            return null;
        }
        // 未设置密码（第三方登录用户）视为永不过期，不做提示
        if (this.pwdResetTime == null) {
            return null;
        }
        return this.pwdResetTime.plusDays(this.passwordExpirationDays);
    }

    /**
     * 密码是否已过期
     *
     * @return 是否过期
     */
    public boolean isPasswordExpired() {
        LocalDateTime expireTime = this.pwdExpireTime();
        return expireTime != null
            && expireTime.isBefore(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
    }

    /**
     * 距密码过期的剩余天数
     *
     * @return 策略关闭或未设置密码时为 null；已过期为 0；否则为剩余天数（向上取整）
     */
    public Integer getPwdExpireDays() {
        LocalDateTime expireTime = this.pwdExpireTime();
        if (expireTime == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID);
        // 与 isPasswordExpired() 使用同一判据，避免出现「未过期但剩余 0 天」的自相矛盾
        if (expireTime.isBefore(now)) {
            return 0;
        }
        long minutes = Duration.between(now, expireTime).toMinutes();
        return Math.max(1, (int) Math.ceil(minutes / (double) MINUTES_PER_DAY));
    }

    /**
     * 是否为超级管理员
     *
     * @return true：是；false：否
     */
    public boolean isSuperAdmin() {
        if (CollUtil.isEmpty(roleCodes)) {
            return false;
        }
        return roleCodes.contains(RoleCodeEnum.SUPER_ADMIN.getCode());
    }

    /**
     * 是否为租户管理员
     *
     * @return true：是；false：否
     */
    public boolean isTenantAdmin() {
        if (CollUtil.isEmpty(roleCodes)) {
            return false;
        }
        TenantExtensionProperties tenantExtensionProperties =
            SpringUtil.getBean(TenantExtensionProperties.class);
        return !tenantExtensionProperties.isDefaultTenant()
            && roleCodes.contains(RoleCodeEnum.TENANT_ADMIN.getCode());
    }
}
