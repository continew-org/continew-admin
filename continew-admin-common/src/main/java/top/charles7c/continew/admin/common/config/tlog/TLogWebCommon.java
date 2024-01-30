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

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TLog
 *
 * @see TLogWebCommon
 * @author Jasmine
 * @since 2024/01/30 11:39
 */
public class TLogWebCommon extends TLogRPCHandler {

    private final static Logger log = LoggerFactory.getLogger(TLogWebCommon.class);

    private static volatile TLogWebCommon tLogWebCommon;

    public static TLogWebCommon loadInstance() {
        if (tLogWebCommon == null) {
            synchronized (TLogWebCommon.class) {
                if (tLogWebCommon == null) {
                    tLogWebCommon = new TLogWebCommon();
                }
            }
        }
        return tLogWebCommon;
    }

    public void preHandle(HttpServletRequest request) {
        String traceId = request.getHeader(TLogConstants.TLOG_TRACE_KEY);
        String spanId = request.getHeader(TLogConstants.TLOG_SPANID_KEY);
        String preIvkApp = request.getHeader(TLogConstants.PRE_IVK_APP_KEY);
        String preIvkHost = request.getHeader(TLogConstants.PRE_IVK_APP_HOST);
        String preIp = request.getHeader(TLogConstants.PRE_IP_KEY);

        TLogLabelBean labelBean = new TLogLabelBean(preIvkApp, preIvkHost, preIp, traceId, spanId);

        processProviderSide(labelBean);
    }

    public void afterCompletion() {
        cleanThreadLocal();
    }
}
