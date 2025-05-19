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

package top.continew.admin.common.constant;

/**
 * 系统相关常量
 *
 * @author Charles7c
 * @since 2023/2/9 22:11
 */
public class SysConstants {

    /**
     * 否
     */
    public static final Integer NO = 0;

    /**
     * 是
     */
    public static final Integer YES = 1;

    /**
     * 超管用户 ID
     */
    public static final Long SUPER_USER_ID = 1L;

    /**
     * 顶级部门 ID
     */
    public static final Long SUPER_DEPT_ID = 1L;

    /**
     * 顶级父 ID
     */
    public static final Long SUPER_PARENT_ID = 0L;

    /**
     * 超管角色编码
     */
    public static final String SUPER_ROLE_CODE = "admin";

    /**
     * 普通用户角色编码
     */
    public static final String GENERAL_ROLE_CODE = "general";

    /**
     * 超管角色 ID
     */
    public static final Long SUPER_ROLE_ID = 1L;

    /**
     * 普通用户角色 ID
     */
    public static final Long GENERAL_ROLE_ID = 2L;

    /**
     * 全部权限标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 登录 URI
     */
    public static final String LOGIN_URI = "/auth/login";

    /**
     * 登出 URI
     */
    public static final String LOGOUT_URI = "/auth/logout";

    /**
     * 描述类字段后缀
     */
    public static final String DESCRIPTION_FIELD_SUFFIX = "String";

    /**
     * 租户数据库前缀
     */
    public static final String TENANT_DB_PREFIX = "tenant_";

    /**
     * 默认租户
     */
    public static final String DEFAULT_TENANT = "0";

    /**
     * 默认数据源
     */
    public static final String DEFAULT_DATASOURCE = "master";

    /**
     * 租户管理员角色编码
     */
    public static final String TENANT_ADMIN_CODE = "tenant_admin";

    private SysConstants() {
    }
}
