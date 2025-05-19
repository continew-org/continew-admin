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

package top.continew.admin.system.model.resp.role;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.common.constant.ContainerConstants;
import top.continew.admin.common.constant.SysConstants;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.enums.GenderEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 角色关联用户响应参数
 *
 * @author Charles7c
 * @since 2025/2/5 22:01
 */
@Data
@Schema(description = "角色关联用户响应参数")
public class RoleUserResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID", example = "1")
    private Long roleId;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID", example = "1")
    @Assemble(prop = ":roleIds", sort = 0, container = ContainerConstants.USER_ROLE_ID_LIST)
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "1")
    private GenderEnum gender;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 是否为系统内置数据
     */
    @Schema(description = "是否为系统内置数据", example = "false")
    private Boolean isSystem;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "测试人员描述信息")
    private String description;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID", example = "5")
    private Long deptId;

    /**
     * 所属部门
     */
    @Schema(description = "所属部门", example = "测试部")
    private String deptName;

    /**
     * 角色 ID 列表
     */
    @Schema(description = "角色 ID 列表", example = "2")
    @Assemble(prop = ":roleNames", container = ContainerConstants.USER_ROLE_NAME_LIST, handlerType = ManyToManyAssembleOperationHandler.class)
    private List<Long> roleIds;

    /**
     * 角色名称列表
     */
    @Schema(description = "角色名称列表", example = "测试人员")
    private List<String> roleNames;

    public Boolean getDisabled() {
        return this.getIsSystem() && Objects.equals(roleId, SysConstants.SUPER_ROLE_ID);
    }
}
