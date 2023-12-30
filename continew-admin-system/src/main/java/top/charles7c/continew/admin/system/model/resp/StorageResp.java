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

import com.fasterxml.jackson.annotation.JsonIgnore;

import top.charles7c.continew.admin.common.enums.DisEnableStatusEnum;
import top.charles7c.continew.admin.system.enums.StorageTypeEnum;
import top.charles7c.continew.starter.extension.crud.base.BaseResp;

/**
 * 存储库信息
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Data
@Schema(description = "存储库信息")
public class StorageResp extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "存储库1")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "local")
    private String code;

    /**
     * 类型
     */
    @Schema(description = "类型", type = "Integer", allowableValues = {"1", "2"}, example = "2")
    private StorageTypeEnum type;

    /**
     * Access Key
     */
    @Schema(description = "Access Key", example = "")
    private String accessKey;

    /**
     * Secret Key
     */
    @Schema(description = "Secret Key", example = "")
    @JsonIgnore
    private String secretKey;

    /**
     * Endpoint
     */
    @Schema(description = "Endpoint", example = "")
    private String endpoint;

    /**
     * 桶名称
     */
    @Schema(description = "桶名称", example = "C:/continew-admin/data/file/")
    private String bucketName;

    /**
     * 自定义域名
     */
    @Schema(description = "自定义域名", example = "http://localhost:8000/file")
    private String domain;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "存储库描述")
    private String description;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储", example = "true")
    private Boolean isDefault;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    private DisEnableStatusEnum status;

    @Override
    public Boolean getDisabled() {
        return this.getIsDefault();
    }
}