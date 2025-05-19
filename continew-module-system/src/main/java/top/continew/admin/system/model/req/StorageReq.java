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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.enums.StorageTypeEnum;
import top.continew.admin.system.validation.ValidationGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 存储创建或修改请求参数
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Data
@Schema(description = "存储创建或修改请求参数")
public class StorageReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "存储1")
    @NotBlank(message = "名称不能为空")
    @Length(max = 100, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "local")
    @NotBlank(message = "编码不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_CODE, message = "编码长度为 2-30 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String code;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    @NotNull(message = "类型无效")
    private StorageTypeEnum type;

    /**
     * Access Key
     */
    @Schema(description = "Access Key", example = "")
    @Length(max = 255, message = "Access Key长度不能超过 {max} 个字符")
    @NotBlank(message = "Access Key不能为空", groups = ValidationGroup.Storage.OSS.class)
    private String accessKey;

    /**
     * Secret Key
     */
    @Schema(description = "Secret Key", example = "")
    @NotBlank(message = "Secret Key不能为空", groups = ValidationGroup.Storage.OSS.class)
    private String secretKey;

    /**
     * Endpoint
     */
    @Schema(description = "Endpoint", example = "")
    @Length(max = 255, message = "Endpoint长度不能超过 {max} 个字符")
    @NotBlank(message = "Endpoint不能为空", groups = ValidationGroup.Storage.OSS.class)
    private String endpoint;

    /**
     * Bucket/存储路径
     */
    @Schema(description = "Bucket/存储路径", example = "C:/continew-admin/data/file/")
    @Length(max = 255, message = "Bucket长度不能超过 {max} 个字符", groups = ValidationGroup.Storage.OSS.class)
    @Length(max = 255, message = "存储路径长度不能超过 {max} 个字符", groups = ValidationGroup.Storage.Local.class)
    @NotBlank(message = "Bucket不能为空", groups = ValidationGroup.Storage.OSS.class)
    @NotBlank(message = "存储路径不能为空", groups = ValidationGroup.Storage.Local.class)
    private String bucketName;

    /**
     * 域名/访问路径
     */
    @Schema(description = "域名/访问路径", example = "http://localhost:8000/file")
    @Length(max = 255, message = "域名长度不能超过 {max} 个字符", groups = ValidationGroup.Storage.OSS.class)
    @Length(max = 255, message = "访问路径长度不能超过 {max} 个字符", groups = ValidationGroup.Storage.Local.class)
    @NotBlank(message = "访问路径不能为空", groups = ValidationGroup.Storage.Local.class)
    private String domain;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "存储描述")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 是否为默认存储
     */
    @JsonIgnore
    private Boolean isDefault;
}