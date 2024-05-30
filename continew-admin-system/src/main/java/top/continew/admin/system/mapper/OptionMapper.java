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

package top.continew.admin.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.continew.admin.system.model.entity.OptionDO;
import top.continew.starter.data.mybatis.plus.base.BaseMapper;

import java.util.List;

/**
 * 参数 Mapper
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
public interface OptionMapper extends BaseMapper<OptionDO> {

    /**
     * 根据类别查询
     *
     * @param category 类别
     * @return 列表
     */
    @Select("SELECT code, value, default_value FROM sys_option WHERE category = #{category}")
    List<OptionDO> selectByCategory(@Param("category") String category);
}