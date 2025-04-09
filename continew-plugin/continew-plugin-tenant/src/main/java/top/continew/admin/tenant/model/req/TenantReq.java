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
import top.continew.starter.extension.crud.validation.CrudValidationGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建或修改租户参数
 *
 * @author 小熊
 * @since 2024/11/26 17:20
 */
@Data
@Schema(description = "创建或修改租户参数")
public class TenantReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户名称
     */
    @Schema(description = "租户名称")
    @NotBlank(message = "租户名称不能为空")
    @Length(max = 64, message = "租户名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 绑定的域名
     */
    @Schema(description = "绑定的域名")
    @Length(max = 128, message = "绑定的域名长度不能超过 {max} 个字符")
    private String domain;

    /**
     * 租户套餐编号
     */
    @Schema(description = "租户套餐编号")
    @NotNull(message = "租户套餐编号不能为空")
    private Long packageId;

    /**
     * 状态（1：启用；2：禁用）
     */
    @Schema(description = "状态")
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 租户过期时间
     */
    @Schema(description = "租户过期时间")
    @Future(message = "过期时间必须是未来时间")
    private LocalDateTime expireTime;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank(message = "用户名不能为空", groups = CrudValidationGroup.Create.class)
    @Pattern(regexp = RegexConstants.USERNAME, message = "用户名长度为 4-64 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String username;

    /**
     * 密码（加密）
     */
    @Schema(description = "密码（加密）", example = "E7c72TH+LDxKTwavjM99W1MdI9Lljh79aPKiv3XB9MXcplhm7qJ1BJCj28yaflbdVbfc366klMtjLIWQGqb0qw==")
    @NotBlank(message = "密码不能为空", groups = CrudValidationGroup.Create.class)
    private String password;

    /**
     * 租户编号
     */
    private String tenantSn;

    /**
     * 隔离级别
     */
    @Schema(description = "隔离级别")
    @NotNull(message = "隔离级别不能为空", groups = CrudValidationGroup.Create.class)
    private Integer isolationLevel;

    /**
     * 数据连接ID
     */
    @Schema(description = "数据连接ID")
    private Long dbConnectId;

    /**
     * ID
     */
    @Schema(hidden = true)
    private Long id;
}