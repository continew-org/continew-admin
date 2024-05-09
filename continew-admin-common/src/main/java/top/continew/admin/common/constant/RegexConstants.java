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
 * 正则相关常量
 *
 * @author Charles7c
 * @since 2023/1/10 20:06
 */
public class RegexConstants {

    /**
     * 用户名正则（长度为 4 到 64 位，可以包含字母、数字，下划线，以字母开头）
     */
    public static final String USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{3,64}$";

    /**
     * 密码正则（长度为 6 到 32 位，可以包含字母、数字、下划线，特殊字符，同时包含字母和数字）
     */
    public static final String PASSWORD = "^(?=.*\\d)(?=.*[a-z]).{6,32}$";

    /**
     * 密码正则严格版（长度为 8 到 32 位，包含至少1个大写字母、1个小写字母、1个数字，1个特殊字符）
     */
    public static final String PASSWORD_STRICT = "^\\S*(?=\\S{8,32})(?=\\S*\\d)(?=\\S*[A-Z])(?=\\S*[a-z])(?=\\S*[!@#$%^&*? ])\\S*$";

    /**
     * 通用编码正则（长度为 2 到 30 位，可以包含字母、数字，下划线，以字母开头）
     */
    public static final String GENERAL_CODE = "^[a-zA-Z][a-zA-Z0-9_]{1,29}$";

    /**
     * 通用名称正则（长度为 2 到 30 位，可以包含中文、字母、数字、下划线，短横线）
     */
    public static final String GENERAL_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{2,30}$";

    /**
     * 包名正则（可以包含大小写字母、数字、下划线，每一级包名不能以数字开头）
     */
    public static final String PACKAGE_NAME = "^(?:[a-zA-Z_][a-zA-Z0-9_]*\\.)*[a-zA-Z_][a-zA-Z0-9_]*$";

    private RegexConstants() {
    }
}
