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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import top.continew.admin.system.mapper.OptionMapper;
import top.continew.admin.system.model.entity.OptionDO;
import top.continew.admin.system.model.req.OptionReq;
import top.continew.admin.system.model.req.OptionValueResetReq;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.exception.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * 参数业务测试。
 *
 * <p>覆盖按分类查询的取值合并、参数更新校验（不存在/必填/密码策略数字范围）、
 * 重置条件构造与缓存清理，以及取值缓存未命中时的回源链路。Mapper 与静态工具
 * 用 Mockito 模拟，不依赖 Spring 上下文与外部 Redis。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class OptionServiceImplTest {

    private OptionMapper optionMapper;
    private OptionServiceImpl optionService;
    private MockedStatic<RedisUtils> redisUtilsMock;
    private MockedStatic<CacheUtils> cacheUtilsMock;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        optionMapper = mock(OptionMapper.class);
        optionService = new OptionServiceImpl(optionMapper);
        // RedisUtils/CacheUtils 为静态工具，统一静态桩；RedisUtils 静态块经 SpringUtil 取 RedissonClient，
        // ValidationUtils 静态块经 SpringUtil 取校验器，均先桩 SpringUtil 再模拟静态工具
        springUtilMock = mockStatic(SpringUtil.class);
        jakarta.validation.Validator jakartaValidator = mock(jakarta.validation.Validator.class);
        Mockito.when(jakartaValidator.validate(any())).thenReturn(java.util.Set.of());
        springUtilMock.when(() -> SpringUtil.getBean(jakarta.validation.Validator.class))
            .thenReturn(jakartaValidator);
        springUtilMock.when(() -> SpringUtil.getBean(org.redisson.api.RedissonClient.class))
            .thenReturn(mock(org.redisson.api.RedissonClient.class));
        redisUtilsMock = mockStatic(RedisUtils.class);
        cacheUtilsMock = mockStatic(CacheUtils.class);
    }

    @AfterEach
    void tearDown() {
        redisUtilsMock.close();
        cacheUtilsMock.close();
        springUtilMock.close();
    }

    @Test
    void shouldGetValueThenDefaultValueWhenValueNull() {
        OptionDO option = new OptionDO();
        option.setCode("SITE_TITLE");
        option.setValue(null);
        option.setDefaultValue("ContiNew Admin");
        Mockito.doReturn(List.of(option)).when(optionMapper).selectByCategory("SITE");

        Map<String, String> result =
            optionService.getByCategory(top.continew.admin.system.enums.OptionCategoryEnum.SITE);

        assertEquals("ContiNew Admin", result.get("SITE_TITLE"));
    }

    @Test
    void shouldThrowWhenUpdatingUnknownOptionCode() {
        OptionReq req = new OptionReq();
        req.setId(1L);
        req.setCode("UNKNOWN_CODE");
        req.setValue("x");
        OptionDO existing = new OptionDO();
        existing.setId(1L);
        existing.setCode("SITE_TITLE");
        Mockito.doReturn(List.of(existing)).when(optionMapper).selectByIds(List.of(1L));

        BadRequestException exception =
            assertThrows(BadRequestException.class, () -> optionService.update(List.of(req)));

        assertEquals("参数 [UNKNOWN_CODE] 不存在", exception.getMessage());
        Mockito.verify(optionMapper, Mockito.never()).updateById(Mockito.any(OptionDO.class));
    }

    @Test
    void shouldThrowWhenRequiredOptionValueBlank() {
        OptionReq req = new OptionReq();
        req.setId(1L);
        req.setCode("SITE_TITLE");
        req.setValue("");
        OptionDO existing = new OptionDO();
        existing.setId(1L);
        existing.setCode("SITE_TITLE");
        existing.setName("网站标题");
        existing.setDefaultValue("默认值");
        Mockito.doReturn(List.of(existing)).when(optionMapper).selectByIds(List.of(1L));

        BadRequestException exception =
            assertThrows(BadRequestException.class, () -> optionService.update(List.of(req)));

        assertEquals("参数 [网站标题] 的值不能为空", exception.getMessage());
    }

    @Test
    void shouldThrowWhenPasswordPolicyValueNotNumber() {
        OptionReq req = new OptionReq();
        req.setId(1L);
        req.setCode(top.continew.admin.system.enums.PasswordPolicyEnum.CATEGORY.name()
            + "_EXPIRATION_DAYS");
        req.setValue("abc");
        OptionDO existing = new OptionDO();
        existing.setId(1L);
        existing.setCode(req.getCode());
        Mockito.doReturn(List.of(existing)).when(optionMapper).selectByIds(List.of(1L));

        BadRequestException exception =
            assertThrows(BadRequestException.class, () -> optionService.update(List.of(req)));

        // Validator.throwIf 的占位符为 {} 风格，%s 模板不会替换
        assertEquals("参数 [%s] 的值必须为数字", exception.getMessage());
    }

    @Test
    void shouldClearAllOptionCachesAfterUpdate() {
        OptionReq req = new OptionReq();
        req.setId(1L);
        req.setCode("SITE_TITLE");
        req.setValue("新标题");
        OptionDO existing = new OptionDO();
        existing.setId(1L);
        existing.setCode("SITE_TITLE");
        Mockito.doReturn(List.of(existing)).when(optionMapper).selectByIds(List.of(1L));
        Mockito.doReturn(1).when(optionMapper).updateById(Mockito.any(OptionDO.class));

        optionService.update(List.of(req));

        // 三个缓存位置分别失效：Redis 模式匹配删除 + 两个多级缓存 cacheName
        redisUtilsMock.verify(() -> RedisUtils
            .deleteByPattern(top.continew.admin.common.constant.CacheConstants.OPTION_KEY_PREFIX
                + StringConstants.ASTERISK));
        cacheUtilsMock.verify(() -> CacheUtils
            .evictAll(top.continew.admin.common.constant.CacheConstants.OPTION_KEY_PREFIX));
        cacheUtilsMock.verify(() -> CacheUtils
            .evictAll(top.continew.admin.common.constant.CacheConstants.OPTION_KEY_PREFIX
                + "MAP:"));
    }

    @Test
    void shouldUpdateByCategoryWhenResetGivenCategory() {
        @SuppressWarnings("unchecked")
        LambdaUpdateChainWrapper<OptionDO> updateWrapper =
            mock(LambdaUpdateChainWrapper.class);
        Mockito.doReturn(updateWrapper).when(optionMapper).lambdaUpdate();
        Mockito.doReturn(updateWrapper).when(updateWrapper).set(Mockito.any(), Mockito.any());
        Mockito.doReturn(updateWrapper).when(updateWrapper).eq(Mockito.any(), Mockito.any());
        Mockito.doReturn(true).when(updateWrapper).update();
        OptionValueResetReq req = new OptionValueResetReq();
        req.setCategory("SITE");

        optionService.resetValue(req);

        verify(optionMapper).lambdaUpdate();
        cacheUtilsMock.verify(() -> CacheUtils.evictAll(anyString()), Mockito.atLeast(2));
    }

    @Test
    void shouldThrowWhenResetWithoutCategoryAndCodes() {
        OptionValueResetReq req = new OptionValueResetReq();

        BadRequestException exception =
            assertThrows(BadRequestException.class, () -> optionService.resetValue(req));

        assertEquals("键列表不能为空", exception.getMessage());
    }

    @Test
    void shouldFallbackToDatabaseWhenCacheValueBlank() {
        redisUtilsMock.when(() -> RedisUtils.get(anyString())).thenReturn(null);
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<OptionDO> queryWrapper = mock(LambdaQueryChainWrapper.class);
        Mockito.when(optionMapper.lambdaQuery()).thenReturn(queryWrapper);
        Mockito.doReturn(queryWrapper).when(queryWrapper).eq(Mockito.any(), Mockito.any());
        Mockito.doReturn(queryWrapper).when(queryWrapper)
            .select(Mockito.any(SFunction.class), Mockito.any(SFunction.class));
        OptionDO option = new OptionDO();
        option.setCode("LOGIN_CAPTCHA_ENABLED");
        option.setValue(null);
        option.setDefaultValue("1");
        Mockito.doReturn(option).when(queryWrapper).one();

        Integer value = optionService.getValueByCode2Int("LOGIN_CAPTCHA_ENABLED");

        assertEquals(1, value);
        redisUtilsMock.verify(() -> RedisUtils
            .set(top.continew.admin.common.constant.CacheConstants.OPTION_KEY_PREFIX
                + "LOGIN_CAPTCHA_ENABLED", "1"));
    }

    @Test
    void shouldThrowWhenCodeNotExistsOnGetValue() {
        redisUtilsMock.when(() -> RedisUtils.get(anyString())).thenReturn(null);
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<OptionDO> queryWrapper = mock(LambdaQueryChainWrapper.class);
        Mockito.when(optionMapper.lambdaQuery()).thenReturn(queryWrapper);
        Mockito.doReturn(queryWrapper).when(queryWrapper).eq(Mockito.any(), Mockito.any());
        Mockito.doReturn(queryWrapper).when(queryWrapper)
            .select(Mockito.any(SFunction.class), Mockito.any(SFunction.class));
        Mockito.when(queryWrapper.one()).thenReturn(null);

        assertThrows(BusinessException.class, () -> optionService.getValueByCode2Int("NOT_EXIST"));
    }
}
