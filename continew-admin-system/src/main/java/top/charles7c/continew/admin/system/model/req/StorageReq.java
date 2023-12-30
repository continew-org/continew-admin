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

package top.charles7c.continew.admin.system.model.req;

import java.io.Serial;

import jakarta.validation.constraints.*;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import top.charles7c.continew.admin.common.constant.RegexConstants;
import top.charles7c.continew.admin.common.enums.DisEnableStatusEnum;
import top.charles7c.continew.admin.system.enums.StorageTypeEnum;
import top.charles7c.continew.starter.extension.crud.base.BaseReq;

/**
 * 创建或修改存储库信息
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Data
@Schema(description = "创建或修改存储库信息")
public class StorageReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "存储库1")
    @NotBlank(message = "名称不能为空")
    @Length(max = 100, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "local")
    @NotBlank(message = "编码不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_CODE, message = "编码长度为 2 到 30 位，可以包含字母、数字，下划线，以字母开头")
    private String code;

    /**
     * 类型
     */
    @Schema(description = "类型", type = "Integer", allowableValues = {"1", "2"}, example = "2")
    @NotNull(message = "类型非法")
    private StorageTypeEnum type;

    /**
     * Access Key
     */
    @Schema(description = "Access Key", example = "")
    @Length(max = 255, message = "Access Key长度不能超过 {max} 个字符")
    private String accessKey;

    /**
     * Secret Key
     */
    @Schema(description = "Secret Key", example = "")
    @Length(max = 255, message = "Secret Key长度不能超过 {max} 个字符")
    private String secretKey;

    /**
     * Endpoint
     */
    @Schema(description = "Endpoint", example = "")
    @Length(max = 255, message = "Endpoint长度不能超过 {max} 个字符")
    private String endpoint;

    /**
     * 桶名称
     */
    @Schema(description = "桶名称", example = "C:/continew-admin/data/file/")
    @Length(max = 255, message = "桶名称长度不能超过 {max} 个字符")
    private String bucketName;

    /**
     * 自定义域名
     */
    @Schema(description = "自定义域名", example = "http://localhost:8000/file")
    @Length(max = 255, message = "自定义域名长度不能超过 {max} 个字符")
    private String domain;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "存储库描述")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储", example = "true")
    @NotNull(message = "是否为默认存储不能为空")
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
}