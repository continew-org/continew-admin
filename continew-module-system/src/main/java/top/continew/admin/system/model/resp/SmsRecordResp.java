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

package top.continew.admin.system.model.resp;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import top.continew.admin.common.model.resp.BaseResp;

import java.io.Serial;
import java.time.*;

/**
 * 短信记录信息
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Data
@Schema(description = "短信记录信息")
public class SmsRecordResp extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置id
     */
    @Schema(description = "配置id")
    private Long configId;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 参数配置
     */
    @Schema(description = "参数配置")
    private String params;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    private Boolean status;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据")
    private String resMsg;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private Long updateUser;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}