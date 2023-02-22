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

package top.charles7c.cnadmin.system.model.vo;

import java.util.List;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import top.charles7c.cnadmin.common.base.BaseDetailVO;
import top.charles7c.cnadmin.common.config.easyexcel.ExcelBaseEnumConverter;
import top.charles7c.cnadmin.common.enums.DisEnableStatusEnum;
import top.charles7c.cnadmin.common.enums.GenderEnum;

/**
 * 用户详情信息
 *
 * @author Charles7c
 * @since 2023/2/20 21:11
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "用户详情信息")
public class UserDetailVO extends BaseDetailVO {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @ExcelProperty(value = "用户名")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @ExcelProperty(value = "昵称")
    private String nickname;

    /**
     * 性别（0未知 1男 2女）
     */
    @Schema(description = "性别（0未知 1男 2女）")
    @ExcelProperty(value = "性别", converter = ExcelBaseEnumConverter.class)
    private GenderEnum gender;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    @ExcelProperty(value = "邮箱")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    @ExcelProperty(value = "手机号码")
    private String phone;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址")
    @ExcelProperty(value = "头像地址")
    private String avatar;

    /**
     * 状态（1启用 2禁用）
     */
    @Schema(description = "状态（1启用 2禁用）")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class)
    private DisEnableStatusEnum status;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @ExcelProperty(value = "描述")
    private String description;

    /**
     * 角色 ID 列表
     */
    @Schema(description = "角色 ID 列表")
    private List<Long> roleIds;

    /**
     * 所属角色
     */
    @Schema(description = "所属角色")
    @ExcelProperty(value = "所属角色")
    private String roleNames;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID")
    private Long deptId;

    /**
     * 所属部门
     */
    @Schema(description = "所属部门")
    @ExcelProperty(value = "所属部门")
    private String deptName;
}
