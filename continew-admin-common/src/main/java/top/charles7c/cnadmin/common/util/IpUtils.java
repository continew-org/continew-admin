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

package top.charles7c.cnadmin.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import cn.hutool.core.net.NetUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import top.charles7c.cnadmin.common.config.properties.ContinewAdminProperties;

import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import net.dreamlu.mica.ip2region.core.IpInfo;

/**
 * IP 工具类
 *
 * @author Charles7c
 * @since 2022/12/23 20:00
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IpUtils {

    /**
     * 太平洋网开放 API，查询 IP 归属地
     */
    private static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true";

    /**
     * 根据IP获取详细地址
     *
     * @param ip
     *            IP地址
     * @return 详细地址
     */
    public static String getCityInfo(String ip) {
        if (ContinewAdminProperties.IP_ADDR_LOCAL_PARSE_ENABLED) {
            return getLocalCityInfo(ip);
        } else {
            return getHttpCityInfo(ip);
        }
    }

    /**
     * 根据 IP 获取详细地址（网络解析）
     *
     * @param ip
     *            IP地址
     * @return 详细地址
     */
    public static String getHttpCityInfo(String ip) {
        if (isInnerIP(ip)) {
            return "内网IP";
        }
        String api = String.format(IP_URL, ip);
        JSONObject object = JSONUtil.parseObj(HttpUtil.get(api));
        return object.get("addr", String.class);
    }

    /**
     * 根据 IP 获取详细地址（本地解析）
     *
     * @param ip
     *            IP 地址
     * @return 详细地址
     */
    public static String getLocalCityInfo(String ip) {
        if (isInnerIP(ip)) {
            return "内网IP";
        }
        Ip2regionSearcher ip2regionSearcher = SpringUtil.getBean(Ip2regionSearcher.class);
        IpInfo ipInfo = ip2regionSearcher.memorySearch(ip);
        if (ipInfo != null) {
            return ipInfo.getAddress();
        }
        return null;
    }

    /**
     * 是否为内网IPv4
     *
     * @param ip
     *            IP 地址
     * @return 是否为内网IP
     */
    public static boolean isInnerIP(String ip) {
        ip = "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(ip);
        return NetUtil.isInnerIP(ip);
    }
}
