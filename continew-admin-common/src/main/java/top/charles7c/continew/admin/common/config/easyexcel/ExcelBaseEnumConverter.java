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

package top.charles7c.continew.admin.common.config.easyexcel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;

import top.charles7c.continew.starter.core.constant.StringConstants;
import top.charles7c.continew.starter.data.mybatis.plus.base.IBaseEnum;

/**
 * Easy Excel 枚举接口转换器
 *
 * @see IBaseEnum
 * @author Charles7c
 * @since 2023/2/5 19:29
 */
public class ExcelBaseEnumConverter implements Converter<IBaseEnum<Integer>> {

    @Override
    public Class<IBaseEnum> supportJavaTypeKey() {
        return IBaseEnum.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * 转换为 Java 数据（读取 Excel）
     */
    @Override
    public IBaseEnum convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return this.getEnum(IBaseEnum.class, Convert.toStr(cellData.getData()));
    }

    /**
     * 转换为 Excel 数据（写入 Excel）
     */
    @Override
    public WriteCellData<String> convertToExcelData(IBaseEnum<Integer> value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (null == value) {
            return new WriteCellData<>(StringConstants.EMPTY);
        }
        return new WriteCellData<>(value.getDescription());
    }

    /**
     * 通过 value 获取枚举对象，获取不到时为 {@code null}
     *
     * @param enumType
     *            枚举类型
     * @param description
     *            描述
     * @return 对应枚举 ，获取不到时为 {@code null}
     */
    private IBaseEnum<Integer> getEnum(Class<?> enumType, String description) {
        Object[] enumConstants = enumType.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            if (ClassUtil.isAssignable(IBaseEnum.class, enumType)) {
                IBaseEnum<Integer> baseEnum = (IBaseEnum<Integer>)enumConstant;
                if (baseEnum.getDescription().equals(description)) {
                    return baseEnum;
                }
            }
        }
        return null;
    }
}
