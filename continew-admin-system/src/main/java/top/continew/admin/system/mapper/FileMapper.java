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

import org.apache.ibatis.annotations.Select;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.resp.FileStatisticsResp;
import top.continew.starter.data.mybatis.plus.base.BaseMapper;

import java.util.List;

/**
 * 文件 Mapper
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
public interface FileMapper extends BaseMapper<FileDO> {

    /**
     * 查询文件资源统计信息
     *
     * @return 文件资源统计信息
     */
    @Select("SELECT type, COUNT(1) number, SUM(size) size FROM sys_file GROUP BY type")
    List<FileStatisticsResp> statistics();
}