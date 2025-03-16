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

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.model.resp.BaseDetailResp;

import java.io.Serial;

/**
 * 短信记录详情信息
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "短信记录详情信息")
public class SmsRecordDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置id
     */
    @Schema(description = "配置id")
    @ExcelProperty(value = "配置id")
    private Long configId;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @ExcelProperty(value = "手机号")
    private String phone;

    /**
     * 参数配置
     */
    @Schema(description = "参数配置")
    @ExcelProperty(value = "参数配置")
    private String params;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    @ExcelProperty(value = "发送状态")
    private Boolean status;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据")
    @ExcelProperty(value = "返回数据")
    private String resMsg;
}