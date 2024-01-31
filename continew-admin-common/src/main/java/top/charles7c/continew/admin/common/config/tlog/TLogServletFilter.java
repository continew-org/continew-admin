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

package top.charles7c.continew.admin.common.config.tlog;

import com.yomahub.tlog.context.TLogContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import top.charles7c.continew.admin.common.constant.SysConstants;

import java.io.IOException;

/**
 * TLog 过滤器
 *
 * <p>
 * 重写 TLog 配置以适配 Spring Boot 3.x
 * </p>
 *
 * @see TLogConfiguration
 * @author Jasmine
 * @since 2024/1/30 11:39
 */
@Component
public class TLogServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest && response instanceof HttpServletResponse httpServletResponse) {
            try {
                TLogWebCommon.loadInstance().preHandle(httpServletRequest);
                // 把 traceId 放入 response 的 header，为了方便有些人有这样的需求，从前端拿整条链路的 traceId
                httpServletResponse.addHeader(SysConstants.TRACE_ID, TLogContext.getTraceId());
                chain.doFilter(request, response);
                return;
            } finally {
                TLogWebCommon.loadInstance().afterCompletion();
            }
        }
        chain.doFilter(request, response);
    }
}
