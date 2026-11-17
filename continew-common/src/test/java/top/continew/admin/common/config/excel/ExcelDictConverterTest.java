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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import cn.hutool.extra.spring.SpringUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.continew.admin.common.api.system.DictItemApi;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Easy Excel 字典转换器测试。
 *
 * <p>覆盖导入时标签转值、导出时值转标签与空值/空字典的空串兜底，以及字段
 * 缺少 {@link DictExcelProperty} 注解时报错。字典数据经 SpringUtil 静态桩供给，
 * 不依赖 Spring 上下文。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class ExcelDictConverterTest {

    private final Converter<Object> converter = new ExcelDictConverter();

    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    /**
     * 带字典注解的演示字段
     */
    static class DemoRow {

        @DictExcelProperty("gender")
        private Integer gender;
    }

    /**
     * 无注解的演示字段
     */
    static class PlainRow {

        @SuppressWarnings("unused")
        private Integer plain;
    }

    private static ExcelContentProperty contentProperty(Class<?> clazz, String fieldName)
        throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        ExcelContentProperty property = new ExcelContentProperty();
        property.setField(field);
        return property;
    }

    private void stubDictItems() {
        DictItemApi dictItemApi = mock(DictItemApi.class);
        LabelValueResp male = new LabelValueResp();
        male.setLabel("男");
        male.setValue(1);
        LabelValueResp female = new LabelValueResp();
        female.setLabel("女");
        female.setValue(2);
        doReturn(List.of(male, female)).when(dictItemApi)
            .listByDictCode("gender");
        springUtilMock.when(() -> SpringUtil.getBean(DictItemApi.class)).thenReturn(dictItemApi);
    }

    @Test
    void shouldConvertLabelToValueOnRead() throws Exception {
        stubDictItems();
        ReadCellData<?> cellData = ReadCellData.newInstance("男", (Integer) null, (Integer) null);
        ExcelContentProperty property = contentProperty(DemoRow.class, "gender");

        Object converted =
            converter.convertToJavaData(cellData, property, new GlobalConfiguration());

        assertEquals(1, converted);
    }

    @Test
    void shouldConvertValueToLabelOnWrite() throws Exception {
        stubDictItems();
        ExcelContentProperty property = contentProperty(DemoRow.class, "gender");

        WriteCellData<?> cellData =
            converter.convertToExcelData(1, property, new GlobalConfiguration());

        assertEquals("男", cellData.getStringValue());
    }

    @Test
    void shouldWriteEmptyWhenValueNull() throws Exception {
        stubDictItems();
        ExcelContentProperty property = contentProperty(DemoRow.class, "gender");

        WriteCellData<?> cellData =
            converter.convertToExcelData(null, property, new GlobalConfiguration());

        assertEquals(StringConstants.EMPTY, cellData.getStringValue());
    }

    @Test
    void shouldWriteEmptyWhenDictItemsEmpty() throws Exception {
        DictItemApi dictItemApi = mock(DictItemApi.class);
        doReturn(List.of()).when(dictItemApi).listByDictCode("gender");
        springUtilMock.when(() -> SpringUtil.getBean(DictItemApi.class)).thenReturn(dictItemApi);
        ExcelContentProperty property = contentProperty(DemoRow.class, "gender");

        WriteCellData<?> cellData =
            converter.convertToExcelData(1, property, new GlobalConfiguration());

        assertEquals(StringConstants.EMPTY, cellData.getStringValue());
    }

    @Test
    void shouldThrowWhenFieldMissesDictAnnotation() throws Exception {
        ReadCellData<?> cellData = ReadCellData.newInstance("男", (Integer) null, (Integer) null);
        ExcelContentProperty property = contentProperty(PlainRow.class, "plain");
        GlobalConfiguration globalConfiguration = new GlobalConfiguration();

        assertThrows(IllegalArgumentException.class,
            () -> converter.convertToJavaData(cellData, property, globalConfiguration));
    }

}
