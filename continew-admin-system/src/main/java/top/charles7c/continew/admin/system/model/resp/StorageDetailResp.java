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

import top.charles7c.continew.starter.extension.crud.converter.ExcelBaseEnumConverter;
import top.charles7c.continew.admin.common.enums.DisEnableStatusEnum;
import top.charles7c.continew.admin.system.enums.StorageTypeEnum;
import top.charles7c.continew.starter.extension.crud.model.resp.BaseDetailResp;

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
    @Schema(description = "名称", example = "存储库1")
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "local")
    @ExcelProperty(value = "编码")
    private String code;

    /**
     * 类型
     */
    @Schema(description = "类型（1：兼容S3协议存储；2：本地存储）", type = "Integer", allowableValues = {"1", "2"}, example = "2")
    @ExcelProperty(value = "类型", converter = ExcelBaseEnumConverter.class)
    private StorageTypeEnum type;

    /**
     * 访问密钥
     */
    @Schema(description = "访问密钥", example = "")
    @ExcelProperty(value = "访问密钥")
    private String accessKey;

    /**
     * 私有密钥
     */
    @Schema(description = "私有密钥", example = "")
    @ExcelProperty(value = "私有密钥")
    private String secretKey;

    /**
     * 终端节点
     */
    @Schema(description = "终端节点", example = "")
    @ExcelProperty(value = "终端节点")
    private String endpoint;

    /**
     * 桶名称
     */
    @Schema(description = "桶名称", example = "C:/continew-admin/data/file/")
    @ExcelProperty(value = "桶名称")
    private String bucketName;

    /**
     * 自定义域名
     */
    @Schema(description = "自定义域名", example = "http://localhost:8000/file")
    @ExcelProperty(value = "自定义域名")
    private String domain;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "存储库描述")
    @ExcelProperty(value = "描述")
    private String description;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储", example = "true")
    @ExcelProperty(value = "是否为默认存储")
    private Boolean isDefault;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序")
    private Integer sort;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class)
    private DisEnableStatusEnum status;

    @Override
    public Boolean getDisabled() {
        return this.getIsDefault();
    }
}