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

package top.charles7c.continew.admin.system.model.resp;

import java.io.Serial;
import java.util.Objects;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import cn.hutool.core.util.DesensitizedUtil;

import top.charles7c.continew.admin.common.enums.DisEnableStatusEnum;
import top.charles7c.continew.admin.common.enums.GenderEnum;
import top.charles7c.continew.admin.common.util.helper.LoginHelper;
import top.charles7c.continew.starter.extension.crud.model.resp.BaseResp;

/**
 * 用户信息
 *
 * @author Charles7c
 * @since 2023/2/20 21:08
 */
@Data
@Schema(description = "用户信息")
public class UserResp extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 性别
     */
    @Schema(description = "性别（0：未知；1：男；2：女）", type = "Integer", allowableValues = {"0", "1", "2"}, example = "1")
    private GenderEnum gender;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址", example = "https://himg.bdimg.com/sys/portrait/item/public.1.81ac9a9e.rf1ix17UfughLQjNo7XQ_w.jpg")
    private String avatar;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "123456789@qq.com")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "13811111111")
    private String phone;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    private DisEnableStatusEnum status;

    /**
     * 是否为系统内置数据
     */
    @Schema(description = "是否为系统内置数据", example = "false")
    private Boolean isSystem;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "张三描述信息")
    private String description;

    public String getEmail() {
        return DesensitizedUtil.email(email);
    }

    public String getPhone() {
        return DesensitizedUtil.mobilePhone(phone);
    }

    @Override
    public Boolean getDisabled() {
        return this.getIsSystem() || Objects.equals(this.getId(), LoginHelper.getUserId());
    }
}
