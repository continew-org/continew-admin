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

package top.charles7c.cnadmin.system.service;

import java.util.List;

import top.charles7c.cnadmin.system.model.query.OptionQuery;
import top.charles7c.cnadmin.system.model.request.OptionRequest;
import top.charles7c.cnadmin.system.model.request.ResetOptionValueRequest;
import top.charles7c.cnadmin.system.model.vo.OptionVO;

/**
 * 系统参数业务接口
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
public interface OptionService {

    /**
     * 查询列表
     *
     * @param query
     *            查询条件
     * @return 列表信息
     */
    List<OptionVO> list(OptionQuery query);

    /**
     * 修改系统参数
     *
     * @param request
     *            参数信息
     */
    void update(List<OptionRequest> request);

    /**
     * 重置系统参数
     *
     * @param request
     *            重置参数信息
     */
    void resetValue(ResetOptionValueRequest request);
}