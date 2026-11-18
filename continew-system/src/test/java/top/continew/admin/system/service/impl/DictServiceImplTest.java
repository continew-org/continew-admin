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

package top.continew.admin.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.system.mapper.DictMapper;
import top.continew.admin.system.model.entity.DictDO;
import top.continew.admin.system.model.req.DictReq;
import top.continew.admin.system.service.DictItemService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import jakarta.validation.Validator;
import java.util.List;

/**
 * 字典业务实现单元测试。
 *
 * <p>覆盖新增与更新的名称/编码查重、编码不可变更、系统内置字典删除拦截、
 * 级联删除字典项与枚举字典包装。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class DictServiceImplTest {

    private static final Long DICT_ID = 5L;

    private DictMapper dictMapper;
    private DictItemService dictItemService;
    private DictServiceImpl dictService;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
        Validator validator = mock(Validator.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(validator);
        dictMapper = mock(DictMapper.class);
        dictItemService = mock(DictItemService.class);
        dictService = new DictServiceImpl(dictItemService);
        ReflectionTestUtils.setField(dictService, "baseMapper", dictMapper);
        // 父类 getById 经实体类缓存推导类型，mock Mapper 缺代理元数据，预置绕开
        ReflectionTestUtils.setField(dictService, "entityClass", DictDO.class);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldRejectDuplicateNameOnCreate() {
        stubRepeatQuery(true);
        DictReq req = req("测试字典", "test");

        BusinessException exception = assertThrows(BusinessException.class,
            () -> dictService.beforeCreate(req));

        assertEquals("名称为 [测试字典] 的字典已存在", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateCodeOnCreate() {
        stubRepeatQuery(false, true);
        DictReq req = req("测试字典", "test");

        BusinessException exception = assertThrows(BusinessException.class,
            () -> dictService.beforeCreate(req));

        assertEquals("编码为 [test] 的字典已存在", exception.getMessage());
    }

    @Test
    void shouldPassCreateWhenNoRepeat() {
        stubRepeatQuery(false, false);

        dictService.beforeCreate(req("测试字典", "test"));

        verify(dictMapper, times(2)).lambdaQuery();
    }

    @Test
    void shouldRejectCodeChangeOnUpdate() {
        stubRepeatQuery(false);
        when(dictMapper.selectById(DICT_ID)).thenReturn(dict("test"));
        DictReq req = req("测试字典", "changed");

        BusinessException exception = assertThrows(BusinessException.class,
            () -> dictService.beforeUpdate(req, DICT_ID));

        assertEquals("不允许修改字典编码", exception.getMessage());
    }

    @Test
    void shouldPassUpdateWhenCodeUnchanged() {
        stubRepeatQuery(false);
        when(dictMapper.selectById(DICT_ID)).thenReturn(dict("test"));

        dictService.beforeUpdate(req("测试字典", "test"), DICT_ID);

        verify(dictMapper).selectById(DICT_ID);
    }

    @Test
    void shouldRejectDeleteOfSystemDict() {
        stubDeleteQuery(List.of(dict("test", true)));
        List<Long> ids = List.of(DICT_ID);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> dictService.beforeDelete(ids));

        assertEquals("所选字典 [测试字典] 是系统内置字典，不允许删除", exception.getMessage());
        verify(dictItemService, never()).deleteByDictIds(ids);
    }

    @Test
    void shouldCascadeDeleteItemsForNormalDict() {
        stubDeleteQuery(List.of(dict("test", false)));
        List<Long> ids = List.of(DICT_ID);

        dictService.beforeDelete(ids);

        verify(dictItemService).deleteByDictIds(ids);
    }

    @Test
    void shouldWrapEnumDictNames() {
        when(dictItemService.listEnumDictNames()).thenReturn(List.of("gender", "status"));

        List<LabelValueResp> result = dictService.listEnumDict();

        assertEquals(2, result.size());
        assertEquals("gender", result.get(0).getLabel());
        assertEquals("gender", result.get(0).getValue());
    }

    private DictReq req(String name, String code) {
        DictReq req = new DictReq();
        req.setName(name);
        req.setCode(code);
        return req;
    }

    private DictDO dict(String code) {
        return dict(code, false);
    }

    private DictDO dict(String code, boolean isSystem) {
        DictDO dict = new DictDO();
        dict.setId(DICT_ID);
        dict.setName("测试字典");
        dict.setCode(code);
        dict.setIsSystem(isSystem);
        return dict;
    }

    @SuppressWarnings("unchecked")
    private void stubRepeatQuery(boolean exists) {
        stubRepeatQuery(exists, exists);
    }

    @SuppressWarnings("unchecked")
    private void stubRepeatQuery(boolean nameExists, boolean codeExists) {
        LambdaQueryChainWrapper<DictDO> chain = mock(LambdaQueryChainWrapper.class);
        when(dictMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        lenient().when(chain.ne(anyBoolean(), any(SFunction.class), any())).thenReturn(chain);
        when(chain.exists()).thenReturn(nameExists, codeExists);
    }

    @SuppressWarnings("unchecked")
    private void stubDeleteQuery(List<DictDO> dicts) {
        LambdaQueryChainWrapper<DictDO> chain = mock(LambdaQueryChainWrapper.class);
        when(dictMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.select(any(SFunction.class), any(SFunction.class))).thenReturn(chain);
        lenient().when(chain.in(any(SFunction.class), any(java.util.Collection.class)))
            .thenReturn(chain);
        when(chain.list()).thenReturn(dicts);
    }
}
