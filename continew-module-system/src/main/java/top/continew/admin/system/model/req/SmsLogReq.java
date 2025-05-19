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

package top.continew.admin.system.model.req;

import lombok.Data;
import top.continew.admin.common.enums.SuccessFailureStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信日志创建或修改请求参数
 *
 * @author luoqiz
 * @author Charles7c
 * @since 2025/03/15 22:15
 */
@Data
public class SmsLogReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置 ID
     */
    private Long configId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 参数配置
     */
    private String params;

    /**
     * 发送状态
     */
    private SuccessFailureStatusEnum status;

    /**
     * 返回数据
     */
    private String resMsg;
}