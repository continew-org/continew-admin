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

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AssembleMethod;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.condition.ConditionOnExpression;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.constant.ContainerConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.GenderEnum;
import top.continew.admin.common.util.helper.LoginHelper;
import top.continew.admin.system.service.DeptService;
import top.continew.starter.extension.crud.converter.ExcelBaseEnumConverter;
import top.continew.starter.extension.crud.model.resp.BaseDetailResp;
import top.continew.starter.file.excel.converter.ExcelListConverter;
import top.continew.starter.security.crypto.annotation.FieldEncrypt;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 用户详情信息
 *
 * @author Charles7c
 * @since 2023/2/20 21:11
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "用户详情信息")
@Assemble(key = "id", prop = ":roleIds", sort = 0, container = ContainerConstants.USER_ROLE_ID_LIST)
public class UserDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    @ExcelProperty(value = "用户名", order = 2)
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    @ExcelProperty(value = "昵称", order = 3)
    private String nickname;

    /**
     * 状态
     */
    @Schema(description = "状态（1：启用；2：禁用）", type = "Integer", allowableValues = {"1", "2"}, example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 4)
    private DisEnableStatusEnum status;

    /**
     * 性别
     */
    @Schema(description = "性别（0：未知；1：男；2：女）", type = "Integer", allowableValues = {"0", "1", "2"}, example = "1")
    @ExcelProperty(value = "性别", converter = ExcelBaseEnumConverter.class, order = 5)
    private GenderEnum gender;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID", example = "5")
    @ConditionOnExpression("#target.deptName == null")
    @AssembleMethod(props = @Mapping(src = "name", ref = "deptName"), targetType = DeptService.class, method = @ContainerMethod(bindMethod = "get", resultType = DeptResp.class))
    @ExcelProperty(value = "部门 ID", order = 6)
    private Long deptId;

    /**
     * 所属部门
     */
    @Schema(description = "所属部门", example = "测试部")
    @ExcelProperty(value = "所属部门", order = 7)
    private String deptName;

    /**
     * 角色 ID 列表
     */
    @Schema(description = "角色 ID 列表", example = "2")
    @Assemble(prop = ":roleNames", container = ContainerConstants.USER_ROLE_NAME_LIST, handlerType = ManyToManyAssembleOperationHandler.class)
    @ExcelProperty(value = "角色 ID 列表", converter = ExcelListConverter.class, order = 8)
    private List<Long> roleIds;

    /**
     * 角色名称列表
     */
    @Schema(description = "角色名称列表", example = "测试人员")
    @ExcelProperty(value = "角色", converter = ExcelListConverter.class, order = 9)
    private List<String> roleNames;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "13811111111")
    @ExcelProperty(value = "手机号码", order = 10)
    @FieldEncrypt
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "123456789@qq.com")
    @ExcelProperty(value = "邮箱", order = 11)
    @FieldEncrypt
    private String email;

    /**
     * 是否为系统内置数据
     */
    @Schema(description = "系统内置", example = "false")
    @ExcelProperty(value = "系统内置", order = 12)
    private Boolean isSystem;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "张三描述信息")
    @ExcelProperty(value = "描述", order = 13)
    private String description;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址", example = "https://himg.bdimg.com/sys/portrait/item/public.1.81ac9a9e.rf1ix17UfughLQjNo7XQ_w.jpg")
    @ExcelProperty(value = "头像地址", order = 14)
    private String avatar;

    /**
     * 最后一次修改密码时间
     */
    @Schema(description = "最后一次修改密码时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime pwdResetTime;

    @Override
    public Boolean getDisabled() {
        return this.getIsSystem() || Objects.equals(this.getId(), LoginHelper.getUserId());
    }
}
