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

package top.continew.admin.generator.model.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.constant.RegexConstants;
import top.continew.starter.core.util.StrUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生成配置实体
 *
 * @author Charles7c
 * @since 2023/4/12 20:21
 */
@Data
@NoArgsConstructor
@TableName("gen_config")
@Schema(description = "生成配置信息")
public class GenConfigDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表名称
     */
    @Schema(description = "表名称", example = "sys_user")
    @TableId(type = IdType.INPUT)
    @NotBlank(message = "表名称不能为空")
    private String tableName;

    /**
     * 模块名称
     */
    @Schema(description = "模块名称", example = "continew-admin-system")
    @NotBlank(message = "模块名称不能为空")
    @Length(max = 60, message = "模块名称不能超过 {max} 个字符")
    private String moduleName;

    /**
     * 包名称
     */
    @Schema(description = "包名称", example = "top.continew.admin.system")
    @NotBlank(message = "包名称不能为空")
    @Pattern(regexp = RegexConstants.PACKAGE_NAME, message = "包名称格式错误")
    @Length(max = 60, message = "包名称不能超过 {max} 个字符")
    private String packageName;

    /**
     * 业务名称
     */
    @Schema(description = "业务名称", example = "用户")
    @NotBlank(message = "业务名称不能为空")
    @Length(max = 50, message = "业务名称不能超过 {max} 个字符")
    private String businessName;

    /**
     * 作者
     */
    @Schema(description = "作者", example = "Charles7c")
    @NotBlank(message = "作者名称不能为空")
    @Length(max = 100, message = "作者名称不能超过 {max} 个字符")
    private String author;

    /**
     * 表前缀
     */
    @Schema(description = "表前缀", example = "sys_")
    private String tablePrefix;

    /**
     * 是否覆盖
     */
    @Schema(description = "是否覆盖", example = "false")
    @NotNull(message = "是否覆盖不能为空")
    private Boolean isOverride;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 类名前缀
     */
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @TableField(exist = false)
    private String classNamePrefix;

    public GenConfigDO(String tableName) {
        this.tableName = tableName;
    }

    public String getClassNamePrefix() {
        String rawClassName = StrUtils.blankToDefault(this.tablePrefix, this.tableName, prefix -> StrUtil
            .removePrefix(this.tableName, prefix));
        return StrUtil.upperFirst(StrUtil.toCamelCase(rawClassName));
    }
}
