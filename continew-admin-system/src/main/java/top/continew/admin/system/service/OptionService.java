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

package top.continew.admin.system.service;

import java.util.List;
import java.util.function.Function;

import top.continew.admin.system.enums.OptionCodeEnum;
import top.continew.admin.system.model.query.OptionQuery;
import top.continew.admin.system.model.req.OptionReq;
import top.continew.admin.system.model.req.OptionResetValueReq;
import top.continew.admin.system.model.resp.OptionResp;

/**
 * 参数业务接口
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
public interface OptionService {

    /**
     * 查询列表
     *
     * @param query 查询条件
     * @return 列表信息
     */
    List<OptionResp> list(OptionQuery query);

    /**
     * 修改参数
     *
     * @param req 参数信息
     */
    void update(List<OptionReq> req);

    /**
     * 重置参数
     *
     * @param req 重置信息
     */
    void resetValue(OptionResetValueReq req);

    /**
     * 根据code获取int参数值
     * 
     * @param code code
     * @return 参数值
     */
    int getValueByCode2Int(OptionCodeEnum code);

    /**
     * 根据code获取参数值
     * 
     * @param code   code
     * @param mapper 类型转换 ex:value -> Integer.parseInt(value)
     * @return 参数值
     */
    <T> T getValueByCode(OptionCodeEnum code, Function<String, T> mapper);
}