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

package top.charles7c.continew.admin.system.model.resp;

import java.io.Serial;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import top.charles7c.continew.admin.system.enums.StorageTypeEnum;
import top.charles7c.continew.starter.extension.crud.base.BaseDetailResp;

/**
 * 存储库详情信息
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "存储库详情信息")
public class StorageDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称")
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码")
    @ExcelProperty(value = "编码")
    private String code;

    /**
     * 类型
     */
    @Schema(description = "类型")
    private StorageTypeEnum type;

    /**
     * Access Key
     */
    @Schema(description = "Access Key")
    @ExcelProperty(value = "Access Key")
    private String accessKey;

    /**
     * Secret Key
     */
    @Schema(description = "Secret Key")
    @ExcelProperty(value = "Secret Key")
    private String secretKey;

    /**
     * Endpoint
     */
    @Schema(description = "Endpoint")
    @ExcelProperty(value = "Endpoint")
    private String endpoint;

    /**
     * 桶名称
     */
    @Schema(description = "桶名称")
    @ExcelProperty(value = "桶名称")
    private String bucketName;

    /**
     * 自定义域名
     */
    @Schema(description = "自定义域名")
    @ExcelProperty(value = "自定义域名")
    private String domain;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @ExcelProperty(value = "描述")
    private String description;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储")
    @ExcelProperty(value = "是否为默认存储")
    private Boolean isDefault;

    /**
     * 排序
     */
    @Schema(description = "排序")
    @ExcelProperty(value = "排序")
    private Integer sort;

    /**
     * 状态
     */
    @Schema(description = "状态")
    @ExcelProperty(value = "状态")
    private Integer status;
}