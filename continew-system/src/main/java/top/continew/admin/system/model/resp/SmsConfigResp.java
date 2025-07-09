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
import top.continew.admin.common.config.excel.DictExcelProperty;
import top.continew.admin.common.config.excel.ExcelDictConverter;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.resp.BaseDetailResp;
import top.continew.starter.file.excel.converter.ExcelBaseEnumConverter;
import top.continew.starter.security.mask.annotation.JsonMask;

import java.io.Serial;

/**
 * 短信配置响应参数
 *
 * @author luoqiz
 * @author Charles7c
 * @since 2025/03/15 18:41
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "短信配置响应参数")
public class SmsConfigResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "短信配置1")
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * 厂商
     *
     * @see org.dromara.sms4j.comm.constant.SupplierConstant
     */
    @Schema(description = "厂商", example = "cloopen")
    @ExcelProperty(value = "厂商", converter = ExcelDictConverter.class)
    @DictExcelProperty("sms_supplier")
    private String supplier;

    /**
     * Access Key
     */
    @Schema(description = "Access Key", example = "7aaf0708674db3ee05676ecbc2f31b7b")
    @ExcelProperty(value = "Access Key")
    private String accessKey;

    /**
     * Secret Key
     */
    @Schema(description = "Secret Key", example = "7fd4************************57be")
    @ExcelProperty(value = "Secret Key")
    @JsonMask(left = 4, right = 4)
    private String secretKey;

    /**
     * 短信签名
     */
    @Schema(description = "短信签名", example = "")
    @ExcelProperty(value = "短信签名")
    private String signature;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID", example = "1")
    @ExcelProperty(value = "模板 ID")
    private String templateId;

    /**
     * 负载均衡权重
     */
    @Schema(description = "负载均衡权重", example = "1")
    @ExcelProperty(value = "负载均衡权重")
    private Integer weight;

    /**
     * 重试间隔（单位：秒）
     */
    @Schema(description = "重试间隔（单位：秒）", example = "5")
    @ExcelProperty(value = "重试间隔（单位：秒）")
    private Integer retryInterval;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数", example = "0")
    @ExcelProperty(value = "重试次数")
    private Integer maxRetries;

    /**
     * 发送上限
     */
    @Schema(description = "发送上限")
    @ExcelProperty(value = "发送上限")
    private Integer maximum;

    /**
     * 各个厂商独立配置
     */
    @Schema(description = "各个厂商独立配置", example = "")
    @ExcelProperty(value = "各个厂商独立配置")
    private String supplierConfig;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储", example = "true")
    @ExcelProperty(value = "是否为默认存储")
    private Boolean isDefault;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class)
    private DisEnableStatusEnum status;
}