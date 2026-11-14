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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.enums.OptionCategoryEnum;
import top.continew.admin.system.enums.PasswordPolicyEnum;
import top.continew.admin.system.mapper.OptionMapper;
import top.continew.admin.system.model.entity.OptionDO;
import top.continew.admin.system.model.query.OptionQuery;
import top.continew.admin.system.model.req.OptionReq;
import top.continew.admin.system.model.req.OptionValueResetReq;
import top.continew.admin.system.model.resp.OptionResp;
import top.continew.admin.system.service.OptionService;
import top.continew.starter.cache.multicache.util.CacheUtils;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.data.util.QueryWrapperHelper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参数业务实现
 *
 * @author Bull-BCLS
 * @since 1.2.0
 */
@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

    private final OptionMapper baseMapper;

    @Override
    public List<OptionResp> list(OptionQuery query) {
        return BeanUtil.copyToList(baseMapper.selectList(QueryWrapperHelper.build(query)),
            OptionResp.class);
    }

    @Override
    @Cacheable(cacheNames = CacheConstants.OPTION_KEY_PREFIX + "MAP:", key = "#category.name()",
        unless = "#result == null")
    public Map<String, String> getByCategory(OptionCategoryEnum category) {
        return baseMapper.selectByCategory(category.name())
            .stream()
            .collect(Collectors.toMap(OptionDO::getCode,
                option -> StrUtil.emptyIfNull(ObjectUtil.defaultIfNull(option
                    .getValue(), option.getDefaultValue())),
                (existing, replacement) -> existing));
    }

    @Override
    public void update(List<OptionReq> options) {
        // 非空校验
        List<Long> optionIds = CollUtils.mapToList(options, OptionReq::getId);
        List<OptionDO> existingOptions = baseMapper.selectByIds(optionIds);
        Map<String, OptionDO> optionByCode = existingOptions.stream()
            .collect(Collectors.toMap(OptionDO::getCode, Function.identity(),
                (existing, replacement) -> existing));
        for (OptionReq req : options) {
            OptionDO option = optionByCode.get(req.getCode());
            ValidationUtils.throwIfNull(option, "参数 [{}] 不存在", req.getCode());
            if (StrUtil.isNotBlank(option.getDefaultValue())) {
                ValidationUtils.throwIfBlank(req.getValue(), "参数 [{}] 的值不能为空",
                    option.getName());
            }
        }
        // 校验密码策略参数取值范围
        Map<String, String> passwordPolicyValueByCode = options.stream()
            .filter(option -> StrUtil.startWith(option.getCode(), PasswordPolicyEnum.CATEGORY
                .name() + StringConstants.UNDERLINE))
            .collect(Collectors.toMap(OptionReq::getCode, OptionReq::getValue,
                (existing, replacement) -> existing));
        for (Map.Entry<String, String> passwordPolicyOptionEntry : passwordPolicyValueByCode
            .entrySet()) {
            String code = passwordPolicyOptionEntry.getKey();
            String value = passwordPolicyOptionEntry.getValue();
            ValidationUtils.throwIf(!NumberUtil.isNumber(value), "参数 [%s] 的值必须为数字", code);
            PasswordPolicyEnum passwordPolicy = PasswordPolicyEnum.valueOf(code);
            passwordPolicy.validateRange(Integer.parseInt(value), passwordPolicyValueByCode);
        }
        baseMapper.updateById(BeanUtil.copyToList(options, OptionDO.class));
        this.clearCache();
    }

    @Override
    public void resetValue(OptionValueResetReq req) {
        String category = req.getCategory();
        List<String> codes = req.getCode();
        ValidationUtils.throwIf(StrUtil.isBlank(category) && CollUtil.isEmpty(codes), "键列表不能为空");
        LambdaUpdateChainWrapper<OptionDO> updateWrapper =
            baseMapper.lambdaUpdate().set(OptionDO::getValue, null);
        if (StrUtil.isNotBlank(category)) {
            updateWrapper.eq(OptionDO::getCategory, category);
        } else {
            updateWrapper.in(OptionDO::getCode, req.getCode());
        }
        updateWrapper.update();
        this.clearCache();
    }

    /**
     * 清除参数缓存
     *
     * <p>
     * 参数数据分布在三个位置：参数值（OPTION:{code}）由 RedisUtils 直接写入 Redis；
     * 公共列表（SITE/TENANT，system CommonController 的 @Cacheable）挂在 cacheName
     * {@code OPTION:} 下；分类参数 Map（{@link #getByCategory}）挂在 cacheName
     * {@code OPTION:MAP:} 下。多级缓存的 cacheName 按名称精确匹配、不做前缀递归，
     * 两个 cacheName 必须各自显式 {@code evictAll} 才能同步失效各节点 L1；
     * Redis 侧的 L2 则由 {@code deleteByPattern} 统一清理。
     * </p>
     */
    private void clearCache() {
        RedisUtils.deleteByPattern(CacheConstants.OPTION_KEY_PREFIX + StringConstants.ASTERISK);
        CacheUtils.evictAll(CacheConstants.OPTION_KEY_PREFIX);
        CacheUtils.evictAll(CacheConstants.OPTION_KEY_PREFIX + "MAP:");
    }

    @Override
    public int getValueByCode2Int(String code) {
        return this.getValueByCode(code, Integer::parseInt);
    }

    @Override
    public <T> T getValueByCode(String code, Function<String, T> mapper) {
        String value = RedisUtils.get(CacheConstants.OPTION_KEY_PREFIX + code);
        if (StrUtil.isNotBlank(value)) {
            return mapper.apply(value);
        }
        OptionDO option = baseMapper.lambdaQuery()
            .eq(OptionDO::getCode, code)
            .select(OptionDO::getValue, OptionDO::getDefaultValue)
            .one();
        CheckUtils.throwIfNull(option, "参数 [{}] 不存在", code);
        value = StrUtil.nullToDefault(option.getValue(), option.getDefaultValue());
        CheckUtils.throwIfBlank(value, "参数 [{}] 数据不正确", code);
        RedisUtils.set(CacheConstants.OPTION_KEY_PREFIX + code, value);
        return mapper.apply(value);
    }
}
