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

package top.charles7c.cnadmin.auth.model.vo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.hutool.core.util.DesensitizedUtil;

import top.charles7c.cnadmin.common.enums.GenderEnum;

/**
 * 用户信息
 *
 * @author Charles7c
 * @since 2022/12/29 20:15
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户信息")
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 性别（0未知 1男 2女）
     */
    @Schema(description = "性别（0未知 1男 2女）", type = "Integer", allowableValues = {"0", "1", "2"})
    private GenderEnum gender;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址")
    private String avatar;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 最后一次修改密码时间
     */
    @Schema(description = "最后一次修改密码时间")
    private LocalDateTime pwdResetTime;

    /**
     * 创建时间
     */
    @JsonIgnore
    private LocalDateTime createTime;

    /**
     * 注册日期
     */
    @Schema(description = "注册日期")
    private LocalDate registrationDate;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 所属部门
     */
    @Schema(description = "所属名称")
    private String deptName;

    /**
     * 用户角色（临时 mock 用，写完角色体系后移除）
     */
    private String role = "admin";

    public String getPhone() {
        return DesensitizedUtil.mobilePhone(phone);
    }

    public LocalDate getRegistrationDate() {
        return createTime.toLocalDate();
    }
}
