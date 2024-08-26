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

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.Cached;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.mapper.DictItemMapper;
import top.continew.admin.system.model.entity.DictItemDO;
import top.continew.admin.system.model.query.DictItemQuery;
import top.continew.admin.system.model.req.DictItemReq;
import top.continew.admin.system.model.resp.DictItemResp;
import top.continew.admin.system.service.DictItemService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.autoconfigure.project.ProjectProperties;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.enums.BaseEnum;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.extension.crud.service.impl.BaseServiceImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 字典项业务实现
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Service
@RequiredArgsConstructor
public class DictItemServiceImpl extends BaseServiceImpl<DictItemMapper, DictItemDO, DictItemResp, DictItemResp, DictItemQuery, DictItemReq> implements DictItemService {

    private final ProjectProperties projectProperties;
    private static final Map<String, List<LabelValueResp>> ENUM_DICT_CACHE = new ConcurrentHashMap<>();

    @Override
    protected void beforeAdd(DictItemReq req) {
        String value = req.getValue();
        CheckUtils.throwIf(this.isValueExists(value, null, req.getDictId()), "新增失败，字典值 [{}] 已存在", value);
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    protected void beforeUpdate(DictItemReq req, Long id) {
        String value = req.getValue();
        CheckUtils.throwIf(this.isValueExists(value, id, req.getDictId()), "修改失败，字典值 [{}] 已存在", value);
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    @Cached(key = "#dictCode", name = CacheConstants.DICT_KEY_PREFIX)
    public List<LabelValueResp> listByDictCode(String dictCode) {
        return Optional.ofNullable(ENUM_DICT_CACHE.get(dictCode.toLowerCase()))
            .orElseGet(() -> baseMapper.listByDictCode(dictCode));
    }

    @Override
    public void deleteByDictIds(List<Long> dictIds) {
        baseMapper.lambdaUpdate().in(DictItemDO::getDictId, dictIds).remove();
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    public List<String> listEnumDictNames() {
        return ENUM_DICT_CACHE.keySet().stream().toList();
    }

    /**
     * 字典值是否存在
     *
     * @param value  字典值
     * @param id     ID
     * @param dictId 字典 ID
     * @return 是否存在
     */
    private boolean isValueExists(String value, Long id, Long dictId) {
        return baseMapper.lambdaQuery()
            .eq(DictItemDO::getValue, value)
            .eq(DictItemDO::getDictId, dictId)
            .ne(null != id, DictItemDO::getId, id)
            .exists();
    }

    /**
     * 将枚举转换为枚举字典
     *
     * @param enumClass 枚举类型
     * @return 枚举字典
     */
    private List<LabelValueResp> toEnumDict(Class<?> enumClass) {
        Object[] enumConstants = enumClass.getEnumConstants();
        return Arrays.stream(enumConstants).map(e -> {
            BaseEnum baseEnum = (BaseEnum)e;
            return new LabelValueResp(baseEnum.getDescription(), baseEnum.getValue(), baseEnum.getColor());
        }).toList();
    }

    /**
     * 缓存枚举字典
     */
    @PostConstruct
    public void init() {
        Set<Class<?>> classSet = ClassUtil.scanPackageBySuper(projectProperties.getBasePackage(), BaseEnum.class);
        ENUM_DICT_CACHE.putAll(classSet.stream()
            .collect(Collectors.toMap(cls -> StrUtil.toUnderlineCase(cls.getSimpleName())
                .toLowerCase(), this::toEnumDict)));
    }
}