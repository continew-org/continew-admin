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
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.enums.PasswordPolicyEnum;
import top.continew.admin.system.mapper.OptionMapper;
import top.continew.admin.system.model.entity.OptionDO;
import top.continew.admin.system.model.query.OptionQuery;
import top.continew.admin.system.model.req.OptionReq;
import top.continew.admin.system.model.req.OptionResetValueReq;
import top.continew.admin.system.model.resp.OptionResp;
import top.continew.admin.system.service.OptionService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.validate.CheckUtils;
import top.continew.starter.core.util.validate.ValidationUtils;
import top.continew.starter.data.mybatis.plus.query.QueryWrapperHelper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参数业务实现
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

    private final OptionMapper baseMapper;

    @Override
    public List<OptionResp> list(OptionQuery query) {
        return BeanUtil.copyToList(baseMapper.selectList(QueryWrapperHelper.build(query)), OptionResp.class);
    }

    @Override
    @Cached(key = "#category", name = CacheConstants.OPTION_KEY_PREFIX + "MAP:")
    public Map<String, String> getByCategory(String category) {
        return baseMapper.selectByCategory(category)
            .stream()
            .collect(Collectors.toMap(OptionDO::getCode, o -> StrUtil.emptyIfNull(ObjectUtil.defaultIfNull(o
                .getValue(), o.getDefaultValue())), (oldVal, newVal) -> oldVal));
    }

    @Override
    public void update(List<OptionReq> options) {
        Map<String, String> passwordPolicyOptionMap = options.stream()
            .filter(option -> StrUtil.startWith(option
                .getCode(), PasswordPolicyEnum.CATEGORY + StringConstants.UNDERLINE))
            .collect(Collectors.toMap(OptionReq::getCode, OptionReq::getValue, (oldVal, newVal) -> oldVal));
        // 校验密码策略参数取值范围
        for (Map.Entry<String, String> passwordPolicyOptionEntry : passwordPolicyOptionMap.entrySet()) {
            String code = passwordPolicyOptionEntry.getKey();
            String value = passwordPolicyOptionEntry.getValue();
            ValidationUtils.throwIf(!NumberUtil.isNumber(value), "参数 [%s] 的值必须为数字", code);
            PasswordPolicyEnum passwordPolicy = PasswordPolicyEnum.valueOf(code);
            passwordPolicy.validateRange(Integer.parseInt(value), passwordPolicyOptionMap);
        }
        RedisUtils.deleteByPattern(CacheConstants.OPTION_KEY_PREFIX + StringConstants.ASTERISK);
        baseMapper.updateBatchById(BeanUtil.copyToList(options, OptionDO.class));
    }

    @Override
    public void resetValue(OptionResetValueReq req) {
        RedisUtils.deleteByPattern(CacheConstants.OPTION_KEY_PREFIX + StringConstants.ASTERISK);
        String category = req.getCategory();
        List<String> codeList = req.getCode();
        ValidationUtils.throwIf(StrUtil.isBlank(category) && CollUtil.isEmpty(codeList), "键列表不能为空");
        LambdaUpdateChainWrapper<OptionDO> chainWrapper = baseMapper.lambdaUpdate().set(OptionDO::getValue, null);
        if (StrUtil.isNotBlank(category)) {
            chainWrapper.eq(OptionDO::getCategory, category);
        } else {
            chainWrapper.in(OptionDO::getCode, req.getCode());
        }
        chainWrapper.update();
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
        CheckUtils.throwIfBlank(value, "参数 [{}] 数据错误", code);
        RedisUtils.set(CacheConstants.OPTION_KEY_PREFIX + code, value);
        return mapper.apply(value);
    }
}