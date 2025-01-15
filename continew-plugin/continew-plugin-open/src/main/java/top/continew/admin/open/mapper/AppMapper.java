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

package top.continew.admin.open.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.continew.admin.open.model.entity.AppDO;
import top.continew.starter.data.mp.base.BaseMapper;
import top.continew.starter.security.crypto.annotation.FieldEncrypt;

/**
 * 应用 Mapper
 *
 * @author chengzi
 * @since 2024/10/17 16:03
 */
public interface AppMapper extends BaseMapper<AppDO> {

    /**
     * 根据 Access Key 查询
     *
     * @param accessKey Access Key
     * @return 应用信息
     */
    @Select("select * from sys_app where access_key = #{accessKey}")
    AppDO selectByAccessKey(@FieldEncrypt @Param("accessKey") String accessKey);
}