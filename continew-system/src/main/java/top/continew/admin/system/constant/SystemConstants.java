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

package top.continew.admin.system.constant;

/**
 * 系统管理相关常量
 *
 * @author Charles7c
 * @since 4.0.0
 */
public class SystemConstants {

    /**
     * 超级管理员角色 ID（内置且仅有一位超级管理员用户）
     */
    public static final Long SUPER_ADMIN_ROLE_ID = 1L;

    /**
     * 全部权限标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    private SystemConstants() {
    }
}
