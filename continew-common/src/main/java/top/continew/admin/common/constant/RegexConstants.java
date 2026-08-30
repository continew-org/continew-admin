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
     * 用户名正则（用户名长度为 4-64 个字符，支持大小写字母、数字、下划线，以字母开头）
     */
    public static final String USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{3,64}$";

    /**
     * 密码正则模板（密码长度为 min-max 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字）
     */
    public static final String PASSWORD_TEMPLATE = "^(?=.*\\d)(?=.*[a-z]).{%s,%s}$";

    /**
     * 密码正则（密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字）
     */
    public static final String PASSWORD = "^(?=.*\\d)(?=.*[a-z]).{8,32}$";

    /**
     * 特殊字符正则
     */
    public static final String SPECIAL_CHARACTER =
        "[-_`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t";

    /**
     * 通用编码正则（长度为 2-30 个字符，支持大小写字母、数字、下划线，以字母开头）
     */
    public static final String GENERAL_CODE = "^[a-zA-Z][a-zA-Z0-9_]{1,29}$";

    /**
     * 通用名称正则（长度为 2-30 个字符，支持中文、字母、数字、下划线，短横线）
     */
    public static final String GENERAL_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{2,30}$";

    /**
     * 包名正则（可以包含大小写字母、数字、下划线，每一级包名不能以数字开头）
     */
    public static final String PACKAGE_NAME =
        "^(?:[a-zA-Z_][a-zA-Z0-9_]*\\.)*[a-zA-Z_][a-zA-Z0-9_]*$";

    /**
     * HTTP URL 正则
     */
    public static final String URL_HTTP = "^(https?)://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]$";

    /**
     * HTTP URL 正则（非 IP 地址）
     */
    public static final String URL_HTTP_NOT_IP =
        "^(https?:\\/\\/)([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(\\/[^\\s]*)?$";

    /**
     * HTTP HOST 正则
     * <p>
     * 域名、IPV4、IPV6
     * </p>
     */
    public static final String HTTP_HOST = "^("
        // ① 域名
        + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}"
        // ② IPv4
        + "|(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)"
        // ③ IPv6（8 组 1-4 位十六进制，用 : 分隔，支持压缩 0）
        + "|(?:[0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}"
        // ④ IPv6 压缩形式（::）
        + "|(?:[0-9A-Fa-f]{1,4}:){0,6}::(?:[0-9A-Fa-f]{1,4}:){0,6}[0-9A-Fa-f]{1,4}"
        + ")$";

    private RegexConstants() {
    }
}
