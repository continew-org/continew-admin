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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.mapper.DictItemMapper;
import top.continew.admin.system.model.entity.DictItemDO;
import top.continew.admin.system.model.req.DictItemReq;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.core.autoconfigure.application.ApplicationProperties;
import top.continew.starter.core.enums.BaseEnum;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * 字典项业务实现单元测试。
 *
 * <p>覆盖字典值查重、枚举字典内存缓存命中与初始化、字典码查询的缓存加载、
 * 按字典批量删除与缓存清理。静态枚举缓存在每个用例前后清空，避免相互污染。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class DictItemServiceImplTest {

    private static final Long DICT_ID = 5L;

    private DictItemMapper dictItemMapper;
    private DictItemServiceImpl dictItemService;
    private MockedStatic<SpringUtil> springUtilMock;
    private MockedStatic<CacheUtils> cacheUtilsMock;
    private Map<String, List<LabelValueResp>> enumDictCache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
        Validator validator = mock(Validator.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(validator);
        cacheUtilsMock = mockStatic(CacheUtils.class);
        dictItemMapper = mock(DictItemMapper.class);
        ApplicationProperties properties = new ApplicationProperties();
        properties.setBasePackage("top.continew.admin");
        dictItemService = new DictItemServiceImpl(properties);
        ReflectionTestUtils.setField(dictItemService, "baseMapper", dictItemMapper);
        enumDictCache = (Map<String, List<LabelValueResp>>) ReflectionTestUtils.getField(
            DictItemServiceImpl.class, "ENUM_DICT_CACHE");
        enumDictCache.clear();
    }

    @AfterEach
    void tearDown() {
        enumDictCache.clear();
        cacheUtilsMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldRejectDuplicateValueOnCreate() {
        stubValueRepeat(true);
        DictItemReq req = req("male");

        BusinessException exception = assertThrows(BusinessException.class,
            () -> dictItemService.beforeCreate(req));

        assertEquals("字典值为 [male] 的字典项已存在", exception.getMessage());
        cacheUtilsMock.verify(() -> CacheUtils.evictAll(
            CacheConstants.DICT_KEY_PREFIX), never());
    }

    @Test
    void shouldEvictCacheOnCreateWhenNoRepeat() {
        stubValueRepeat(false);

        dictItemService.beforeCreate(req("male"));

        cacheUtilsMock.verify(() -> CacheUtils.evictAll(CacheConstants.DICT_KEY_PREFIX));
    }

    @Test
    void shouldRejectDuplicateValueOnUpdate() {
        stubValueRepeat(true);
        DictItemReq req = req("male");

        BusinessException exception = assertThrows(BusinessException.class,
            () -> dictItemService.beforeUpdate(req, 9L));

        assertEquals("字典值为 [male] 的字典项已存在", exception.getMessage());
    }

    @Test
    void shouldReturnEnumCacheWhenHit() {
        List<LabelValueResp> cached = List.of(new LabelValueResp("男", "male"));
        enumDictCache.put("gender", cached);

        List<LabelValueResp> result = dictItemService.listByDictCode("GENDER");

        assertSame(cached, result);
        cacheUtilsMock.verifyNoInteractions();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldLoadFromDatabaseWhenEnumCacheMiss() {
        List<LabelValueResp> fromDb = List.of(new LabelValueResp("启用", "1"));
        when(dictItemMapper.listByDictCode("status")).thenReturn(fromDb);
        cacheUtilsMock.when(() -> CacheUtils.get(anyString(), anyString(), any()))
            .thenAnswer(invocation -> {
                Callable<List<LabelValueResp>> loader = invocation.getArgument(2);
                return loader.call();
            });

        List<LabelValueResp> result = dictItemService.listByDictCode("status");

        assertSame(fromDb, result);
        verify(dictItemMapper).listByDictCode("status");
    }

    @Test
    void shouldSkipDeleteWhenIdsEmpty() {
        dictItemService.deleteByDictIds(List.of());

        verify(dictItemMapper, never()).lambdaUpdate();
        cacheUtilsMock.verify(() -> CacheUtils.evictAll(
            CacheConstants.DICT_KEY_PREFIX), never());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteAndEvictWhenIdsPresent() {
        LambdaUpdateChainWrapper<DictItemDO> chain = mock(LambdaUpdateChainWrapper.class);
        when(dictItemMapper.lambdaUpdate()).thenReturn(chain);
        lenient().when(chain.in(any(SFunction.class), any(java.util.Collection.class)))
            .thenReturn(chain);
        when(chain.remove()).thenReturn(true);

        dictItemService.deleteByDictIds(List.of(DICT_ID));

        verify(chain).remove();
        cacheUtilsMock.verify(() -> CacheUtils.evictAll(CacheConstants.DICT_KEY_PREFIX));
    }

    @Test
    void shouldListEnumDictNamesFromCache() {
        enumDictCache.put("gender", List.of());
        enumDictCache.put("status", List.of());

        assertEquals(Set.of("gender", "status"), Set.copyOf(dictItemService.listEnumDictNames()));
    }

    @Test
    void shouldCacheEnumsOnInit() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.setBasePackage("top.continew.admin");
        DictItemServiceImpl service = new DictItemServiceImpl(properties);
        try (MockedStatic<ClassUtil> classUtil = mockStatic(ClassUtil.class)) {
            classUtil.when(() -> ClassUtil.scanPackageBySuper(anyString(), any()))
                .thenReturn(Set.of(SampleEnum.class));

            service.init();

            List<LabelValueResp> cached = enumDictCache.get("sample_enum");
            assertEquals(1, cached.size());
            assertEquals("示例一", cached.get(0).getLabel());
            assertEquals("one", cached.get(0).getValue());
        }
    }

    private DictItemReq req(String value) {
        DictItemReq req = new DictItemReq();
        req.setLabel(value);
        req.setValue(value);
        req.setDictId(DICT_ID);
        return req;
    }

    @SuppressWarnings("unchecked")
    private void stubValueRepeat(boolean exists) {
        LambdaQueryChainWrapper<DictItemDO> chain = mock(LambdaQueryChainWrapper.class);
        when(dictItemMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        lenient().when(chain.ne(anyBoolean(), any(SFunction.class), any())).thenReturn(chain);
        when(chain.exists()).thenReturn(exists);
    }

    private enum SampleEnum implements BaseEnum<String> {

        ONE("one", "示例一");

        private final String value;
        private final String description;

        SampleEnum(String value, String description) {
            this.value = value;
            this.description = description;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
