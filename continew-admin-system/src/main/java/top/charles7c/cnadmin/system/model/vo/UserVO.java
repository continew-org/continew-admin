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

package top.charles7c.cnadmin.system.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

import cn.hutool.core.util.DesensitizedUtil;

import top.charles7c.cnadmin.common.base.BaseVO;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.enums.GenderEnum;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;

/**
 * 用户信息
 *
 * @author Charles7c
 * @since 2023/2/20 21:08
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户信息")
public class UserVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
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
    @Schema(description = "性别（0未知 1男 2女）")
    private GenderEnum gender;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    private String phone;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址")
    private String avatar;

    /**
     * 状态（1启用 2禁用）
     */
    @Schema(description = "状态（1启用 2禁用）")
    private DisEnableStatusEnum status;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 是否禁用修改
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    public Boolean getDisabled() {
        if (userId.equals(LoginHelper.getUserId())) {
            return true;
        }
        return disabled;
    }

    public String getPhone() {
        if (phone == null) {
            return null;
        }
        return DesensitizedUtil.mobilePhone(phone);
    }
}
