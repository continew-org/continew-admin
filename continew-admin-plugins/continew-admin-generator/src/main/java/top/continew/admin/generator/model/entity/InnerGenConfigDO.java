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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.StrUtils;

import java.io.Serial;
import java.util.List;
import java.util.Set;

/**
 * 内部生成配置信息
 *
 * @author Charles7c
 * @since 2024/8/30 19:35
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InnerGenConfigDO extends GenConfigDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字段配置信息
     */
    private List<FieldConfigDO> fieldConfigs;

    /**
     * 生成时间
     */
    private String datetime;

    /**
     * API 模块名称
     */
    private String apiModuleName;

    /**
     * API 名称
     */
    private String apiName;

    /**
     * 类名
     */
    private String className;

    /**
     * 类名前缀
     */
    private String classNamePrefix;

    /**
     * 子包名称
     */
    private String subPackageName;

    /**
     * 字典编码列表
     */
    private Set<String> dictCodes;

    /**
     * 是否包含必填字段
     */
    private boolean hasRequiredField;

    /**
     * 是否包含字典字段
     */
    private boolean hasDictField;

    /**
     * 是否包含 BigDecimal 字段
     */
    private boolean hasBigDecimalField;

    /**
     * 是否包含 List 字段
     */
    private boolean hasListField;

    /**
     * 是否包含 Time 包字段
     */
    private boolean hasTimeField;

    public InnerGenConfigDO() {
    }

    public InnerGenConfigDO(GenConfigDO genConfig) {
        BeanUtil.copyProperties(genConfig, this);
        this.setDatetime(DateUtil.date().toString("yyyy/MM/dd HH:mm"));
        this.setApiName(StrUtil.lowerFirst(this.getClassNamePrefix()));
    }

    @Override
    public void setPackageName(String packageName) {
        super.setPackageName(packageName);
        String realPackageName = this.getPackageName();
        this.setApiModuleName(StrUtil.subSuf(realPackageName, StrUtil
            .lastIndexOfIgnoreCase(realPackageName, StringConstants.DOT) + 1));
    }

    public String getClassNamePrefix() {
        String tableName = super.getTableName();
        String rawClassName = StrUtils.blankToDefault(super.getTablePrefix(), tableName, prefix -> StrUtil
            .removePrefix(tableName, prefix));
        return StrUtil.upperFirst(StrUtil.toCamelCase(rawClassName));
    }
}
