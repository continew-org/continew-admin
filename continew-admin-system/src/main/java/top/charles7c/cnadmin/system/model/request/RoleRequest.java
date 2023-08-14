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

package top.charles7c.cnadmin.system.model.request;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import top.charles7c.cnadmin.common.base.BaseRequest;
import top.charles7c.cnadmin.common.constant.RegexConsts;
import top.charles7c.cnadmin.common.enums.DataScopeEnum;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;

/**
 * 创建或修改角色信息
 *
 * @author Charles7c
 * @since 2023/2/8 23:12
 */
@Data
@Schema(description = "创建或修改角色信息")
public class RoleRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "测试人员")
    @NotBlank(message = "角色名称不能为空")
    @Pattern(regexp = RegexConsts.GENERAL_NAME, message = "角色名称长度为 1 到 20 位，可以包含中文、字母、数字、下划线，短横线")
    private String name;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码", example = "test")
    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = RegexConsts.GENERAL_CODE, message = "角色编码长度为 2 到 16 位，可以包含字母、数字，下划线，以字母开头")
    private String code;

    /**
     * 角色排序
     */
    @Schema(description = "角色排序", example = "1")
    @NotNull(message = "角色排序不能为空")
    private Integer sort;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "测试人员描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 功能权限：菜单 ID 列表
     */
    @Schema(description = "功能权限：菜单 ID 列表", example = "1000,1010,1011,1012,1013,1014")
    private List<Long> menuIds = new ArrayList<>();

    /**
     * 数据权限（1：全部数据权限，2：本部门及以下数据权限，3：本部门数据权限，4：仅本人数据权限，5：自定义数据权限）
     */
    @Schema(description = "数据权限（1：全部数据权限，2：本部门及以下数据权限，3：本部门数据权限，4：仅本人数据权限，5：自定义数据权限）", type = "Integer",
        allowableValues = {"1", "2", "3", "4", "5"}, example = "5")
    private DataScopeEnum dataScope;

    /**
     * 权限范围：部门 ID 列表
     */
    @Schema(description = "权限范围：部门 ID 列表", example = "5")
    private List<Long> deptIds = new ArrayList<>();

    /**
     * 状态（1：启用，2：禁用）
     */
    @Schema(description = "状态（1：启用，2：禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    private DisEnableStatusEnum status;
}
