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

package top.continew.admin.tenant.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.starter.extension.crud.validation.CrudValidationGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户创建或修改请求参数
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/26 17:20
 */
@Data
@Schema(description = "租户创建或修改请求参数")
public class TenantReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "Xxx租户")
    @NotBlank(message = "名称不能为空")
    @Length(max = 30, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 域名
     */
    @Schema(description = "域名", example = "https://T0sL6RWv0vFh.continew.top/")
    @Length(max = 255, message = "域名长度不能超过 {max} 个字符")
    @Pattern(regexp = RegexConstants.HTTP_HOST, message = "域名格式不正确")
    private String domain;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间", example = "2023-08-08 08:08:08", type = "string")
    @Future(message = "过期时间必须是未来时间")
    private LocalDateTime expireTime;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "租户描述")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 套餐 ID
     */
    @Schema(description = "套餐 ID", example = "1")
    @NotNull(message = "套餐不能为空")
    private Long packageId;

    /**
     * 管理员用户名
     */
    @Schema(description = "管理员用户名", example = "zhangsan")
    @NotBlank(message = "管理员用户名不能为空", groups = CrudValidationGroup.Create.class)
    @Pattern(regexp = RegexConstants.USERNAME, message = "管理员用户名长度为 4-64 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String adminUsername;

    /**
     * 管理员密码
     */
    @Schema(description = "管理员密码", example = "RSA 公钥加密的管理员密码")
    @NotBlank(message = "管理员密码不能为空", groups = CrudValidationGroup.Create.class)
    private String adminPassword;

    /**
     * 编码
     */
    @Schema(hidden = true)
    private String code;

    /**
     * ID
     */
    @Schema(hidden = true)
    private Long id;
}
