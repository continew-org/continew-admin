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

package top.continew.admin.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 参数枚举
 *
 * @author Kils
 * @since 2024/05/09 11:25
 */
@Getter
@RequiredArgsConstructor
public enum OptionCodeEnum {

    /**
     * 密码是否允许包含正反序帐户名
     */
    PASSWORD_CONTAIN_NAME("password_contain_name", "密码不允许包含正反序帐户名"),
    /**
     * 密码错误锁定帐户次数
     */
    PASSWORD_ERROR_COUNT("password_error_count", "密码错误锁定帐户次数"),
    /**
     * 密码有效期
     */
    PASSWORD_EXPIRATION_DAYS("password_expiration_days", "密码有效期"),
    /**
     * 密码是否允许包含正反序帐户名
     */
    PASSWORD_LOCK_MINUTES("password_lock_minutes", "密码错误锁定帐户的时间"),
    /**
     * 密码最小长度
     */
    PASSWORD_MIN_LENGTH("password_min_length", "密码最小长度"),
    /**
     * 密码是否必须包含特殊字符
     */
    PASSWORD_SPECIAL_CHAR("password_special_char", "密码是否必须包含特殊字符"),
    /**
     * 修改密码最短间隔
     */
    PASSWORD_UPDATE_INTERVAL("password_update_interval", "修改密码最短间隔");

    private final String value;
    private final String description;
}
