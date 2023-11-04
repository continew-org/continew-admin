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

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;

/**
 * Servlet 工具类
 *
 * @author Charles7c
 * @since 2022/12/23 20:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServletUtils {

    /**
     * 获取请求对象
     *
     * @return /
     */
    public static HttpServletRequest getRequest() {
        return getServletRequestAttributes().getRequest();
    }

    /**
     * 获取响应对象
     *
     * @return /
     */
    public static HttpServletResponse getResponse() {
        return getServletRequestAttributes().getResponse();
    }

    /**
     * 获取浏览器及其版本信息
     *
     * @param request
     *            请求对象
     * @return 浏览器及其版本信息
     */
    public static String getBrowser(HttpServletRequest request) {
        if (null == request) {
            return null;
        }
        UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
        return userAgent.getBrowser().getName() + " " + userAgent.getVersion();
    }

    private static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes)Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
    }
}
