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
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.common.constant.CacheConstants;
import top.continew.admin.system.enums.OptionCodeEnum;
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
import top.continew.starter.data.mybatis.plus.query.QueryWrapperHelper;

import java.util.List;
import java.util.function.Function;

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
    public void update(List<OptionReq> req) {
        RedisUtils.deleteByPattern(CacheConstants.OPTION_KEY_PREFIX + StringConstants.ASTERISK);
        baseMapper.updateBatchById(BeanUtil.copyToList(req, OptionDO.class));
    }

    @Override
    public void resetValue(OptionResetValueReq req) {
        RedisUtils.deleteByPattern(CacheConstants.OPTION_KEY_PREFIX + StringConstants.ASTERISK);
        baseMapper.lambdaUpdate().set(OptionDO::getValue, null).in(OptionDO::getCode, req.getCode()).update();
    }

    @Override
    public int getValueByCode2Int(OptionCodeEnum code) {
        return this.getValueByCode(code, Integer::parseInt);
    }

    @Override
    public <T> T getValueByCode(OptionCodeEnum code, Function<String, T> mapper) {
        String value = RedisUtils.get(CacheConstants.OPTION_KEY_PREFIX + code.getValue());
        if (StrUtil.isNotBlank(value)) {
            return mapper.apply(value);
        }
        LambdaQueryWrapper<OptionDO> queryWrapper = Wrappers.<OptionDO>lambdaQuery()
            .eq(OptionDO::getCode, code.getValue())
            .select(OptionDO::getValue, OptionDO::getDefaultValue);
        OptionDO optionDO = baseMapper.selectOne(queryWrapper);
        CheckUtils.throwIf(ObjUtil.isEmpty(optionDO), "配置 [{}] 不存在", code);
        value = StrUtil.nullToDefault(optionDO.getValue(), optionDO.getDefaultValue());
        CheckUtils.throwIf(StrUtil.isBlank(value), "配置 [{}] 不存在", code);
        RedisUtils.set(CacheConstants.OPTION_KEY_PREFIX + code.getValue(), value);
        return mapper.apply(value);
    }

}