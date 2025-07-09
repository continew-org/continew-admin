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

package top.continew.admin.common.config.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import top.continew.admin.common.service.CommonDictItemService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.List;
import java.util.Objects;

/**
 * Easy Excel 字典转换器
 *
 * @author Charles7c
 * @since 2025/4/9 20:22
 */
public class ExcelDictConverter implements Converter<Object> {

    @Override
    public Object convertToJavaData(ReadCellData<?> cellData,
                                    ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        // 获取字典项数据
        List<LabelValueResp> dictItemList = this.getDictCode(contentProperty);
        // 转换字典标签为字典值
        String value = dictItemList.stream()
            .filter(item -> Objects.equals(cellData.getStringValue(), item.getValue()))
            .findFirst()
            .map(LabelValueResp::getLabel)
            .orElse(null);
        // 转换字典值为对应类型
        return Convert.convert(contentProperty.getField().getType(), value);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object data,
                                                    ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (data == null) {
            return new WriteCellData<>(StringConstants.EMPTY);
        }
        // 获取字典项数据
        List<LabelValueResp> dictItemList = this.getDictCode(contentProperty);
        if (CollUtil.isEmpty(dictItemList)) {
            return new WriteCellData<>(StringConstants.EMPTY);
        }
        // 转换字典值为字典标签
        return new WriteCellData<>(dictItemList.stream()
            .filter(item -> Objects.equals(data, item.getValue()))
            .findFirst()
            .map(LabelValueResp::getLabel)
            .orElse(StringConstants.EMPTY));
    }

    /**
     * 获取字典项数据
     *
     * @param contentProperty Excel 内容属性
     * @return 字典项数据
     */
    private List<LabelValueResp> getDictCode(ExcelContentProperty contentProperty) {
        DictExcelProperty dictExcelProperty = contentProperty.getField().getAnnotation(DictExcelProperty.class);
        if (dictExcelProperty == null) {
            throw new IllegalArgumentException("Excel 字典转换器异常：请为字段添加 @DictExcelProperty 注解");
        }
        CommonDictItemService dictItemService = SpringUtil.getBean(CommonDictItemService.class);
        return dictItemService.listByDictCode(dictExcelProperty.value());
    }
}
